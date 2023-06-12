package io.legado.app.ui.comment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.data.entities.fuyou.FyComment
import io.legado.app.databinding.DialogCommentViewBinding
import io.legado.app.databinding.ViewLoadMoreBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.lib.theme.primaryColor
import io.legado.app.ui.widget.recycler.LoadMoreView
import io.legado.app.ui.widget.recycler.UpLinearLayoutManager
import io.legado.app.ui.widget.recycler.VerticalDivider
import io.legado.app.utils.DebugLog
import io.legado.app.utils.applyTint
import io.legado.app.utils.hideSoftInput
import io.legado.app.utils.setEdgeEffectColor
import io.legado.app.utils.setLayout
import io.legado.app.utils.toastOnUi
import io.legado.app.utils.viewbindingdelegate.viewBinding
import kotlinx.coroutines.Dispatchers
import splitties.init.appCtx


class CommentListFragment() : BaseDialogFragment(R.layout.dialog_comment_view),
    CommentAdapter.Callback {
    private val adapter by lazy { CommentAdapter(requireContext(), this) }
    private val mLayoutManager by lazy { UpLinearLayoutManager(requireContext()) }
    private val upAdapterLiveData = MutableLiveData<String>()
    private var idSet=HashSet<Int>()
    private val binding by viewBinding(DialogCommentViewBinding::bind)
    private var curPageNum: Int = 1
    private val pageSize: Int = 20
    private var readFeelId: Int = -1
    private var pages: Int = 1
    private val loadMoreView by lazy { LoadMoreView(requireContext()) }

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
        adapter.notifyItemRangeChanged(0, adapter.itemCount, "isInCommentList")
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
        binding.recyclerView.adapter = adapter

        adapter.addFooterView {
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
            if (binding.tieMyComment.text!!.toString() != "") {
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
                            loadMoreView.hasMore()
                            queryPageComment(readFeelId, pages)
                        }.onError {
                            appCtx.toastOnUi("评论发送失败" + it.localizedMessage)
                        }
                    }
                }
            }
        }
    }

    private fun isInComment() {
        upAdapterLiveData.postValue("isInCommentList")
    }

    private fun scrollToBottom() {
        adapter.let {
            if (loadMoreView.hasMore && !loadMoreView.isLoading) {
                loadMoreView.startLoad()
                queryPageComment(readFeelId, curPageNum)
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
                            if (adapter.isEmpty()) {
                                loadMoreView.noMore(getString(R.string.empty))
                            } else {
                                loadMoreView.noMore()
                            }
                        } else {
                            curPageNum++
                            it.list!!.forEach{ fyComment ->
                                if (idSet.isNotEmpty() && idSet.contains(fyComment.id)){
//                                    isInComment()
                                }else {
                                    idSet.add(fyComment.id!!)
                                    adapter.addItem(fyComment)
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
}


