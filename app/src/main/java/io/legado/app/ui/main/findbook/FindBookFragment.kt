package io.legado.app.ui.main.findbook

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.base.VMBaseFragment
import io.legado.app.data.entities.fuyou.FyFindbook
import io.legado.app.databinding.FragmentFindbookBinding
import io.legado.app.databinding.ViewLoadMoreBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.lib.theme.primaryColor
import io.legado.app.lib.theme.primaryTextColor
import io.legado.app.ui.widget.recycler.LoadMoreView
import io.legado.app.ui.widget.recycler.UpLinearLayoutManager
import io.legado.app.ui.widget.recycler.VerticalDivider
import io.legado.app.utils.applyTint
import io.legado.app.utils.cnCompare
import io.legado.app.utils.setEdgeEffectColor
import io.legado.app.utils.startActivity
import io.legado.app.utils.viewbindingdelegate.viewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.LinkedList


class FindBookFragment : VMBaseFragment<FindBookViewModel>(R.layout.fragment_findbook),
    FindBookAdapter.Callback {
    override val viewModel by viewModels<FindBookViewModel>()
    override val scope: CoroutineScope get() = lifecycleScope
    private val findBookAdapter by lazy { FindBookAdapter(requireContext(), this) }
    private val mLayoutManager by lazy { UpLinearLayoutManager(requireContext()) }

    private val binding by viewBinding(FragmentFindbookBinding::bind)
    private val searchView: SearchView by lazy {
        binding.titleBar.findViewById(R.id.search_view)
    }
    private val groups = linkedSetOf<String>()
    private var groupsMenu: SubMenu? = null
    private var searchKey:String?=null
    private var idSet = HashSet<Int>()
    private var curPageNum: Int = 1
    private val pageSize: Int = 20
    private var pages: Int = 1
    private val loadMoreView by lazy { LoadMoreView(requireContext()) }
    private val labels = LinkedList<String>()



    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        setSupportToolbar(binding.titleBar.toolbar)
        initSearchView()

        initRecyclerView()

        initGroupData()

        upExploreData()
    }
    override fun onPause() {
        super.onPause()
        searchView.clearFocus()
    }

    override fun onCompatCreateOptionsMenu(menu: Menu) {
        menuInflater.inflate(R.menu.main_findbook, menu)
        groupsMenu = menu.findItem(R.id.menu_group)?.subMenu
        upGroupsMenu()
    }
    override fun onCompatOptionsItemSelected(item: MenuItem) {
        super.onCompatOptionsItemSelected(item)
        when (item.itemId) {
            R.id.menu_findbook_publish -> startActivity<FindbookEditActivity>()
            else -> if (item.groupId == R.id.menu_group_text) {
                searchView.setQuery(item.title, true)
            }
        }
    }


    /**
     * 分页获取找书贴
     */
    private fun upExploreData(searchKey: String? = null) {
        if (loadMoreView.hasMore && !loadMoreView.isLoading) {
            loadMoreView.startLoad()
            when {
                searchKey.isNullOrBlank() -> {
                    queryPageFindBook(curPageNum,null)
                }

                searchKey.startsWith("group:") -> {
                    val key = searchKey.substringAfter("group:")
                    var requestVO = FyFindbook()
                    when(key){
                        "未解决" ->requestVO.readfeelId=0
                        "已解决" ->requestVO.readfeelId=1
                    }
                    queryPageFindBook(curPageNum,requestVO)
                }
                else -> {
                    queryPageFindBook(curPageNum, FyFindbook(labels = searchKey))
                }
            }
        }
    }

    private fun initGroupData() {
        launch {
            labels.add("全部")
            labels.add("未解决")
            labels.add("已解决")
            labels.let {
                groups.clear()
                groups.addAll(it)
                upGroupsMenu()
            }
        }
    }

    private fun upGroupsMenu() = groupsMenu?.let { subMenu ->
        subMenu.removeGroup(R.id.menu_group_text)
        groups.sortedWith { o1, o2 ->
            o1.cnCompare(o2)
        }.forEach {
            subMenu.add(R.id.menu_group_text, Menu.NONE, Menu.NONE, it)
        }
    }

    private fun initRecyclerView() {
        binding.rvFindbook.setEdgeEffectColor(primaryColor)
        binding.rvFindbook.layoutManager = mLayoutManager
        binding.rvFindbook.addItemDecoration(VerticalDivider(requireContext()))
        binding.rvFindbook.adapter = findBookAdapter

        findBookAdapter.addFooterView {
            ViewLoadMoreBinding.bind(loadMoreView)
        }
        loadMoreView.setOnClickListener {
            if (!loadMoreView.isLoading) {
                //请求找书列表
                upExploreData(searchKey)
            }
        }
        //2，下滑事件分页查询
        binding.rvFindbook.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    upExploreData(searchKey)
                }
            }
        })
    }

    private fun initSearchView() {
        searchView.applyTint(primaryTextColor)
        searchView.onActionViewExpanded()
        searchView.isSubmitButtonEnabled = true
        searchView.queryHint = getString(R.string.screen_find)
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchKey=newText
                upExploreData(newText)
                return false
            }
        })
    }

    /**
     * 分页请求评论列表
     */
    private fun queryPageFindBook( pageNum: Int,requestVO:FyFindbook?) {
        if (pageNum > pages) {
            loadMoreView.noMore("没有更多了")
            return
        }
        Coroutine.async(this, Dispatchers.IO) {
            FuYouHelp.fuYouHelpPost?.run {
                queryPageFindBook(lifecycleScope, pageNum, pageSize, requestVO)
                    .onSuccess {
                        loadMoreView.stopLoad()
                        pages = it.pages
                        if (it.list == null || it.list!!.isEmpty()) {
                            if (findBookAdapter.isEmpty()) {
                                loadMoreView.noMore(getString(R.string.empty))
                            } else {
                                loadMoreView.noMore()
                            }
                        } else {
                            curPageNum++
                            it.list!!.forEach { findbook ->
                                if (idSet.isNotEmpty() && idSet.contains(findbook.id)) {
//                                    adapter.updateItem(findbook)
                                } else {
                                    idSet.add(findbook.id!!)
                                    findBookAdapter.addItem(findbook)
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


