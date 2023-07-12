package io.legado.app.ui.main.findbook

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.data.entities.fuyou.FyComment
import io.legado.app.data.entities.fuyou.FyReply
import io.legado.app.databinding.DialogCommentViewBinding
import io.legado.app.databinding.ViewLoadMoreBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.lib.theme.primaryColor
import io.legado.app.ui.comment.CommentAdapter
import io.legado.app.ui.comment.ReplyAdapter
import io.legado.app.ui.widget.recycler.LoadMoreView
import io.legado.app.ui.widget.recycler.UpLinearLayoutManager
import io.legado.app.ui.widget.recycler.VerticalDivider
import io.legado.app.utils.DebugLog
import io.legado.app.utils.StringUtils
import io.legado.app.utils.applyTint
import io.legado.app.utils.hideSoftInput
import io.legado.app.utils.setEdgeEffectColor
import io.legado.app.utils.setLayout
import io.legado.app.utils.showSoftInput
import io.legado.app.utils.toastOnUi
import io.legado.app.utils.viewbindingdelegate.viewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import splitties.init.appCtx


class FindBookFragment() : BaseDialogFragment(R.layout.dialog_comment_view),
    CommentAdapter.Callback {
    private val commentAdapter by lazy { CommentAdapter(requireContext(), this) }
    private val mLayoutManager by lazy { UpLinearLayoutManager(requireContext()) }
    private var idSet=HashSet<Int>()
    private val binding by viewBinding(DialogCommentViewBinding::bind)
    private var curPageNum: Int = 1
    private val pageSize: Int = 20
    private var readFeelId: Int? = null
    private var commentId: Int? = null
    private var fatherId: Int? = null
    private var pages: Int = 1
    private val loadMoreView by lazy { LoadMoreView(requireContext()) }
    private var replyType:Int=2
    private var replyAdapter : ReplyAdapter? =null

    constructor(
        readFeelId: Int,
        timeCount: Int
    ) : this() {
        arguments = Bundle().apply {
            putInt("readFeelId", readFeelId)
            putInt("timeCount", timeCount)

        }
        isCancelable = false
    }

    override fun onStart() {
        super.onStart()
        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 0.7f)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
//        upAdapterLiveData.observe(this) {
//            adapter.notifyItemRangeChanged(0, adapter.itemCount, it)
//        }
//        commentAdapter.notifyItemRangeChanged(0, commentAdapter.itemCount, "isInCommentList")
        binding.toolBar.setBackgroundColor(primaryColor)
        binding.toolBar.inflateMenu(R.menu.dialog_text)
        binding.toolBar.menu.applyTint(requireContext())
        binding.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_close -> dismiss()
            }
            true
        }
        binding.toolBar.title = "评论"

        val timeCount = arguments?.getInt("timeCount")
        readFeelId = requireArguments().getInt("readFeelId")

        binding.recyclerView.setEdgeEffectColor(primaryColor)
        binding.recyclerView.layoutManager = mLayoutManager
        binding.recyclerView.addItemDecoration(VerticalDivider(requireContext()))
        binding.recyclerView.adapter = commentAdapter

        commentAdapter.addFooterView {
            ViewLoadMoreBinding.bind(loadMoreView)
        }
        loadMoreView.setOnClickListener {
            if (!loadMoreView.isLoading) {
                //1,初始化请求评论列表
                scrollToBottom()
            }
        }
        scrollToBottom()
        //2，下滑事件分页查询
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    scrollToBottom()
                }
            }
        })
        //3,发送评论
        binding.sendComment.setOnClickListener {
            binding.tieMyComment.clearFocus()
            binding.tieMyComment.hideSoftInput()
            sendReplyOrComment(timeCount)
        }
        binding.tilCommentJj.setOnClickListener{
            returnComment()
        }
    }

    private fun sendReplyOrComment(timeCount: Int?) {
        if (binding.tieMyComment.text!!.toString() != "") {
            if (replyType==2){
                //发评论
                sendComment(timeCount)
            }else {
                //发回复
                sendReply(timeCount)
            }
        }
    }

    private fun sendComment(timeCount: Int?) {
        Coroutine.async(this, Dispatchers.IO) {
            FuYouHelp.fuYouHelpPost?.run {
                publishComment(
                    lifecycleScope, FyComment(
                        readfeelId = readFeelId,
                        content = binding.tieMyComment.text!!.toString(),
                        timeCount = timeCount
                    )
                ).onSuccess {
                    DebugLog.i(javaClass.name, "评论发布成功！id：${it.id}")
                    binding.tieMyComment.text!!.clear()
                    appCtx.toastOnUi("评论发送成功")
                    commentAdapter.addItem(it)
                }.onError {
                    appCtx.toastOnUi("评论发送失败" + it.localizedMessage)
                }
            }
        }
    }

    private fun sendReply(timeCount: Int?) {
        Coroutine.async(this, Dispatchers.IO) {
            FuYouHelp.fuYouHelpPost?.run {
                publishReply(
                    lifecycleScope, FyReply(
                        commentId = commentId,
                        fatherId=fatherId,
                        content = binding.tieMyComment.text!!.toString(),
                        timeCount = timeCount
                    )
                ).onSuccess {
                    DebugLog.i(javaClass.name, "回复成功！id：${it.id}")
                    binding.tieMyComment.text!!.clear()
                    returnComment()
                    appCtx.toastOnUi("回复成功")
                    commentAdapter.addReply(replyAdapter,it)
                }.onError {
                    appCtx.toastOnUi("回复失败" + it.localizedMessage)
                }
            }
        }
    }

    private fun returnComment() {
        binding.tilCommentJj.hint = "写评论"
        replyType = 2
        binding.tieMyComment.hideSoftInput()
    }

    private fun scrollToBottom() {
        commentAdapter.let {
            if (loadMoreView.hasMore && !loadMoreView.isLoading) {
                loadMoreView.startLoad()
                queryPageComment(readFeelId!!, curPageNum)
            }
        }
    }

    /**
     * 分页请求评论列表
     */
    private fun queryPageComment(feelId: Int, pageNum: Int) {
        if (pageNum > pages) {
            loadMoreView.noMore("没有更多了")
            return
        }
        Coroutine.async(this, Dispatchers.IO) {
            FuYouHelp.fuYouHelpPost?.run {
                queryPageComment(lifecycleScope, feelId, pageNum, pageSize)
                    .onSuccess {
                        loadMoreView.stopLoad()
                        pages = it.pages!!
                        if (it.list == null || it.list!!.isEmpty()) {
                            if (commentAdapter.isEmpty()) {
                                loadMoreView.noMore(getString(R.string.empty))
                            } else {
                                loadMoreView.noMore()
                            }
                        } else {
                            curPageNum++
                            it.list!!.forEach{ fyComment ->
                                if (idSet.isNotEmpty() && idSet.contains(fyComment.id)){
//                                    adapter.updateItem(fyComment)
                                }else {
                                    idSet.add(fyComment.id!!)
                                    commentAdapter.addItem(fyComment)
                                }

                             }

                        }
                    }
                    .onError { e ->
                        e.localizedMessage?.let { loadMoreView.error(it) }
                    }
            }
        }
    }
    override val scope: CoroutineScope
        get() = lifecycleScope
    override fun reply(replyAdapter: ReplyAdapter, fyReply: FyReply) {
        this.commentId=fyReply.commentId
        this.replyType=3
        this.replyAdapter=replyAdapter;
        binding.tilCommentJj.hint= StringUtils.getUserName(fyReply.userId!!)
        binding.tieMyComment.findFocus()
        binding.tieMyComment.showSoftInput()
    }

    override fun replyFather(fyReply: FyReply, replyAdapter: ReplyAdapter) {
        this.commentId=fyReply.commentId
        this.fatherId=fyReply.fatherId
        this.replyType=3
        this.replyAdapter=replyAdapter;
        binding.tilCommentJj.hint=StringUtils.getUserName(fyReply.userId!!)
        binding.tieMyComment.findFocus()
        binding.tieMyComment.showSoftInput()
    }
}


