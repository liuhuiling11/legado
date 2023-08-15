package io.legado.app.ui.book.findbook.message.tender

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.base.VMBaseActivity
import io.legado.app.data.entities.fuyou.FyMessageFeel
import io.legado.app.databinding.ActivityFuyouMessageTenderBinding
import io.legado.app.databinding.ViewLoadMoreBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.ui.widget.recycler.LoadMoreView
import io.legado.app.ui.widget.recycler.VerticalDivider
import io.legado.app.utils.viewbindingdelegate.viewBinding
import java.util.Date

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
    

    private fun initTenderNum() {
        FuYouHelp.fuYouHelpPost?.run {
            getTenderNum(
                lifecycleScope,
                Date()
            ).onSuccess {
                    
                }
        }
    }

    private fun initReadNum() {
        FuYouHelp.fuYouHelpPost?.run {
            getReadNum(
                lifecycleScope,
                Date()
            ).onSuccess {
                    
                }
        }
    }

    private fun initLoveNum() {
        FuYouHelp.fuYouHelpPost?.run {
            getLoveNum(
                lifecycleScope,
                Date()
            ).onSuccess {
                    
                }
        }
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



}
