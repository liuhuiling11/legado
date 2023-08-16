package io.legado.app.ui.comment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.data.entities.fuyou.FyComment
import io.legado.app.data.entities.fuyou.FyMessage
import io.legado.app.data.entities.fuyou.FyReply
import io.legado.app.data.entities.fuyou.MessageType
import io.legado.app.databinding.DialogReplyViewBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.lib.theme.primaryColor
import io.legado.app.utils.StringUtils
import io.legado.app.utils.applyTint
import io.legado.app.utils.hideSoftInput
import io.legado.app.utils.setLayout
import io.legado.app.utils.toastOnUi
import io.legado.app.utils.viewbindingdelegate.viewBinding
import kotlinx.coroutines.Dispatchers
import splitties.init.appCtx


class ReplyFragment() : BaseDialogFragment(R.layout.dialog_reply_view){
    private val binding by viewBinding(DialogReplyViewBinding::bind)
    private var readFeelId: Int? = null
    private var commentId: Int? = null
    private var fatherId: Int? = null
    private var curTimeCount: Int? = null
    private var  heUserId : String?=null
    private var replyType:Int=2 //2 发送评论，3，发送回复

    constructor(
        fyMessage: FyMessage,
        timeCount: Int,
    ) : this() {
        initData(fyMessage,timeCount)
        isCancelable = false
    }

    fun initData(
        fyMessage: FyMessage,
        timeCount: Int,
    ){
        when(fyMessage.type){
            MessageType.ANSWER.code -> {
                //找书贴被回答
                readFeelId=fyMessage.contentId
                replyType=2
            }
            MessageType.COMMENT.code -> {
                //读后感被评论
                readFeelId=fyMessage.myId
                commentId=fyMessage.contentId
                replyType=3
            }
            MessageType.REPLY.code -> {
                //评论被回复
                commentId=fyMessage.myId
                fatherId=fyMessage.contentId
                replyType=3
            }
            MessageType.AGAIN_REPLY.code -> {
                //回复被追复
                commentId=fyMessage.commentId
                fatherId=fyMessage.contentId
                replyType=3
            }
        }
        heUserId=fyMessage.userId
        curTimeCount=timeCount
    }

    override fun onStart() {
        super.onStart()
        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 0.7f)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolBar.setBackgroundColor(primaryColor)
        binding.toolBar.inflateMenu(R.menu.dialog_text)
        binding.toolBar.menu.applyTint(requireContext())
        binding.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_close -> dismiss()
            }
            true
        }
        binding.toolBar.title = "评论"
        setHintHeUserId()

        //3,发送评论
        binding.sendComment.setOnClickListener {
            binding.tieMyComment.clearFocus()
            binding.tieMyComment.hideSoftInput()
            sendReplyOrComment(curTimeCount)
        }
    }

    fun setHintHeUserId() {
        binding.tilCommentJj.hint = "回复 " + StringUtils.getUserName(heUserId ?: "写评论")+":"
    }

    private fun sendReplyOrComment(timeCount: Int?) {
        if (binding.tieMyComment.text!!.toString() != "") {
            if (replyType==2){
                //发评论
                sendComment(timeCount)
            }else {
                //发回复
                sendReply(timeCount)
            }
        }
    }

    private fun sendComment(timeCount: Int?) {
        Coroutine.async(this, Dispatchers.IO) {
            FuYouHelp.fuYouHelpPost?.run {
                publishComment(
                    lifecycleScope, FyComment(
                        readfeelId = readFeelId,
                        content = binding.tieMyComment.text!!.toString(),
                        timeCount = timeCount
                    )
                ).onSuccess {
                    binding.tieMyComment.text!!.clear()
                    appCtx.toastOnUi("评论发送成功")
                    dismiss()
                }.onError {
                    appCtx.toastOnUi("评论发送失败" + it.localizedMessage)
                }
            }
        }
    }

    private fun sendReply(timeCount: Int?) {
        Coroutine.async(this, Dispatchers.IO) {
            FuYouHelp.fuYouHelpPost?.run {
                publishReply(
                    lifecycleScope, FyReply(
                        fatherId=fatherId,
                        content = binding.tieMyComment.text!!.toString(),
                        timeCount = timeCount
                    )
                ).onSuccess {
                    binding.tieMyComment.text!!.clear()
                    appCtx.toastOnUi("回复成功")
                    dismiss()
                }.onError {
                    appCtx.toastOnUi("回复失败" + it.localizedMessage)
                }
            }
        }
    }
}


