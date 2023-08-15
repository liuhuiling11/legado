package io.legado.app.ui.book.findbook.message.tender

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.base.VMBaseActivity
import io.legado.app.data.entities.fuyou.FyMessageFeel
import io.legado.app.databinding.ActivityFuyouMessageTenderBinding
import io.legado.app.databinding.ViewLoadMoreBinding
import io.legado.app.ui.book.info.BookInfoActivity
import io.legado.app.ui.widget.recycler.LoadMoreView
import io.legado.app.ui.widget.recycler.VerticalDivider
import io.legado.app.utils.startActivity
import io.legado.app.utils.viewbindingdelegate.viewBinding

class FuyouMessageTenderActivity :
    VMBaseActivity<ActivityFuyouMessageTenderBinding, FuyouMessageTenderViewModel>(),
    FuyouMessageTenderAdapter.CallBack {
    override val binding by viewBinding(ActivityFuyouMessageTenderBinding::inflate)
    override val viewModel by viewModels<FuyouMessageTenderViewModel>()

    private val adapter by lazy { FuyouMessageTenderAdapter(this, this) }
    private val loadMoreView by lazy { LoadMoreView(this) }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        //初始化列表
        initRecyclerView()

        //数据观测者注册
        viewModel.booksData.observe(this) { upData(it) }
        viewModel.errorLiveData.observe(this) {
            loadMoreView.error(it)
        }
        //初始列表化数据
        viewModel.initData()
    }


    /**
     * 初始化列表
     */
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

    /**
     * 拉取数据
     */
    private fun scrollToBottom() {
        adapter.let {
            if (loadMoreView.hasMore && !loadMoreView.isLoading) {
                loadMoreView.startLoad()
                viewModel.queryPageMessageTender()
            }
        }
    }
    


    private fun upData(feelList: List<FyMessageFeel>) {
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
            if (!viewModel.hasNextPage()) {
                loadMoreView.noMore()
            }
            adapter.addItems(feelList)
        }
    }

    override fun startNovel(novelName: String, novelAuth: String, novelUrl: String) {
        startActivity<BookInfoActivity> {
            putExtra("name", novelName)
            putExtra("author", novelAuth)
            putExtra("bookUrl", novelUrl)
            putExtra("originType", 5)//来源类型
        }
    }


}
