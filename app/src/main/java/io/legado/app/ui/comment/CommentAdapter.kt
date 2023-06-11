package io.legado.app.ui.comment

import android.content.Context
import android.view.ViewGroup
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.fuyou.FyComment
import io.legado.app.databinding.ItemCommentListBinding
import io.legado.app.utils.StringUtils


class CommentAdapter(context: Context, val callback: CommentAdapter.Callback) :
    RecyclerAdapter<FyComment, ItemCommentListBinding>(context) {


    override fun getViewBinding(parent: ViewGroup): ItemCommentListBinding {
        return ItemCommentListBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemCommentListBinding,
        item: FyComment,
        payloads: MutableList<Any>
    ) {
        binding.tvComment.text = item.content
        binding.tvCommentTime.text =
            StringUtils.dateConvert(item.createTime.toString(), "yyyyMMdd HH:mm:ss")
        binding.tvUserName.text = item.userId
        binding.tvLike.text = item.like.toString()
        binding.tvReplay.text = item.replay.toString()

    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemCommentListBinding) {

    }



    interface Callback {
    }
}