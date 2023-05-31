package io.legado.app.help

import com.jayway.jsonpath.DocumentContext
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
import io.legado.app.utils.jsonPath
import io.legado.app.utils.readLong
import io.legado.app.utils.readString
import kotlinx.coroutines.CoroutineScope

class FuYouHelpPost: FuYouHelp.FuYouHelpInterface {

    private val baseUrl = "http://127.0.0.1:8088"

    private suspend fun post(url:String,bodyMap:String): String? {
        val tokenMap = HashMap<String, String>()
        tokenMap["token"]= LocalConfig.fyToken.toString()
        return post(url, tokenMap, bodyMap)
    }


    /**
     * 发送请求
     */
    private suspend fun post(url:String, headerMap:HashMap<String, String>?, bodyJson: String): String? {
        val header = HashMap<String, String>()
        if (headerMap!=null){
            header.putAll(headerMap)
        }
        val resR = getProxyClient(null).newCallStrResponse() {
            addHeaders(header)
            url(baseUrl+url)
            postJson(bodyJson)
        }
        DebugLog.w("异步发送请求", resR.toString())
        if (resR.isSuccessful() && resR.code() == 200) {
            return resR.body
        }
        return null
    }

    /**
     * 登录或注册
     */
    override fun login(scope: CoroutineScope, user: FuYouUser): Coroutine<FuYouUser> {
        return Coroutine.async(scope) {

            val responseBody = post("/fy-api/login", null, GSON.toJson(user))
            if (responseBody != null) {
                return@async GSON.fromJson(responseBody,FuYouUser::class.java)
            } else {
                throw NoStackTraceException("登录蜉蝣失败")
            }
        }.timeout(1000)
    }



    /**
     * 获取读后感
     */
    override fun findReadFeel(scope: CoroutineScope): Coroutine<ReadFeel> {
        return Coroutine.async(scope) {
            val bodyMap = HashMap<String, String>()
            bodyMap["pagesize"] = "1"
            bodyMap["pagenum"] = "1"
            val responseBody = post("/findReadFeel", GSON.toJson(bodyMap))
            return@async GSON.fromJson(responseBody,ReadFeel::class.java)
        }.timeout(1000)
    }

    /**
     * 异步记录行为
     */
    override fun sendBehave(scope: CoroutineScope,behave: FuYouHelp.Behave){
        Coroutine.async(scope) {
            val responseBody = post("/findReadFeel", GSON.toJson(behave))
            AppLog.put("异步行为记录：$responseBody")
        }.timeout(1000)
    }
}