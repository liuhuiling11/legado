package io.legado.app.ui.book.findbook

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.fuyou.FyFeel
import io.legado.app.databinding.ItemReadfeelFindBinding
import io.legado.app.utils.DebugLog
import io.legado.app.utils.StringUtils
import io.legado.app.utils.gone
import io.legado.app.utils.invisible
import io.legado.app.utils.visible


class FindboolAnswerAdapter(context: Context, val callBack: CallBack) :
    RecyclerAdapter<FyFeel, ItemReadfeelFindBinding>(context) {

    override fun getViewBinding(parent: ViewGroup): ItemReadfeelFindBinding {
        return ItemReadfeelFindBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemReadfeelFindBinding,
        item: FyFeel,
        payloads: MutableList<Any>
    ) {
        val bundle = payloads.getOrNull(0) as? Bundle
        if (bundle == null) {
            bind(binding, item)
        } else {
            bindChange(binding, item, bundle)
        }

    }

    private fun bind(binding: ItemReadfeelFindBinding, item: FyFeel) {
        binding.run {
            tvUserName.text = StringUtils.getUserName(item.userId!!)
            tvCreateTime.text = StringUtils.dateConvert(item.createTime)
            tvFeelContent.text = item.content
            novelPhoto.load(item.novelPhoto, "", "")
            if (item.labels != null && item.labels != "") {
                val kinds = item.labels.split(" ")
                if (kinds.isEmpty()) {
                    lbKind.gone()
                } else {
                    lbKind.visible()
                    lbKind.setLabels(kinds)
                }
            }
        }
    }

    private fun bindChange(binding: ItemReadfeelFindBinding, item: FyFeel, bundle: Bundle) {
        binding.run {
        }
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemReadfeelFindBinding) {
        holder.itemView.setOnClickListener {
            getItem(holder.layoutPosition)?.let {
                //开启评论列表
                callBack.showComment(it.id!!)
            }
        }
        binding.tenderBook.setOnClickListener {
            binding.tenderBook.invisible()
            DebugLog.i(javaClass.name, "蜉蝣采书")
            getItem(holder.layoutPosition)?.let {
                callBack.tenderBook(it, binding)
            }
        }
        //2，开启书籍详情
        binding.novelUrl.setOnClickListener {
            getItem(holder.layoutPosition)?.let {
                callBack.startNovel(binding.novelName.text.toString(),binding.novelAuth.text.toString(),it.novelUrl!!)
            }
        }
    }

    interface CallBack {

        fun tenderBook(feel: FyFeel, binding: ItemReadfeelFindBinding)

        fun startNovel(name:String,author:String,url:String)
        fun showComment(feelId: Int)
    }
}