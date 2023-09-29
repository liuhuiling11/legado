package io.legado.app.ui.book.findbook.message

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.base.VMBaseActivity
import io.legado.app.data.entities.fuyou.FyMessage
import io.legado.app.databinding.ActivityFuyouMessageBinding
import io.legado.app.databinding.ViewLoadMoreBinding
import io.legado.app.ui.book.findbook.message.like.FuyouMessageLikeActivity
import io.legado.app.ui.book.findbook.message.read.FuyouMessageReadActivity
import io.legado.app.ui.book.findbook.message.tender.FuyouMessageTenderActivity
import io.legado.app.ui.comment.ReplyFragment
import io.legado.app.ui.widget.recycler.LoadMoreView
import io.legado.app.ui.widget.recycler.VerticalDivider
import io.legado.app.utils.StringUtils
import io.legado.app.utils.invisible
import io.legado.app.utils.showDialogFragment
import io.legado.app.utils.startActivity
import io.legado.app.utils.viewbindingdelegate.viewBinding

class FuyouMessageActivity :
    VMBaseActivity<ActivityFuyouMessageBinding, FuyouMessageViewModel>(),
    FuyouMessageAdapter.CallBack {
    override val binding by viewBinding(ActivityFuyouMessageBinding::inflate)
    override val viewModel by viewModels<FuyouMessageViewModel>()

    private val adapter by lazy { FuyouMessageAdapter(this, this) }
    private val loadMoreView by lazy { LoadMoreView(this) }
    private val PEPLY_FRAGMENT="replyFragment"
    private var idSet= HashSet<Int>()


    override fun onDestroy() {
        viewModel.updateHadReadMessageTime()
        super.onDestroy()
    }
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
        viewModel.tenderNum.observe(this){initTenderNum(it)}
        viewModel.readNum.observe(this){initReadNum(it)}
        viewModel.likeNum.observe(this){initLikeNum(it)}

        //初始列表化数据
        viewModel.initData()
    }


    private fun initTenderNum(num:Int) {
        if (num <=0){
            binding.bvTender.invisible()
            return
        }
        binding.bvTender.setHighlight(true)
        binding.bvTender.setBadgeCount(num)
    }

    private fun initReadNum(num:Int) {
        if (num <=0){
            binding.bvReaded.invisible()
            return
        }
        binding.bvReaded.setHighlight(true)
        binding.bvReaded.setBadgeCount(num)
    }

    private fun initLikeNum(num:Int) {
        if (num <=0){
            binding.bvLoved.invisible()
            return
        }
        binding.bvLoved.setHighlight(true)
        binding.bvLoved.setBadgeCount(num)
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
        binding.bvLoved.invisible()
        startActivity<FuyouMessageLikeActivity> {
            putExtra("type", "like")
        }
    }

    private fun showRead() {
        binding.bvReaded.invisible()
        startActivity<FuyouMessageReadActivity> {
            putExtra("type", "read")
        }
    }
    private fun showTender() {
        binding.bvTender.invisible()
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
    


    private fun upData(messageList: List<FyMessage>) {
        loadMoreView.stopLoad()
        if (messageList.isEmpty() && adapter.isEmpty()) {
            loadMoreView.noMore(getString(R.string.empty))
        } else if (messageList.isEmpty()) {
            loadMoreView.noMore()
        } else if (adapter.getItems().contains(messageList.first()) && adapter.getItems()
                .contains(messageList.last())
        ) {
            loadMoreView.noMore()
        } else {
            if (!viewModel.hasNextPage()) {
                loadMoreView.noMore()
            }
            adapter.addItems(messageList)
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

    override fun isUnRead(createTime: String): Boolean {
        return StringUtils.isAfter(createTime,viewModel.lastMessageTime)
    }


}
