package io.legado.app.ui.comment

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.fuyou.FyComment
import io.legado.app.data.entities.fuyou.FyReply
import io.legado.app.databinding.ItemCommentListBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.lib.theme.ThemeStore
import io.legado.app.ui.widget.recycler.VerticalDivider
import io.legado.app.utils.StringUtils
import io.legado.app.utils.gone
import io.legado.app.utils.setEdgeEffectColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class CommentAdapter(context: Context, val callback: CommentAdapter.Callback) :
    RecyclerAdapter<FyComment, ItemCommentListBinding>(context),
    ReplyAdapter.Callback{

    private val recyclerPool = RecyclerView.RecycledViewPool()
    private var pages:Int=0
    private var curPageNum:Int=1
    private val pageSize: Int = 10
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
                    tvLike.text = item.numLike.toString()
                    if (item.numReply !=null && item.numReply >0) {
                        binding.tvMoreReply.text = " 展开${item.numReply}条回复"
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
        binding.tvUserName.text = StringUtils.getUserName(item.userId!!)
        binding.tvLike.text = item.numLike.toString()
        if (item.numReply !=null && item.numReply >0) {
            binding.tvMoreReply.text = "更多回复(${item.numReply})>>"
            hasMore=true
        }else{
            binding.tvMoreReply.gone()
        }

        binding.recyclerView.setRecycledViewPool(recyclerPool)
        binding.recyclerView.setEdgeEffectColor(ThemeStore.primaryColor(context))
        binding.recyclerView.layoutManager =  GridLayoutManager(context, 1)
        binding.recyclerView.addItemDecoration(VerticalDivider(context))

    }

    @SuppressLint("SetTextI18n")
    override fun registerListener(holder: ItemViewHolder, binding: ItemCommentListBinding) {
        var replyAdapter =binding.recyclerView.adapter
        if (replyAdapter !is ReplyAdapter){
            replyAdapter=ReplyAdapter(context,this@CommentAdapter)
            binding.recyclerView.adapter=replyAdapter
        }
        binding.run {
            var i=0;
            tvLike.setOnClickListener{//点赞
                tvLike.isSelected=(i==0)
                if (i==0){
                    i =1
                    tvLike.text= (1+tvLike.text.toString().toInt()).toString()
                    getItem(holder.layoutPosition)?.let {
                        FuYouHelp.fuYouHelpPost?.run {
                            sendLikeBehave(it.id!!,2)
                        }
                    }
                } else {
                    i =0
                }
            }
            tvComment.setOnClickListener{//回复
                getItem(holder.layoutPosition)?.let {
                    callback.reply(replyAdapter,FyReply(commentId = it.id, userId = it.userId))
                }
            }
            tvMoreReply.setOnClickListener{//查看更多回复
                getItem(holder.layoutPosition)?.let {
                    queryPageReply(replyAdapter,it.id!!,curPageNum)
                    if (hasMore) {
                        binding.tvMoreReply.text = "更多回复>>"
                        binding.tvMoreReply.isVisible
                    }else{
                        binding.tvMoreReply.gone()
                    }

                }
            }
        }
    }



    override fun replyFather(fyReply: FyReply, replyAdapter: ReplyAdapter) {
        return callback.replyFather( fyReply,replyAdapter)
    }

    /**
     * 分页查询回复列表
     */
    private fun queryPageReply(
        replyAdapter: ReplyAdapter,
        commentId: Int,
        pageNum: Int
    ){
        if (pageNum > pages) {
            hasMore=false
        }
        Coroutine.async(callback.scope, Dispatchers.IO) {
            FuYouHelp.fuYouHelpPost?.run {
                queryPageReply(callback.scope, commentId, pageNum, pageSize)
                    .onSuccess {
                        pages = it.pages
                        if (it.list == null || it.list!!.isEmpty()) {
                            hasMore=false
                        } else {
                            curPageNum++
                            replyAdapter.addItems(it.list!!)
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

    fun addReply(replyAdapter: ReplyAdapter?, fyReply: FyReply){
        if (replyAdapter !=null){
            replyAdapter.addItem(fyReply)
            replyAdapter.notifyDataSetChanged()
        }
    }



    interface Callback {
        val scope: CoroutineScope
        fun reply(replyAdapter: ReplyAdapter, fyReply: FyReply)
        fun replyFather(fyReply: FyReply, replyAdapter: ReplyAdapter)
    }


}