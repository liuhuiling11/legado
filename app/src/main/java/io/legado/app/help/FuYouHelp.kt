package io.legado.app.help

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
import io.legado.app.data.entities.fuyou.PageResponse
import io.legado.app.data.entities.fuyou.ReadBehave
import io.legado.app.help.coroutine.Coroutine
import kotlinx.coroutines.CoroutineScope
import java.util.Date

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

    interface FuYouHelpInterface {

        suspend fun login(user: FuYouUser): FuYouUser
        fun findReadFeel(scope: CoroutineScope): Coroutine<FyFeel>
        fun sendFirstReadBehave( novel: FyNovel)
        fun sendReadBehave(readBehave: ReadBehave)
        fun tenderBook(scope: CoroutineScope, feelBehave: FeelBehave): Coroutine<FyFeel>

        fun publishFeel(scope: CoroutineScope, readFeel: FyFeel): Coroutine<FyFeel>
        fun publishComment(scope: CoroutineScope, fyComment: FyComment): Coroutine<FyComment>

        fun queryPageComment(scope: CoroutineScope, feelId:Int,pageNum:Int,pageSize:Int) : Coroutine<PageResponse<FyComment>>
        fun sendLikeBehave(id: Int, contentType: Int)
        fun publishReply(scope: CoroutineScope, fyReply: FyReply): Coroutine<FyReply>
        fun queryPageReply(
            scope: CoroutineScope,
            commentId: Int,
            pageNum: Int,
            pageSize: Int
        ): Coroutine<PageResponse<FyReply>>

        fun queryPageFindBook(
            scope: CoroutineScope,
            pageNum: Int,
            pageSize: Int,
            requestVO:FyFindbook?
        ): Coroutine<PageResponse<FyFindbook>>

        fun publishFindBook(scope: CoroutineScope, findbook: FyFindbook): Coroutine<FyFindbook>

        fun queryPageReadFeel(
            scope: CoroutineScope,
            pageNum: Int,
            pageSize: Int,
            requestVO:FyFeel?
        ): Coroutine<PageResponse<FyFeel>>

        fun findBestAnswer(scope: CoroutineScope, feelId: Int, findId: Int?): Coroutine<FyFeel>

        fun setBestAnswer(scope: CoroutineScope,findbook: FyFindbook):Coroutine<Boolean>

        fun getTenderNum(scope: CoroutineScope,preTime:Date):Coroutine<Int>
        fun getReadNum(scope: CoroutineScope,preTime:Date):Coroutine<Int>
        fun getLoveNum(scope: CoroutineScope,preTime:Date):Coroutine<Int>

        fun queryPageMessage(
            scope: CoroutineScope,
            pageNum: Int,
            pageSize: Int,
            requestVO:FyMessage?
        ): Coroutine<PageResponse<FyMessage>>

        fun queryPageMessageTender(
            scope: CoroutineScope,
            pageNum: Int,
            pageSize: Int,
        ): Coroutine<PageResponse<FyMessageFeel>>

        fun queryPageMessageRead(
            scope: CoroutineScope,
            pageNum: Int,
            pageSize: Int,
        ): Coroutine<PageResponse<FyMessageFeel>>

        fun queryPageMessageLike(
            scope: CoroutineScope,
            pageNum: Int,
            pageSize: Int,
        ): Coroutine<PageResponse<FyMessageComment>>
    }



}