package io.legado.app.ui.comment

import android.content.Context
import android.view.ViewGroup
import androidx.core.view.isInvisible
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.fuyou.FyComment
import io.legado.app.data.entities.fuyou.FyReply
import io.legado.app.databinding.ItemCommentListBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.lib.theme.ThemeStore
import io.legado.app.ui.widget.recycler.UpLinearLayoutManager
import io.legado.app.ui.widget.recycler.VerticalDivider
import io.legado.app.utils.StringUtils
import io.legado.app.utils.setEdgeEffectColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class CommentAdapter(context: Context, val callback: CommentAdapter.Callback) :
    RecyclerAdapter<FyComment, ItemCommentListBinding>(context),
    ReplyAdapter.Callback{
    private val adapter by lazy { ReplyAdapter(context, this) }
    private val mLayoutManager by lazy { UpLinearLayoutManager(context) }

    private var pages:Int=0
    private var curPageNum:Int=1
    private val pageSize: Int = 20
    private var hasMore:Boolean=false
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
                    if (item.replay !=null && item.replay >0) {
                        binding.tvMoreReply.text = " 展开${item.replay}条回复"
                    }
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
        if (item.replay !=null && item.replay >0) {
            binding.tvMoreReply.text = "更多回复(${item.replay})>>"
            hasMore=true
        }else{
            binding.tvMoreReply.isInvisible=false
        }
        adapter.notifyItemRangeChanged(0, adapter.itemCount, "isInCommentList")
        binding.recyclerView.setEdgeEffectColor(ThemeStore.primaryColor(context))
        binding.recyclerView.layoutManager = mLayoutManager
        binding.recyclerView.addItemDecoration(VerticalDivider(context))
        binding.recyclerView.adapter = adapter
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemCommentListBinding) {
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
                    callback.reply(FyReply(commentId = it.id, userId = it.userId))
                }
            }
            tvMoreReply.setOnClickListener{//查看更多回复
                getItem(holder.layoutPosition)?.let {
                    queryPageReply(it.id!!,curPageNum)
                    if (hasMore) binding.tvMoreReply.text = "更多回复>>"
                    binding.tvMoreReply.isInvisible=hasMore
                }
            }
        }
    }
    override fun replyFather(fyReply: FyReply) {
        callback.reply(fyReply)
    }

    /**
     * 分页查询回复列表
     */
    private fun queryPageReply(commentId: Int, pageNum: Int){
        if (pageNum > pages) {
            hasMore=false
        }
        Coroutine.async(callback.scope, Dispatchers.IO) {
            FuYouHelp.fuYouHelpPost?.run {
                queryPageReply(callback.scope, commentId, pageNum, pageSize)
                    .onSuccess {
                        pages = it.pages!!
                        if (it.list == null || it.list!!.isEmpty()) {
                            hasMore=false
                        } else {
                            curPageNum++
                            adapter.addItems(it.list!!)
                            if (curPageNum>=pages){
                                hasMore=true
                            }
                        }
                    }
                    .onError { e ->
                        e.localizedMessage?.let { }
                    }
            }
        }
    }

    fun addReply(fyReply: FyReply){
        adapter.addItem(fyReply)
    }



    interface Callback {
        val scope: CoroutineScope
        fun reply(fyReply: FyReply)

    }


}