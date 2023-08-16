package io.legado.app.ui.book.findbook

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.base.VMBaseActivity
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookSource
import io.legado.app.data.entities.fuyou.FeelBehave
import io.legado.app.data.entities.fuyou.FyFeel
import io.legado.app.databinding.ActivityFindbookAnswerBinding
import io.legado.app.databinding.ItemReadfeelFindBinding
import io.legado.app.databinding.ViewLoadMoreBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.help.config.LocalConfig
import io.legado.app.help.source.SourceHelp
import io.legado.app.ui.book.findbook.answer.SelectBookFragment
import io.legado.app.ui.book.info.BookInfoActivity
import io.legado.app.ui.comment.CommentListFragment
import io.legado.app.ui.widget.recycler.LoadMoreView
import io.legado.app.ui.widget.recycler.VerticalDivider
import io.legado.app.utils.DebugLog
import io.legado.app.utils.GSON
import io.legado.app.utils.StringUtils
import io.legado.app.utils.fromJsonObject
import io.legado.app.utils.gone
import io.legado.app.utils.invisible
import io.legado.app.utils.printOnDebug
import io.legado.app.utils.showDialogFragment
import io.legado.app.utils.startActivity
import io.legado.app.utils.toastOnUi
import io.legado.app.utils.viewbindingdelegate.viewBinding
import io.legado.app.utils.visible
import splitties.init.appCtx

