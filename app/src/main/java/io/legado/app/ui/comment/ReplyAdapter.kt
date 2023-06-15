package io.legado.app.ui.comment

import android.content.Context
import android.view.ViewGroup
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.fuyou.FyReply
import io.legado.app.databinding.ItemReplyListBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.utils.StringUtils


class ReplyAdapter(context: Context, val callback: ReplyAdapter.Callback) :
    RecyclerAdapter<FyReply, ItemReplyListBinding>(context) {

    override fun getViewBinding(parent: ViewGroup): ItemReplyListBinding {
        return ItemReplyListBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemReplyListBinding,
        item: FyReply,
        payloads: MutableList<Any>
    ) {

        val bundle = payloads.getOrNull(0) as? String
        if (bundle == null) {
            bind(binding, item)
        }
    }

    private fun bind(
        binding: ItemReplyListBinding,
        item: FyReply
    ) {
        binding.tvComment.text = item.content
        binding.tvCommentTime.text = StringUtils.dateConvert(item.createTime)
        binding.tvUserName.text= StringUtils.getUserName(item.userId!!)
        binding.tvLike.text = item.like.toString()

    }



    override fun registerListener(holder: ItemViewHolder, binding: ItemReplyListBinding) {
        binding.run {
            tvLike.setOnClickListener{//点赞
                getItem(holder.layoutPosition)?.let {
                    FuYouHelp.fuYouHelpPost?.run {
                        sendLikeBehave(it.id!!,2)
                    }
                }
            }
            tvComment.setOnClickListener{//回复
                getItem(holder.layoutPosition)?.let {
                    callback.replyFather(FyReply(userId = it.userId, fatherId = it.id, commentId = it.commentId),this@ReplyAdapter)
                }
            }
        }
    }


    interface Callback {
        fun replyFather(fyReply: FyReply, replyAdapter: ReplyAdapter)
    }
}