package io.legado.app.ui.book.findbook

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.isVisible
import io.legado.app.R
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.SearchBook
import io.legado.app.data.entities.fuyou.FyFeel
import io.legado.app.databinding.ItemReadfeelFindBinding
import io.legado.app.databinding.ItemSearchBinding
import io.legado.app.help.config.AppConfig
import io.legado.app.utils.gone
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
            tvUserName.text = item.name
            tvAuthor.text = context.getString(R.string.author_show, item.author)
            ivInBookshelf.isVisible = callBack.isInBookshelf(item.name, item.author)
            if (item.latestChapterTitle.isNullOrEmpty()) {
                tvLasted.gone()
            } else {
                tvLasted.text = context.getString(R.string.lasted_show, item.latestChapterTitle)
                tvLasted.visible()
            }
            tvIntroduce.text = item.trimIntro(context)
            val kinds = item.getKindList()
            if (kinds.isEmpty()) {
                llKind.gone()
            } else {
                llKind.visible()
                llKind.setLabels(kinds)
            }
            ivCover.load(
                item.coverUrl,
                item.name,
                item.author,
                AppConfig.loadCoverOnlyWifi,
                item.origin
            )
        }
    }

    private fun bindChange(binding: ItemReadfeelFindBinding, item: FyFeel, bundle: Bundle) {
        binding.run {
            bundle.keySet().forEach {
                when (it) {
                    "isInBookshelf" -> ivInBookshelf.isVisible =
                        callBack.isInBookshelf(item.name, item.author)
                }
            }
        }
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemReadfeelFindBinding) {
        holder.itemView.setOnClickListener {
            getItem(holder.layoutPosition)?.let {
                callBack.showBookInfo(it.toBook())
            }
        }
    }

    interface CallBack {

        fun showBookInfo(book: Book)
    }
}