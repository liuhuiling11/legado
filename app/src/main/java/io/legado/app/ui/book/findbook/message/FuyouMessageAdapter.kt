package io.legado.app.ui.book.findbook.message

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.fuyou.FyMessage
import io.legado.app.data.entities.fuyou.MessageType
import io.legado.app.databinding.ItemMessageContentBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.lib.theme.ThemeStore
import io.legado.app.utils.StringUtils


class FuyouMessageAdapter(context: Context, val callBack: CallBack) :
    RecyclerAdapter<FyMessage, ItemMessageContentBinding>(context) {

    override fun getViewBinding(parent: ViewGroup): ItemMessageContentBinding {
        return ItemMessageContentBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemMessageContentBinding,
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
    private fun bind(binding: ItemMessageContentBinding, item: FyMessage) {
        binding.run {
            tvUserName.text = StringUtils.getUserName(item.userId!!)
            if (callBack.isUnRead(item.createTime!!)){
                tvContent.setTextColor(ThemeStore.accentColor(context))
                tvCreateTime.setTextColor(ThemeStore.accentColor(context))
            }
            tvCreateTime.text = StringUtils.dateConvert(item.createTime?:"")
            tvContent.text = item.content
            when(item.type){
                1 -> tvReplyType.text="回答了你的找书贴"
                2 -> tvReplyType.text="评论了你的读后感"
                3 -> tvReplyType.text="回复了你的评论"
                4 -> tvReplyType.text="回复了你的评论"
            }

        }
    }

    private fun bindChange(binding: ItemMessageContentBinding, item: FyMessage, bundle: Bundle) {
        binding.run {
        }
    }

    @SuppressLint("SetTextI18n")
    override fun registerListener(holder: ItemViewHolder, binding: ItemMessageContentBinding) {
        //1，查看详情
//        holder.itemView.setOnClickListener {
//            getItem(holder.layoutPosition)?.let {
//                //开启详情
//                showContent(it)
//            }
//        }

        //2，直接点赞
        var i =0
        binding.run {
            tvLike.setOnClickListener {
                tvLike.isSelected=(i==0)
                if (i==0){
                    i =1
                    getItem(holder.layoutPosition)?.let {
                        if (it.type !=MessageType.ANSWER.code) {
                            FuYouHelp.fuYouHelpPost?.run {
                                sendLikeBehave(it.contentId!!, if(it.type==2)2 else 3)
                            }
                        }
                    }
                } else {
                    i =0
                }
            }
        //3.直接回复
            tvReply.setOnClickListener{
                getItem(holder.layoutPosition)?.let {
                    callBack.reply(it)
                }
            }
        }
    }

    interface CallBack {
        fun reply(message: FyMessage)
        fun isUnRead(createTime: String):Boolean
    }
}