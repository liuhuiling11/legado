package io.legado.app.help

import androidx.annotation.Keep
import io.legado.app.constant.AppConst
import io.legado.app.data.entities.fuyou.FeelBehave
import io.legado.app.data.entities.fuyou.FuYouUser
import io.legado.app.data.entities.fuyou.FyComment
import io.legado.app.data.entities.fuyou.FyFeel
import io.legado.app.data.entities.fuyou.FyFindbook
import io.legado.app.data.entities.fuyou.FyMessage
import io.legado.app.data.entities.fuyou.FyMessageComment
import io.legado.app.data.entities.fuyou.FyMessageFeel
import io.legado.app.data.entities.fuyou.FyNovel
import io.legado.app.data.entities.fuyou.FyReply
import io.legado.app.data.entities.fuyou.FyResponse
import io.legado.app.data.entities.fuyou.PageRequest
import io.legado.app.data.entities.fuyou.PageResponse
import io.legado.app.data.entities.fuyou.ReadBehave
import io.legado.app.exception.NoStackTraceException
import io.legado.app.help.config.AppConfig
import io.legado.app.help.config.LocalConfig
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.help.http.addHeaders
import io.legado.app.help.http.getProxyClient
import io.legado.app.help.http.newCallStrResponse
import io.legado.app.help.http.postJson
import io.legado.app.utils.DebugLog
import io.legado.app.utils.GSON
import io.legado.app.utils.fromJsonArray
import kotlinx.coroutines.CoroutineScope

@Keep
@Suppress
object FuYouHelpPost : FuYouHelp.FuYouHelpInterface {

        private const val baseUrl = "ws:www.liuhuiling.cn/fuyouapi"
//    private const val baseUrl = "ws:10.0.2.2:8080"
    private const val timeOut = 20000L

    private suspend fun post(url: String, bodyMap: String): FyResponse? {
        if (LocalConfig.fyToken == null || LocalConfig.fyToken == "") {
            login(FuYouUser(AppConst.androidId, LocalConfig.password ?: "1234567", "", ""))
        }
        val headerMap = HashMap<String, String>()
        headerMap["Authorization"] = "Bearer " + LocalConfig.fyToken
        return post(url, headerMap, bodyMap)

    }


