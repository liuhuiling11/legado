package io.legado.app.help

import com.script.rhino.RhinoScriptEngine.eval
import io.legado.app.help.config.LocalConfig
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.help.http.addHeaders
import io.legado.app.help.http.getProxyClient
import io.legado.app.help.http.newCallStrResponse
import io.legado.app.help.http.postJson
import io.legado.app.utils.DebugLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject

/**
 * 蜉蝣后台服务
 */
object FuYouHelp {


    val fuYouHelpPost by lazy {
        kotlin.runCatching {
            Class.forName("io.legado.app.help.FuYouHelpPost")
                .kotlin.objectInstance as FuYouHelpInterface
        }.getOrNull()
    }

    /**
     * 用户类
     */
    data class FuYouUser(
        val username: String,
        val password: String,
        val token: String?,
        val userId: String?
    ) {
        constructor(username: String, password: String) : this(username,password,"","")
    }

    /**
     * 读后感类
     */
    data class ReadFeel(
        val id: Number?,
        val userId: String?,
        val userName: String?,
        val novelUrl: String?,
        val content: String?,
        val labels: String?,
        val updateTime: String?
    )

    /**
     * 行为类
     */
    data class Behave(
        val userId: String,
        val type: String,
        val timeCount: Number,
        val source: String,
        val bookName: String?,
        val bookAuthor: String?,
    )

    interface FuYouHelpInterface {

        fun login(scope: CoroutineScope,user:FuYouUser): Coroutine<FuYouUser>
        fun findReadFeel(scope: CoroutineScope): Coroutine<ReadFeel>
        fun sendBehave(scope: CoroutineScope,behave: Behave)
    }


    /**
     * 发送行为
     */


    /**
     * 采书
     */



}