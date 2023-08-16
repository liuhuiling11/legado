package io.legado.app.ui.book.findbook.message.like

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.base.VMBaseActivity
import io.legado.app.data.entities.fuyou.FyMessageComment
import io.legado.app.databinding.ActivityFuyouMessageLikeBinding
import io.legado.app.databinding.ViewLoadMoreBinding
import io.legado.app.ui.widget.recycler.LoadMoreView
import io.legado.app.ui.widget.recycler.VerticalDivider
import io.legado.app.utils.viewbindingdelegate.viewBinding

class FuyouMessageLikeActivity :
    VMBaseActivity<ActivityFuyouMessageLikeBinding, FuyouMessageLikeViewModel>(),
    FuyouMessageLikeAdapter.CallBack {
    override val binding by viewBinding(ActivityFuyouMessageLikeBinding::inflate)
    override val viewModel by viewModels<FuyouMessageLikeViewModel>()

    private val adapter by lazy { FuyouMessageLikeAdapter(this, this) }
    private val loadMoreView by lazy { LoadMoreView(this) }
    private var idSet= HashSet<Int>()


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
                viewModel.queryPageMessageRead()
            }
        }
    }
    


    private fun upData(messageCommentList: List<FyMessageComment>) {
        loadMoreView.stopLoad()
        if (messageCommentList.isEmpty() && adapter.isEmpty()) {
            loadMoreView.noMore(getString(R.string.empty))
        } else if (messageCommentList.isEmpty()) {
            loadMoreView.noMore()
        } else if (adapter.getItems().contains(messageCommentList.first()) && adapter.getItems()
                .contains(messageCommentList.last())
        ) {
            loadMoreView.noMore()
        } else {
            if (!viewModel.hasNextPage()) {
                loadMoreView.noMore()
            }
            messageCommentList.forEach {
                if(idSet.isNotEmpty() && idSet.contains(it.id)){

                }else{
                    idSet.add(it.id!!)
                    adapter.addItem(it)
                }
            }
        }
    }



}