    /**
     * 发送请求
     */
    private suspend fun post(
        url: String,
        headerMap: HashMap<String, String>?,
        bodyJson: String
    ): FyResponse? {
        try {
            val header = HashMap<String, String>()
            header["Content-Type"] = "application/json"
            if (headerMap != null) {
                header.putAll(headerMap)
            }
            DebugLog.i("post蜉蝣", "$url 请求：$bodyJson")
            val resR = getProxyClient(null).newCallStrResponse() {
                addHeaders(header)
                url(baseUrl + url)
                postJson(bodyJson)
            }
//            DebugLog.i("post蜉蝣", "$url 响应：$resR")
            if (resR.isSuccessful() && resR.code() == 200) {
                var fyResponse = GSON.fromJson(resR.body, FyResponse::class.java)
                if (fyResponse.code != "401") {
                    return fyResponse
                } else {
                    //token失效
//                    DebugLog.e("登录失效","旧token:"+LocalConfig.fyToken)
                    //重新登录
                    login(
                        FuYouUser(AppConst.androidId, LocalConfig.password ?: "1234567", "", "")
                    )
                    //再次发送
//                    DebugLog.e("登录失效","再重新请求，新"+LocalConfig.fyToken)
                    header["Authorization"] = "Bearer " + LocalConfig.fyToken.toString()
                    val newResR = getProxyClient(null).newCallStrResponse() {
                        addHeaders(header)
                        url(baseUrl + url)
                        postJson(bodyJson)
                    }
                    if (newResR.isSuccessful() && newResR.code() == 200) {
//                        DebugLog.e("登录失效","再重新请求成功，获取到数据${newResR.body}")
                        fyResponse = GSON.fromJson(newResR.body, FyResponse::class.java)
                        return fyResponse
                    }
                    return null
                }
            } else {
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw NoStackTraceException("远程post请求失败")
        }
    }

    /**
     * 登录或注册
     */
    override suspend fun login(user: FuYouUser): FuYouUser {
        val response = post("/auth/read-login", null, GSON.toJson(user))
        if (response != null && response.code == "200") {
            DebugLog.i(javaClass.name, "登录响应:" + response.data)
            val fuYouUser = GSON.fromJson(response.data, FuYouUser::class.java)
            LocalConfig.fyToken = fuYouUser.access_token
            LocalConfig.fyUserId=fuYouUser.userId
            return fuYouUser
        } else {
//            DebugLog.e(javaClass.name, "登录失败响应：$response")
            throw NoStackTraceException("登录蜉蝣失败")
        }
    }


    /**
     * 获取读后感
     */
    override fun findReadFeel(scope: CoroutineScope): Coroutine<FyFeel> {
        return Coroutine.async(scope) {
            if (AppConfig.readFeelPage == 0) {
                AppConfig.readFeelPage = 1;
            }
            val request = PageRequest<FyFeel>(
                pageNum = AppConfig.readFeelPage,
                pageSize = 1,
                requestVO = null
            )
            val response = post("/read/readfeel/recommend", GSON.toJson(request))

            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣获取读后感响应", response.data)
                val pageResponse = GSON.fromJson(response.data, PageResponse::class.java)
                if (pageResponse.pages > 0 && pageResponse.pages > AppConfig.readFeelPage) {
                    AppConfig.readFeelPage++
                } else {
                    AppConfig.readFeelPage = 1
                }
                val feelList =
                    GSON.fromJsonArray<FyFeel>(GSON.toJson(pageResponse.list)).getOrNull()
                return@async feelList!![0]
            } else {
                if (response != null) {
                    throw NoStackTraceException("获取读后感失败:" + response.msg)
                } else {
                    throw NoStackTraceException("获取读后感失败:")
                }
            }
        }.timeout(timeOut)
    }

    /**
     * 异步记录初始阅读行为
     */
    override fun sendFirstReadBehave(novel: FyNovel) {
        Coroutine.async {
            post("/behave/behavereader/record-first", GSON.toJson(novel))
        }.timeout(timeOut)
    }

    /**
     * 异步记录阅读行为
     */
    override fun sendReadBehave(readBehave: ReadBehave) {
        Coroutine.async {
            post("/behave/behavereader/record", GSON.toJson(readBehave))
        }.timeout(timeOut)
    }

