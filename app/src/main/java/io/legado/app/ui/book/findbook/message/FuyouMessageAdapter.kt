package io.legado.app.ui.book.findbook.message

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.fuyou.FyMessage
import io.legado.app.databinding.ItemMessageCommentBinding
import io.legado.app.utils.StringUtils


class FuyouMessageAdapter(context: Context, val callBack: CallBack) :
    RecyclerAdapter<FyMessage, ItemMessageCommentBinding>(context) {

    override fun getViewBinding(parent: ViewGroup): ItemMessageCommentBinding {
        return ItemMessageCommentBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemMessageCommentBinding,
        item: FyMessage,
        payloads: MutableList<Any>
    ) {
        val bundle = payloads.getOrNull(0) as? Bundle
        if (bundle == null) {
            bind(binding, item)
        } else {
            bindChange(binding, item, bundle)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun bind(binding: ItemMessageCommentBinding, item: FyMessage) {
        binding.run {
            tvUserName.text = StringUtils.getUserName(item.userId!!)
            tvCreateTime.text = StringUtils.dateConvert(item.createTime)
            tvContent.text = item.content
        }
    }

    private fun bindChange(binding: ItemMessageCommentBinding, item: FyMessage, bundle: Bundle) {
        binding.run {
        }
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemMessageCommentBinding) {
        //1，查看详情
        holder.itemView.setOnClickListener {
            getItem(holder.layoutPosition)?.let {
                //开启详情
                showContent(it)
            }
        }

        //2，直接点赞
        binding.tvLike.setOnClickListener {
            getItem(holder.layoutPosition)?.let {
                like(it)
            }
        }

        //3.直接回复
        binding.tvReply.setOnClickListener{
            getItem(holder.layoutPosition)?.let {
                reply(it)
            }
        }
    }

    private fun reply(message: FyMessage) {
        TODO("Not yet implemented")
    }

    private fun like(message: FyMessage) {
        TODO("Not yet implemented")
    }

    private fun showContent(message: FyMessage) {
        TODO("Not yet implemented")
    }

    interface CallBack {


    }
}