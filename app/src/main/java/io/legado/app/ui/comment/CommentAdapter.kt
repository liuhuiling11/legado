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

        val bundle = payloads.getOrNull(0) as? String
        if (bundle == null) {
            bind(binding, item)
        } else {
            bindChange(binding, item, bundle)
        }

    }

    private fun bindChange(binding: ItemCommentListBinding, item: FyComment, bundle: String) {
        binding.run {
            when (bundle) {
                "isInCommentList" -> {
                    tvLike.text = item.like.toString()
                    tvReplay.text = item.replay.toString()
                }
            }

        }
    }

    private fun bind(
        binding: ItemCommentListBinding,
        item: FyComment
    ) {
        binding.tvComment.text = item.content
        binding.tvCommentTime.text =
            StringUtils.dateConvert(item.createTime)
        item.userId!!.let {
            if (it.length > 5) {
                binding.tvUserName.text = "采友${it.substring(0, 7)}"
            } else {
                binding.tvUserName.text = "采友${it}"
            }
        }
        binding.tvLike.text = item.like.toString()
        binding.tvReplay.text = item.replay.toString()
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemCommentListBinding) {
        holder.itemView.setOnClickListener {
            getItem(holder.layoutPosition)?.let {

            }
        }
    }


    interface Callback {
    }
}