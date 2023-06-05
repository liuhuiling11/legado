package io.legado.app.help

import io.legado.app.help.coroutine.Coroutine
import kotlinx.coroutines.CoroutineScope

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
     * 蜉蝣请求响应类
     */
    data class FyResponse(
        val msg: String,
        val code: String,
        val data: String
    )

    /**
     * 用户类
     */
    data class FuYouUser(
        val username: String,
        val password: String,
        val token: String?,
        val access_token: String

    ) {
        constructor(username: String, password: String) : this(username,password,"","")
    }

    /**
     * 读后感类
     */
    data class ReadFeel(
        val id: Int,
        val userId: String?,
        val novelName: String?,
        val novelUrl: String?,
        val novelAuthor: String?,
        val novelPhoto: String?,
        val content: String?,
        val labels: String?,
        val updateTime: String?,
        val sourceJson: String,
        var tocUrl: String,
        val intro: String?
    ) {

    }

    /**
     * 阅读行为类
     */
    data class ReadBehave(
        val novelId: Number,
        val type: String,
        val timeCount: Number,
        val originType: String,
    )

    /**
     * 读后感行为类
     */
    data class FeelBehave(
        val feelId: Int,
        val type: String,
        val timeCount: Int,
    )

    /**
     * 小说类
     */
    data class FyNovel(
        val novelName: String,
        val novelAuthor: String,
        val novelIntroduction: String?,
        val novelUrl: String,
        val novelPhoto: String?,
        val labels: String?,
        val originType:Number?
    )

    interface FuYouHelpInterface {

        fun login(scope: CoroutineScope,user:FuYouUser): Coroutine<FuYouUser>
        fun findReadFeel(scope: CoroutineScope): Coroutine<ReadFeel>
        fun sendFirstReadBehave(scope: CoroutineScope, novel: FyNovel)
        fun sendReadBehave(scope: CoroutineScope, readBehave: ReadBehave)
        fun tenderBook(scope: CoroutineScope, feelBehave: FeelBehave): Coroutine<ReadFeel>
    }



}