class FindbookAnswerActivity :
    VMBaseActivity<ActivityFindbookAnswerBinding, FindbookAnswerViewModel>(),
    FindbookAnswerAdapter.CallBack {
    private var idSet= HashSet<Int>()
    override val binding by viewBinding(ActivityFindbookAnswerBinding::inflate)
    override val viewModel by viewModels<FindbookAnswerViewModel>()

    private val adapter by lazy { FindbookAnswerAdapter(this, this) }
    private val loadMoreView by lazy { LoadMoreView(this) }
    private var findId:Int?=null
    private var findContent:String="找书贴"
    private val  SELECT_BOOK_FRAGMENT="selectBookFragment"


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        findContent = intent.getStringExtra("findContent")?:findContent
        binding.titleBar.title = findContent
        findId = intent.getIntExtra("findId", 0)
         val bestAnswerId = intent.getIntExtra("bestAnswerId", 0)
         val findUserId = intent.getStringExtra("userId")
        if (findUserId !=null && findUserId==LocalConfig.fyUserId){
            //自己的找书贴
            viewModel.isSelfFind = true
            if(bestAnswerId!=0){
                //已经设置了最佳答案
                binding.tvAddAnswer.text="已设置最佳答案"
                viewModel.hadSetBest=true
            }else {
                binding.tvAddAnswer.text = "设置最佳答案"
                viewModel.hadSetBest=false
            }
        }
        //注册监听
        registerListen()
        binding.llBasteAnswer.gone()
        if (bestAnswerId!=0){
            initBestAnswer(bestAnswerId)
        }

        initRecyclerView()
        initFragment()
        viewModel.booksData.observe(this) { upData(it) }

        viewModel.initData(findId!!,findContent)

        viewModel.errorLiveData.observe(this) {
            loadMoreView.error(it)
        }
    }

    private fun initFragment() {
        var selectBookFragment =supportFragmentManager.findFragmentByTag(SELECT_BOOK_FRAGMENT) as SelectBookFragment?
        if (selectBookFragment==null) {
            val bundle=Bundle()
            bundle.putInt("findId",findId?:0)
            selectBookFragment=SelectBookFragment()
            selectBookFragment.findId=findId!!
            selectBookFragment.arguments = bundle
            supportFragmentManager.beginTransaction()
                .replace(R.id.selectbook_fragment, selectBookFragment, SELECT_BOOK_FRAGMENT)
                .commit()
        }else{
            selectBookFragment.findId=findId!!
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
                bestAnswerId,
                findId
            )
                .onSuccess {
                    binding.llBasteAnswer.visible()
                    binding.run {
                        viewModel.bestAnswer=it
                        tvUserName.text = StringUtils.getUserName(it.userId!!)
                        tvCreateTime.text = StringUtils.dateConvert(it.createTime)
                        tvFeelContent.text = it.content
                        tvCommentNum.text= it.numComment.toString() +" 评"
                        tvTenderNum.text= it.numTender.toString() +" 采"
                        tvSaveNum.text= it.saveRate() +"% 存"
                        novelPhoto.load(it.novelPhoto, "", "")
                        if (it.labels != null && it.labels != "") {
                            val kinds = it.labels.split(" ")
                            if (kinds.isEmpty()) {
                                llKind.gone()
                            } else {
                                llKind.visible()
                                lbKind.setLabels(kinds)
                            }
                        }
                    }
                }.onError {
                    it.printOnDebug()
                }
        }
    }

    private fun registerListen(){
        //1，最佳答案监听，
        //1.1 展示评论
        binding.llBasteAnswer.setOnClickListener {
            showComment(viewModel.bestAnswer!!.id!!)
        }

        //1.2 采书
        binding.tenderBook.setOnClickListener {
            binding.tenderBook.gone()
            DebugLog.i(javaClass.name, "蜉蝣采书")
            FuYouHelp.fuYouHelpPost?.run {
                tenderBook(
                    lifecycleScope, FeelBehave(
                        viewModel.bestAnswer!!.id!!, "5", viewModel.timeCount
                    )
                ).onSuccess {
                    if (it.novelName != null) {
                        binding.novelName.setText(it.novelName)
                    }

                    viewModel.bestAnswer!!.novelUrl = it.novelUrl!!
                    binding.novelAuth.setText(it.novelAuthor)
                    binding.novelUrl.isVisible = true


                    //1，写入书籍数据
                    //1.1 写入书源数据
                    var feelSource = GSON.fromJsonObject<BookSource>(it.sourceJson).getOrThrow()
                    //先检查是否存在
                    val source = appDb.bookSourceDao.getBookSource(feelSource.bookSourceUrl)
                    if (source == null) {
                        SourceHelp.insertBookSource(feelSource)
                    } else {
                        feelSource = source
                    }
                    //1.2 构造书籍对象
                    val book = Book(
                        name = it.novelName!!,
                        author = it.novelAuthor!!,
                        bookUrl = it.novelUrl!!,
                        origin = feelSource.bookSourceUrl,
                        originName = feelSource.bookSourceName,
                        coverUrl = it.novelPhoto,
                        intro = it.novelIntroduction,
                        tocUrl = it.listChapterUrl,
                        originOrder = feelSource.customOrder,
                        fyBookId = it.novelId
                    )
                    //1.3写入查询书记录
                    appDb.searchBookDao.insert(book.toSearchBook())
                    //1.4 写入书籍
                    appDb.bookDao.insert(book)
                }
                    .onError {
                        appCtx.toastOnUi("采书失败！${it.localizedMessage}")
                    }
            }

        }
        //1.3，开启书籍详情
        binding.novelUrl.setOnClickListener {
            startNovel(viewModel.bestAnswer!!.novelName!!,viewModel.bestAnswer!!.novelAuthor!!,viewModel.bestAnswer!!.novelUrl!!)
        }

        //2. 添加回答
        binding.tvAddAnswer.setOnClickListener{
            if (!viewModel.isSelfFind) {
                //不是自己的找书贴，才可作答
                if(binding.llBasteAnswer.isVisible) {
                    binding.llBasteAnswer.invisible()
                }
                binding.llFooter.invisible()
                binding.recyclerView.invisible()
                binding.titleBar.invisible()
                binding.llFragment.isVisible = true
            }else{
                //设置最佳答案
                if(viewModel.hadSetBest){
                    //已经设置了最佳答案
                    appCtx.toastOnUi("你已经设置了最佳答案")
                }else{
                    binding.tvAddAnswer.text = "请设置最佳答案"
                    viewModel.willSetBest=true
                    appCtx.toastOnUi("请长按书籍详情，设置最佳答案")
                }
            }
        }

        //3. 添加关注
        binding.tvAddCare.setOnClickListener {
            appCtx.toastOnUi("后续功能，敬请期待")
        }

        //4.关闭书架
        binding.tvCloseBook.setOnClickListener {
            if(binding.llBasteAnswer.isInvisible){
                binding.llBasteAnswer.visible()
            }
            binding.llFragment.gone()
            binding.titleBar.visible()
            binding.llFooter.visible()
            binding.recyclerView.visible()
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
            if (!viewModel.hasNextPage()){
                loadMoreView.noMore()
            }
            feelList.forEach {
                if(idSet.isNotEmpty() && idSet.contains(it.id)){

                }else{
                    idSet.add(it.id!!)
                    adapter.addItem(it)
                }
            }
        }
    }


    override fun showComment(feelId: Int) {
        val dialog = CommentListFragment(feelId, viewModel.timeCount)
        showDialogFragment(dialog)
    }

    override fun setBestAnswer(feel: FyFeel): Boolean {
        if(!viewModel.willSetBest){
            //未准备开始设置最佳答案
            return false
        }
        //设置为最佳答案
        viewModel.setBestAnswer(feel.id!!)
            ?.onSuccess{
            if (it){
                //设置成功
                viewModel.hadSetBest=true
                binding.tvAddAnswer.text="已设置最佳答案"
                initBestAnswer(feel.id)
                adapter.removeItem(feel)
            }else{
                //设置失败
                appCtx.toastOnUi("设置最佳答案失败")
            }
        }?.onError {
                appCtx.toastOnUi("设置最佳答案失败:"+it.localizedMessage)
            }
         return true
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
