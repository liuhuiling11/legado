package io.legado.app.ui.book.findbook.message.tender

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.fuyou.FyMessageFeel
import io.legado.app.databinding.ItemMessageTenderBinding
import io.legado.app.utils.StringUtils


class FuyouMessageTenderAdapter(context: Context, val callBack: CallBack) :
    RecyclerAdapter<FyMessageFeel, ItemMessageTenderBinding>(context) {

    override fun getViewBinding(parent: ViewGroup): ItemMessageTenderBinding {
        return ItemMessageTenderBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemMessageTenderBinding,
        item: FyMessageFeel,
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
    private fun bind(binding: ItemMessageTenderBinding, item: FyMessageFeel) {
        binding.run {
            tvUserName.text = StringUtils.getUserName(item.userId!!)
            tvCreateTime.text = StringUtils.dateConvert(item.createTime)
            novelPhoto.load(item.novelPhoto, "", "")
            binding.novelName.text = item.novelName
            binding.novelAuth.text = item.novelAuthor
            tvContent.text = item.content
            tvCommentNum.text= item.numComment.toString() +" 评"
            tvTenderNum.text= item.numTender.toString() +" 采"
        }
    }

    private fun bindChange(binding: ItemMessageTenderBinding, item: FyMessageFeel, bundle: Bundle) {
        binding.run {
        }
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemMessageTenderBinding) {
        //1，查看详情
        holder.itemView.setOnClickListener {
            getItem(holder.layoutPosition)?.let {
                //开启详情
                showContent(it)
            }
        }
        //2.展示书籍详情
        binding.novelUrl.setOnClickListener{
            getItem(holder.layoutPosition)?.let {
                callBack.startNovel(binding.novelName.text.toString(),binding.novelAuth.text.toString(),it.novelUrl!!)
            }
        }

        //3.展示评论
        binding.tvCommentNum.setOnClickListener{
            getItem(holder.layoutPosition)?.let {
                showComment(it)
            }
        }
    }

    private fun showComment(message: FyMessageFeel) {
        TODO("Not yet implemented")
    }

    private fun showContent(message: FyMessageFeel) {
        TODO("Not yet implemented")
    }

    interface CallBack {
        fun startNovel(novelName: String, novelAuth: String, novelUrl: String)

    }
}