    /**
     * 采书
     */
    override fun tenderBook(
        scope: CoroutineScope,
        feelBehave: FeelBehave
    ): Coroutine<FyFeel> {
        return Coroutine.async(scope) {
            val response = post("/read/readfeel/tenderBook", GSON.toJson(feelBehave))
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣采书响应", response.data)
                return@async GSON.fromJson(response.data, FyFeel::class.java)
            }
            throw NoStackTraceException("获取读后感失败:" + response!!.msg)
        }.timeout(timeOut)
    }


    /**
     * 发表读后感
     */
    override fun publishFeel(scope: CoroutineScope, readFeel: FyFeel): Coroutine<FyFeel> {
        return Coroutine.async(scope) {
            val response = post("/read/readfeel/publish", GSON.toJson(readFeel))
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣发表读后感响应", response.data)
                return@async GSON.fromJson(response.data, FyFeel::class.java)
            }
            throw NoStackTraceException("获取读后感失败:" + response!!.msg)
        }.timeout(timeOut)
    }


    /**
     * 发表评论
     */
    override fun publishComment(
        scope: CoroutineScope,
        fyComment: FyComment
    ): Coroutine<FyComment> {
        return Coroutine.async(scope) {
            val response = post("/read/readcomment/publish", GSON.toJson(fyComment))
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣发表评论响应", response.data)
                return@async GSON.fromJson(response.data, FyComment::class.java)
            }
            throw NoStackTraceException("发表评论失败:" + response!!.msg)
        }.timeout(timeOut)
    }

    /**
     * 分页查询评论列表
     */
    override fun queryPageComment(
        scope: CoroutineScope,
        feelId: Int,
        pageNum: Int,
        pageSize: Int
    ): Coroutine<PageResponse<FyComment>> {
        return Coroutine.async(scope) {

            val response = post(
                "/read/readcomment/queryPage", GSON.toJson(
                    PageRequest<FyComment>(
                        pageNum, pageSize, FyComment(readfeelId = feelId)
                    )
                )
            )
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣分页查询评论列表响应", response.data)
                val pageResponse = GSON.fromJson(response.data, PageResponse::class.java)
                val fyCommentList =
                    GSON.fromJsonArray<FyComment>(GSON.toJson(pageResponse.list)).getOrNull()
                return@async PageResponse<FyComment>(
                    pageResponse.totalCount,
                    pageResponse.pages,
                    fyCommentList
                )
            }
            throw NoStackTraceException("分页查询评论列表失败:" + response!!.msg)
        }.timeout(timeOut)
    }

    /**
     * 异步记录点赞行为
     *
     * contentType: 1  读后感
     *              2  评论
     *              3  回复
     */
    override fun sendLikeBehave(id: Int, contentType: Int) {
        Coroutine.async {
            var body = HashMap<String, Int>()
            body.put("id", id)
            body.put("type", 3) //3  被点赞
            when (contentType) {
                1 -> post("/behave/countfeel/count", GSON.toJson(body))
                2 -> post("/behave/countcomment/count", GSON.toJson(body))
                3 -> post("/behave/countreply/count", GSON.toJson(body))
                else -> return@async
            }
        }.timeout(timeOut)
    }

    /**
     * 发表回复
     */
    override fun publishReply(
        scope: CoroutineScope,
        fyReply: FyReply
    ): Coroutine<FyReply> {
        return Coroutine.async(scope) {
            val response = post("/read/readreply/publish", GSON.toJson(fyReply))
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣发表回复响应", response.data)
                return@async GSON.fromJson(response.data, FyReply::class.java)
            }
            throw NoStackTraceException("发表回复失败:" + response!!.msg)
        }.timeout(timeOut)
    }


    /**
     * 分页查询回复列表
     */
    override fun queryPageReply(
        scope: CoroutineScope,
        commentId: Int,
        pageNum: Int,
        pageSize: Int
    ): Coroutine<PageResponse<FyReply>> {
        return Coroutine.async(scope) {

            val response = post(
                "/read/readreply/queryPage", GSON.toJson(
                    PageRequest<FyReply>(
                        pageNum, pageSize, FyReply(commentId = commentId)
                    )
                )
            )
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣分页查询回复列表响应", response.data)
                val pageResponse = GSON.fromJson(response.data, PageResponse::class.java)
                val fyCommentList =
                    GSON.fromJsonArray<FyReply>(GSON.toJson(pageResponse.list)).getOrNull()
                return@async PageResponse<FyReply>(
                    pageResponse.totalCount,
                    pageResponse.pages,
                    fyCommentList
                )
            }
            throw NoStackTraceException("分页查询回复列表失败:" + response!!.msg)
        }.timeout(timeOut)
    }

    /**
     * 分页查询找书贴列表
     */
    override fun queryPageFindBook(
        scope: CoroutineScope,
        pageNum: Int,
        pageSize: Int,
        requestVO: FyFindbook?
    ): Coroutine<PageResponse<FyFindbook>> {
        return Coroutine.async(scope) {
            val response = post(
                "/read/readfindbook/queryPage", GSON.toJson(
                    PageRequest<FyFindbook>(
                        pageNum, pageSize, requestVO
                    )
                )
            )
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣分页查询找书贴列表响应", response.data)
                val pageResponse = GSON.fromJson(response.data, PageResponse::class.java)
                val findbookList =
                    GSON.fromJsonArray<FyFindbook>(GSON.toJson(pageResponse.list)).getOrNull()
                return@async PageResponse<FyFindbook>(
                    pageResponse.totalCount,
                    pageResponse.pages,
                    findbookList
                )
            }
            throw NoStackTraceException("分页查询找书贴列表失败:" + response?.msg)
        }.timeout(timeOut)
    }

    /**
     * 发表找书贴
     */
    override fun publishFindBook(
        scope: CoroutineScope,
        findbook: FyFindbook
    ): Coroutine<FyFindbook> {
        return Coroutine.async(scope) {
            val response = post("/read/readfindbook/publish", GSON.toJson(findbook))
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣发表找书贴响应", response.data)
                return@async GSON.fromJson(response.data, FyFindbook::class.java)
            }
            throw NoStackTraceException("发表找书贴失败:" + response?.msg)
        }.timeout(timeOut)
    }

    /**
     * 分页查询读后感列表
     */
    override fun queryPageReadFeel(
        scope: CoroutineScope,
        pageNum: Int,
        pageSize: Int,
        requestVO: FyFeel?
    ): Coroutine<PageResponse<FyFeel>> {
        return Coroutine.async(scope) {
            val response = post(
                "/read/readfeel/pageQuery", GSON.toJson(
                    PageRequest<FyFeel>(
                        pageNum, pageSize, requestVO
                    )
                )
            )
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣分页查询读后感列表响应", response.data)
                val pageResponse = GSON.fromJson(response.data, PageResponse::class.java)
                val feelList =
                    GSON.fromJsonArray<FyFeel>(GSON.toJson(pageResponse.list)).getOrNull()
                return@async PageResponse<FyFeel>(
                    pageResponse.totalCount,
                    pageResponse.pages,
                    feelList
                )
            }
            throw NoStackTraceException("分页查询读后感列表失败:" + response?.msg)
        }.timeout(timeOut)

    }

    /**
     * 获取最佳答案
     */
    override fun findBestAnswer(
        scope: CoroutineScope,
        feelId: Int,
        findId: Int?
    ): Coroutine<FyFeel> {
        return Coroutine.async(scope) {
            val response = post("/read/readfeel/pageQuery", GSON.toJson(PageRequest<FyFeel>(
                1, 1, FyFeel(findId = findId, type = 1, id = feelId)
            )))
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣获取最佳答案响应", response.data)
                val pageResponse = GSON.fromJson(response.data, PageResponse::class.java)
                val feelList =
                    GSON.fromJsonArray<FyFeel>(GSON.toJson(pageResponse.list)).getOrNull()
                if (feelList != null) {
                    if (feelList.isNotEmpty()){
                        return@async feelList[0]
                    }
                }
            }
            throw NoStackTraceException("获取获取最佳答案失败:" + response?.msg)
        }.timeout(timeOut)
    }

    /**
     * 设置为最佳答案
     */
    override fun setBestAnswer(scope: CoroutineScope, findbook: FyFindbook): Coroutine<Boolean> {
        return Coroutine.async(scope) {
            val response = post("/read/readfindbook/bastFeel", GSON.toJson(findbook))
            if (response != null && response.code == "200") {
                return@async true
            }
            throw NoStackTraceException("设置最佳答案失败:" + response?.msg)
        }.timeout(timeOut)
    }

    /**
     * 获取消息数量
     * type 1；被阅读
     *      3：被点赞
     *      5：被采纳
     */
    override fun getMessageNum(scope: CoroutineScope, lastMessageTime: String, type:Int): Coroutine<Int> {
        return Coroutine.async(scope) {
            val response = post("/behave/message/pageNum", GSON.toJson(FyMessage(createTime = lastMessageTime, type = type)))
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣获取消息数量响应", "类型：$type 数量:"+response.data)
                return@async response.data.toInt()
            }
            throw NoStackTraceException("获取消息数量失败:" + response!!.msg)
        }.timeout(timeOut)
    }

    /**
     * 获取内容消息
     */
    override fun queryPageMessage(
        scope: CoroutineScope,
        pageNum: Int,
        pageSize: Int,
        requestVO: FyMessage?
    ): Coroutine<PageResponse<FyMessage>> {
        return Coroutine.async(scope) {
            val response = post(
                "/behave/message/pageContent", GSON.toJson(
                    PageRequest<FyMessage>(
                        pageNum, pageSize, requestVO
                    )
                )
            )
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣分页获取内容消息响应", response.data)
                val pageResponse = GSON.fromJson(response.data, PageResponse::class.java)
                val messageList =
                    GSON.fromJsonArray<FyMessage>(GSON.toJson(pageResponse.list)).getOrNull()
                return@async PageResponse<FyMessage>(
                    pageResponse.totalCount,
                    pageResponse.pages,
                    messageList
                )
            }
            throw NoStackTraceException("分页获取内容消息失败:" + response?.msg)
        }.timeout(timeOut)
    }

    override fun queryPageMessageTender(
        scope: CoroutineScope,
        pageNum: Int,
        pageSize: Int
    ): Coroutine<PageResponse<FyMessageFeel>> {
        return Coroutine.async(scope) {
            val response = post(
                "/behave/message/pageTender", GSON.toJson(
                    PageRequest<FyMessage>(pageNum, pageSize,null)
                )
            )
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣分页获取采书消息响应", response.data)
                val pageResponse = GSON.fromJson(response.data, PageResponse::class.java)
                val messageList =
                    GSON.fromJsonArray<FyMessageFeel>(GSON.toJson(pageResponse.list)).getOrNull()
                return@async PageResponse<FyMessageFeel>(
                    pageResponse.totalCount,
                    pageResponse.pages,
                    messageList
                )
            }
            throw NoStackTraceException("分页获取采书消息失败:" + response?.msg)
        }.timeout(timeOut)
    }

    override fun queryPageMessageRead(
        scope: CoroutineScope,
        pageNum: Int,
        pageSize: Int
    ): Coroutine<PageResponse<FyMessageFeel>> {
        return Coroutine.async(scope) {
            val response = post(
                "/behave/message/pageRead", GSON.toJson(
                    PageRequest<FyMessage>(pageNum, pageSize,null)
                )
            )
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣分页获取阅读消息响应", response.data)
                val pageResponse = GSON.fromJson(response.data, PageResponse::class.java)
                val messageList =
                    GSON.fromJsonArray<FyMessageFeel>(GSON.toJson(pageResponse.list)).getOrNull()
                return@async PageResponse<FyMessageFeel>(
                    pageResponse.totalCount,
                    pageResponse.pages,
                    messageList
                )
            }
            throw NoStackTraceException("分页获取阅读消息失败:" + response?.msg)
        }.timeout(timeOut)
    }

    override fun queryPageMessageLike(
        scope: CoroutineScope,
        pageNum: Int,
        pageSize: Int
    ): Coroutine<PageResponse<FyMessageComment>> {
        return Coroutine.async(scope) {
            val response = post(
                "/behave/message/pageLike", GSON.toJson(
                    PageRequest<FyMessage>(pageNum, pageSize,null)
                )
            )
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣分页获取点赞消息响应", response.data)
                val pageResponse = GSON.fromJson(response.data, PageResponse::class.java)
                val messageList =
                    GSON.fromJsonArray<FyMessageComment>(GSON.toJson(pageResponse.list)).getOrNull()
                return@async PageResponse<FyMessageComment>(
                    pageResponse.totalCount,
                    pageResponse.pages,
                    messageList
                )
            }
            throw NoStackTraceException("分页获取点赞消息失败:" + response?.msg)
        }.timeout(timeOut)
    }
}