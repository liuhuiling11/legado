package io.legado.app.help

import androidx.annotation.Keep
import io.legado.app.constant.AppLog
import io.legado.app.exception.NoStackTraceException
import io.legado.app.help.FuYouHelp.FuYouUser
import io.legado.app.help.FuYouHelp.ReadFeel
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

    private const val baseUrl = "ws:10.0.2.2:8080"

    private suspend fun post(url: String, bodyMap: String): FuYouHelp.FyResponse? {
        val headerMap = HashMap<String, String>()
        headerMap["Authorization"] = "Bearer " + LocalConfig.fyToken.toString()
        return post(url, headerMap, bodyMap)
    }


    /**
     * 发送请求
     */
    private suspend fun post(
        url: String,
        headerMap: HashMap<String, String>?,
        bodyJson: String
    ): FuYouHelp.FyResponse? {
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
                return GSON.fromJson(resR.body, FuYouHelp.FyResponse::class.java)
            }
            return null
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
            val response = post("/auth/read-login", null, GSON.toJson(user))
            if (response != null && response.code =="200") {
                DebugLog.i(javaClass.name, "登录响应:" + response.data)
                return@async GSON.fromJson(response.data, FuYouUser::class.java)
            } else {
                DebugLog.e(javaClass.name, "登录失败响应：$response")
                throw NoStackTraceException("登录蜉蝣失败")
            }
        }.timeout(5000)
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
        }.timeout(5000)
    }

    /**
     * 异步记录初始阅读行为
     */
    override fun sendFirstReadBehave(scope: CoroutineScope, novel: FuYouHelp.FyNovel) {
        Coroutine.async(scope) {
            val responseBody = post("/behave/behavereader/record-first", GSON.toJson(novel))
        }.timeout(3000)
    }

    /**
     * 异步记录阅读行为
     */
    override fun sendReadBehave(scope: CoroutineScope, readBehave: FuYouHelp.ReadBehave) {
        Coroutine.async(scope) {
            val responseBody = post("/behave/behavereader/record", GSON.toJson(readBehave))
        }.timeout(3000)
    }

    /**
     * 采书
     */
    override fun tenderBook(scope: CoroutineScope, feelBehave: FuYouHelp.FeelBehave): Coroutine<ReadFeel> {
        return Coroutine.async(scope) {
            val response = post("/read/readfeel/tenderBook", GSON.toJson(feelBehave))
            if (response != null && response.code == "200") {
                DebugLog.i("蜉蝣采书响应", response.data)
                return@async GSON.fromJson(response.data, ReadFeel::class.java)
            }
            throw NoStackTraceException("获取读后感失败:" + response!!.msg)
        }.timeout(3000)
    }


}