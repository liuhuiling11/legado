package io.legado.app.ui.book.findbook.message.like

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.fuyou.FyMessageComment
import io.legado.app.databinding.ItemMessageLikeBinding
import io.legado.app.utils.StringUtils


class FuyouMessageLikeAdapter(context: Context, val callBack: CallBack) :
    RecyclerAdapter<FyMessageComment, ItemMessageLikeBinding>(context) {

    override fun getViewBinding(parent: ViewGroup): ItemMessageLikeBinding {
        return ItemMessageLikeBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemMessageLikeBinding,
        item: FyMessageComment,
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
    private fun bind(binding: ItemMessageLikeBinding, item: FyMessageComment) {
        binding.run {
            tvCreateTime.text = StringUtils.dateConvert(item.updateTime)
            tvContent.text = item.content
        }
    }

    private fun bindChange(binding: ItemMessageLikeBinding, item: FyMessageComment, bundle: Bundle) {
        binding.run {
        }
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemMessageLikeBinding) {
        //1，查看详情
        holder.itemView.setOnClickListener {
            getItem(holder.layoutPosition)?.let {
                //开启详情
                showContent(it)
            }
        }

    }

    private fun showContent(message: FyMessageComment) {
        TODO("Not yet implemented")
    }

    interface CallBack {


    }
}