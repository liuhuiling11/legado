package io.legado.app.ui.book.findbook.message

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.base.VMBaseActivity
import io.legado.app.data.entities.fuyou.FyMessage
import io.legado.app.databinding.ActivityFuyouMessageBinding
import io.legado.app.databinding.ViewLoadMoreBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.ui.book.findbook.message.like.FuyouMessageLikeActivity
import io.legado.app.ui.book.findbook.message.read.FuyouMessageReadActivity
import io.legado.app.ui.book.findbook.message.tender.FuyouMessageTenderActivity
import io.legado.app.ui.comment.ReplyFragment
import io.legado.app.ui.widget.recycler.LoadMoreView
import io.legado.app.ui.widget.recycler.VerticalDivider
import io.legado.app.utils.showDialogFragment
import io.legado.app.utils.startActivity
import io.legado.app.utils.viewbindingdelegate.viewBinding
import java.util.Date

class FuyouMessageActivity :
    VMBaseActivity<ActivityFuyouMessageBinding, FuyouMessageViewModel>(),
    FuyouMessageAdapter.CallBack {
    override val binding by viewBinding(ActivityFuyouMessageBinding::inflate)
    override val viewModel by viewModels<FuyouMessageViewModel>()

    private val adapter by lazy { FuyouMessageAdapter(this, this) }
    private val loadMoreView by lazy { LoadMoreView(this) }
    private var findId: Int? = null
    private var findContent: String = "找书贴"
    private val PEPLY_FRAGMENT="replyFragment"


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        //初始化列表
        initRecyclerView()
        //注册监听
        registerListen()

        //数据观测者注册
        viewModel.booksData.observe(this) { upData(it) }
        viewModel.errorLiveData.observe(this) {
            loadMoreView.error(it)
        }
        //数量消息查询
        initNumMessage()
        //初始列表化数据
        viewModel.initData(findId!!, findContent)
    }

    /**
     * 查询数量消息
     */
    private fun initNumMessage() {
        //采纳消息数
        initTenderNum()

        //阅读消息数
        initReadNum()

        //点赞消息数
        initLoveNum()
    }

    private fun initTenderNum() {
        FuYouHelp.fuYouHelpPost?.run {
            getMessageNum(
                lifecycleScope,
                Date(),
                5
            ).onSuccess {
                    binding.bvTender.setHighlight(true)
                    binding.bvTender.setBadgeCount(it)
                }
        }
    }

    private fun initReadNum() {
        FuYouHelp.fuYouHelpPost?.run {
            getMessageNum(
                lifecycleScope,
                Date(),
                1
            ).onSuccess {
                    binding.bvTender.setHighlight(true)
                    binding.bvTender.setBadgeCount(it)
                }
        }
    }

    private fun initLoveNum() {
        FuYouHelp.fuYouHelpPost?.run {
            getMessageNum(
                lifecycleScope,
                Date(),
                3
            ).onSuccess {
                    binding.bvTender.setHighlight(true)
                    binding.bvTender.setBadgeCount(it)
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
     * 注册监听
     */
    private fun registerListen() {
        //1，数量消息
        //1.1 采纳数量
        binding.llTendered.setOnClickListener {
            showTender()
        }

        //1.2 阅读数量
        binding.llReaded.setOnClickListener {
            showRead()
        }
        //1.3，点赞数量
        binding.llLoved.setOnClickListener {
            showLike()
        }
    }

    private fun showLike() {
        startActivity<FuyouMessageLikeActivity> {
            putExtra("type", "like")
        }
    }

    private fun showRead() {
        startActivity<FuyouMessageReadActivity> {
            putExtra("type", "read")
        }
    }
    private fun showTender() {
        startActivity<FuyouMessageTenderActivity> {
            putExtra("type", "tender")
        }
    }

    /**
     * 拉取数据
     */
    private fun scrollToBottom() {
        adapter.let {
            if (loadMoreView.hasMore && !loadMoreView.isLoading) {
                loadMoreView.startLoad()
                viewModel.queryPageMessage()
            }
        }
    }
    


    private fun upData(feelList: List<FyMessage>) {
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

    override fun reply(message: FyMessage) {
        var replyFragment =supportFragmentManager.findFragmentByTag(PEPLY_FRAGMENT) as ReplyFragment?
        if (replyFragment==null) {
            replyFragment = ReplyFragment(message, 1)
            showDialogFragment(replyFragment)
        }else{
            replyFragment.initData(message,1)
            replyFragment.setHintHeUserId()
            showDialogFragment(replyFragment)
        }
    }


}
