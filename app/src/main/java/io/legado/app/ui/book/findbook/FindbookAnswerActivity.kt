package io.legado.app.ui.book.findbook

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.base.VMBaseActivity
import io.legado.app.data.entities.fuyou.FyFeel
import io.legado.app.databinding.ActivityFindbookAnswerBinding
import io.legado.app.databinding.ItemReadfeelFindBinding
import io.legado.app.databinding.ViewLoadMoreBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.ui.book.info.BookInfoActivity
import io.legado.app.ui.comment.CommentListFragment
import io.legado.app.ui.widget.recycler.LoadMoreView
import io.legado.app.ui.widget.recycler.VerticalDivider
import io.legado.app.utils.StringUtils
import io.legado.app.utils.gone
import io.legado.app.utils.printOnDebug
import io.legado.app.utils.showDialogFragment
import io.legado.app.utils.startActivity
import io.legado.app.utils.viewbindingdelegate.viewBinding
import io.legado.app.utils.visible

class FindbookAnswerActivity :
    VMBaseActivity<ActivityFindbookAnswerBinding, FindbookAnswerViewModel>(),
    FindboolAnswerAdapter.CallBack {
    override val binding by viewBinding(ActivityFindbookAnswerBinding::inflate)
    override val viewModel by viewModels<FindbookAnswerViewModel>()

    private val adapter by lazy { FindboolAnswerAdapter(this, this) }
    private val loadMoreView by lazy { LoadMoreView(this) }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        binding.titleBar.title = intent.getStringExtra("findContent")
         val bestAnswerId = intent.getIntExtra("bestAnswerId", 0)
        if (bestAnswerId!=0){
            initBestAnswer(bestAnswerId)
        }
        initRecyclerView()
        viewModel.booksData.observe(this) { upData(it) }
        viewModel.initData(intent)
        viewModel.errorLiveData.observe(this) {
            loadMoreView.error(it)
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.addItemDecoration(VerticalDivider(this))
        binding.recyclerView.adapter = adapter
        adapter.addFooterView {
            ViewLoadMoreBinding.bind(loadMoreView)
        }
        loadMoreView.startLoad()
        loadMoreView.setOnClickListener {
            if (!loadMoreView.isLoading) {
                loadMoreView.hasMore()
                scrollToBottom()
            }
        }
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    scrollToBottom()
                }
            }
        })
    }

    private fun initBestAnswer(bestAnswerId: Int) {
        FuYouHelp.fuYouHelpPost?.run {
            findBestAnswer(
                lifecycleScope,
                bestAnswerId
            )
                .onSuccess {
                    binding.llBasteAnswer.isVisible=true
                    binding.run {
                        tvUserName.text = StringUtils.getUserName(it.userId!!)
                        tvCreateTime.text = StringUtils.dateConvert(it.createTime)
                        tvFeelContent.text = it.content
                        novelPhoto.load(it.novelPhoto, "", "")
                        if (it.labels != null && it.labels != "") {
                            val kinds = it.labels.split(" ")
                            if (kinds.isEmpty()) {
                                lbKind.gone()
                            } else {
                                lbKind.visible()
                                lbKind.setLabels(kinds)
                            }
                        }
                    }
                }.onError {
                    it.printOnDebug()
                    binding.llBasteAnswer.gone()
                }
        }
    }

    private fun scrollToBottom() {
        adapter.let {
            if (loadMoreView.hasMore && !loadMoreView.isLoading) {
                loadMoreView.startLoad()
                viewModel.queryPageAnswer()
            }
        }
    }

    private fun upData(feelList: List<FyFeel>) {
        loadMoreView.stopLoad()
        if (feelList.isEmpty() && adapter.isEmpty()) {
            loadMoreView.noMore(getString(R.string.empty))
        } else if (feelList.isEmpty()) {
            loadMoreView.noMore()
        } else if (adapter.getItems().contains(feelList.first()) && adapter.getItems()
                .contains(feelList.last())
        ) {
            loadMoreView.noMore()
        } else {
            adapter.addItems(feelList)
        }
    }


    override fun showComment(feelId: Int) {
        val dialog = CommentListFragment(feelId, viewModel.timeCount)
        showDialogFragment(dialog)
    }

    override fun tenderBook(feel: FyFeel, binding: ItemReadfeelFindBinding) {
        viewModel.tenderBook(feel, binding)
    }

    override fun startNovel(name:String,author:String,url:String) {
        startActivity<BookInfoActivity> {
            putExtra("name", name)
            putExtra("author", author)
            putExtra("bookUrl", url)
            putExtra("originType", 3)//来源类型
        }
    }
}
