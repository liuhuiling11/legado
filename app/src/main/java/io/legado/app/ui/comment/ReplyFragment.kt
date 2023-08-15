package io.legado.app.ui.comment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.data.entities.fuyou.FyComment
import io.legado.app.data.entities.fuyou.FyReply
import io.legado.app.databinding.DialogReplyViewBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.lib.theme.primaryColor
import io.legado.app.utils.DebugLog
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
    private var pages: Int = 1
    private var replyType:Int=2

    constructor(
        myId: Int,
        contentId: Int,
        timeCount: Int,
        type: Int
    ) : this() {
        arguments = Bundle().apply {
            putInt("myId", myId)
            putInt("contentId", contentId)
            putInt("timeCount", timeCount)
            putInt("type", type)

        }
        isCancelable = false
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

        val timeCount = arguments?.getInt("timeCount")
        readFeelId = requireArguments().getInt("readFeelId")


        //3,发送评论
        binding.sendComment.setOnClickListener {
            binding.tieMyComment.clearFocus()
            binding.tieMyComment.hideSoftInput()
            sendReplyOrComment(timeCount)
        }
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
                    DebugLog.i(javaClass.name, "评论发布成功！id：${it.id}")
                    binding.tieMyComment.text!!.clear()
                    appCtx.toastOnUi("评论发送成功")
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
                        commentId = commentId,
                        fatherId=fatherId,
                        content = binding.tieMyComment.text!!.toString(),
                        timeCount = timeCount
                    )
                ).onSuccess {
                    DebugLog.i(javaClass.name, "回复成功！id：${it.id}")
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


