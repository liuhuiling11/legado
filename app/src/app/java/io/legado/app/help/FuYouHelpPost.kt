package io.legado.app.help

import androidx.annotation.Keep
import io.legado.app.constant.AppConst
import io.legado.app.data.entities.fuyou.FeelBehave
import io.legado.app.data.entities.fuyou.FuYouUser
import io.legado.app.data.entities.fuyou.FyComment
import io.legado.app.data.entities.fuyou.FyNovel
import io.legado.app.data.entities.fuyou.FyResponse
import io.legado.app.data.entities.fuyou.ReadBehave
import io.legado.app.data.entities.fuyou.ReadFeel
import io.legado.app.exception.NoStackTraceException
import io.legado.app.help.config.LocalConfig
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.help.http.addHeaders
import io.legado.app.help.http.getProxyClient
import io.legado.app.help.http.newCallStrResponse
import io.legado.app.help.http.postJson
import io.legado.app.utils.DebugLog
import io.legado.app.utils.GSON
import kotlinx.coroutines.CoroutineScope

@Keep
@Suppress
object FuYouHelpPost : FuYouHelp.FuYouHelpInterface {

    private const val baseUrl = "ws:www.liuhuiling.cn/fuyouapi"
    private const val timeOut = 20000L

    private suspend fun post(url: String, bodyMap: String): FyResponse? {
        if (LocalConfig.fyToken==null||LocalConfig.fyToken==""){
            LoginfuYou(FuYouUser(AppConst.androidId, LocalConfig.password ?: "123456","",""))
            return null
        }else{
            val headerMap = HashMap<String, String>()
            headerMap["Authorization"] = "Bearer " + LocalConfig.fyToken
            return post(url, headerMap, bodyMap)
        }

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
            DebugLog.i("post蜉蝣", "$url 响应：$resR")
            if (resR.isSuccessful() && resR.code() == 200) {
                var fyResponse = GSON.fromJson(resR.body, FyResponse::class.java)
                if (fyResponse.code == "401") {
                    //未登录或登录失效
                    //重新登录
                    val loginfuYou =
                        LoginfuYou(FuYouUser(AppConst.androidId, LocalConfig.password ?: "123456","",""))

                    //再次发送
                    if (loginfuYou != null) {
                        header["Authorization"] = "Bearer " + LocalConfig.fyToken.toString()
                        val newResR = getProxyClient(null).newCallStrResponse() {
                            addHeaders(header)
                            url(baseUrl + url)
                            postJson(bodyJson)
                        }
                        if (newResR.isSuccessful() && newResR.code() == 200) {
                            GSON.fromJson(resR.body, FyResponse::class.java)
                        }
                    }
                }
                return fyResponse
            } else {
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw NoStackTraceException("远程post请求蜉蝣失败")
        }
    }

    /**
     * 登录或注册
     */
    override fun login(scope: CoroutineScope, user: FuYouUser): Coroutine<FuYouUser> {
        return Coroutine.async(scope) {
            return@async LoginfuYou(user)
        }.timeout(timeOut)
    }

    private suspend fun LoginfuYou(user: FuYouUser): FuYouUser {
        val response = post("/auth/read-login", null, GSON.toJson(user))
        if (response != null && response.code == "200") {
            DebugLog.i(javaClass.name, "登录响应:" + response.data)
            val fuYouUser = GSON.fromJson(response.data, FuYouUser::class.java)
            LocalConfig.fyToken = fuYouUser.access_token
            return fuYouUser
        } else {
            DebugLog.e(javaClass.name, "登录失败响应：$response")
            throw NoStackTraceException("登录蜉蝣失败")
        }
    }


    /**
     * 获取读后感
     */
    override fun findReadFeel(scope: CoroutineScope): Coroutine<ReadFeel> {
        return Coroutine.async(scope) {
            val bodyMap = HashMap<String, String>()
            val response = post("/read/readfeel/recommend", GSON.toJson(bodyMap))
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣获取读后感响应", response.data)
                return@async GSON.fromJson(response.data, ReadFeel::class.java)
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
    override fun sendFirstReadBehave(scope: CoroutineScope, novel: FyNovel) {
        Coroutine.async(scope) {
            val responseBody = post("/behave/behavereader/record-first", GSON.toJson(novel))
        }.timeout(timeOut)
    }

    /**
     * 异步记录阅读行为
     */
    override fun sendReadBehave(scope: CoroutineScope, readBehave: ReadBehave) {
        Coroutine.async(scope) {
            val responseBody = post("/behave/behavereader/record", GSON.toJson(readBehave))
        }.timeout(timeOut)
    }

    /**
     * 采书
     */
    override fun tenderBook(
        scope: CoroutineScope,
        feelBehave: FeelBehave
    ): Coroutine<ReadFeel> {
        return Coroutine.async(scope) {
            val response = post("/read/readfeel/tenderBook", GSON.toJson(feelBehave))
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣采书响应", response.data)
                return@async GSON.fromJson(response.data, ReadFeel::class.java)
            }
            throw NoStackTraceException("获取读后感失败:" + response!!.msg)
        }.timeout(timeOut)
    }


    /**
     * 发表读后感
     */
    override fun publishFeel(scope: CoroutineScope, readFeel: ReadFeel): Coroutine<ReadFeel> {
        return Coroutine.async(scope) {
            val response = post("/read/readfeel/publish", GSON.toJson(readFeel))
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣发表读后感响应", response.data)
                return@async GSON.fromJson(response.data, ReadFeel::class.java)
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
            throw NoStackTraceException("获取读后感失败:" + response!!.msg)
        }.timeout(timeOut)
    }
}