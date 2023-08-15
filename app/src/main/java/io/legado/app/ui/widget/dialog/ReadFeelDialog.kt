package io.legado.app.ui.widget.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookSource
import io.legado.app.data.entities.fuyou.FeelBehave
import io.legado.app.data.entities.fuyou.FyFeel
import io.legado.app.databinding.DialogReadfeelViewBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.help.source.SourceHelp
import io.legado.app.lib.theme.primaryColor
import io.legado.app.ui.book.info.BookInfoActivity
import io.legado.app.ui.comment.CommentListFragment
import io.legado.app.utils.DebugLog
import io.legado.app.utils.GSON
import io.legado.app.utils.StringUtils
import io.legado.app.utils.applyTint
import io.legado.app.utils.fromJsonObject
import io.legado.app.utils.invisible
import io.legado.app.utils.setLayout
import io.legado.app.utils.showDialogFragment
import io.legado.app.utils.startActivity
import io.legado.app.utils.toastOnUi
import io.legado.app.utils.viewbindingdelegate.viewBinding
import io.legado.app.utils.visible
import splitties.init.appCtx


class ReadFeelDialog() : BaseDialogFragment(R.layout.dialog_readfeel_view) {

    enum class Mode {
        MD, HTML, TEXT
    }

    constructor(
        title: String,
        mode: Mode = Mode.TEXT,
        timeCount: Int,
        readFeel: FyFeel
    ) : this() {
        curFeel = readFeel
        this.timeCount = timeCount
        arguments = Bundle().apply {
            putString("title", title)
            putString("mode", mode.name)

        }
        isCancelable = false
    }

    private val binding by viewBinding(DialogReadfeelViewBinding::bind)
    private var curFeel: FyFeel= FyFeel()
    private var timeCount: Int = 0


    override fun onStart() {
        super.onStart()
        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 0.9f)
    }

    @SuppressLint("SetTextI18n")
    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolBar.setBackgroundColor(primaryColor)
        binding.toolBar.inflateMenu(R.menu.dialog_text)
        binding.toolBar.menu.applyTint(requireContext())
        binding.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_close -> dismiss()
                R.id.menu_other -> changeOther()
            }
            true
        }

        arguments?.let { ait -> binding.toolBar.title = ait.getString("title") }

        bindViewText()

        binding.tenderBook.setOnClickListener {
            binding.tenderBook.invisible()
            DebugLog.i(javaClass.name, "蜉蝣采书")
            FuYouHelp.fuYouHelpPost?.run {
                tenderBook(
                    lifecycleScope, FeelBehave(
                        curFeel.id!!, "5", timeCount
                    )
                ).onSuccess {
                    if (it.novelName != null) {
                        binding.novelName.text = it.novelName
                    }
                    curFeel.novelAuthor = it.novelAuthor!!
                    curFeel.novelName = it.novelName!!
                    curFeel.novelUrl = it.novelUrl!!
                    binding.novelAuth.text = it.novelAuthor
                    binding.novelUrl.visible()
                    binding.comment.visible()

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


            //2，开启书籍详情
            binding.novelUrl.setOnClickListener {
                startActivity<BookInfoActivity> {
                    putExtra("name", curFeel.novelName)
                    putExtra("author", curFeel.novelAuthor)
                    putExtra("bookUrl", curFeel.novelUrl)
                    putExtra("originType", 2)//来源类型
                }
            }

            //开启评论列表
            binding.comment.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    val dialog = CommentListFragment(curFeel.id!!, timeCount)
                    showDialogFragment(dialog)
                }
            })
        }

    }

    /**
     * 绑定界面文字
     */
    @SuppressLint("SetTextI18n")
    private fun bindViewText() {
        binding.run {
            userId.text = StringUtils.getUserName(curFeel.userId!!)
            createTime.text = StringUtils.dateConvert(curFeel.createTime)
            textView.text = curFeel.content
            if (curFeel.commentUser != null) {
                hotUser.text = StringUtils.getUserName(curFeel.commentUser!!)
            }
            if (curFeel.commentContent != null) {
                hotComment.text = curFeel.commentContent
            }
            novelPhoto.load(curFeel.novelPhoto, "", "")
            tvCommentNum.text= curFeel.commentNum.toString() +" 评"
            tvTenderNum.text= curFeel.tenderNum.toString() +" 采"
            tvSaveNum.text= curFeel.saveRate() +"% 存"

            isLocalBook(curFeel.novelId)
        }

    }

    private fun isLocalBook(fyBookId:Int?){
        if (fyBookId!=null) {
            appDb.bookDao.getBook(fyBookId)?.let {
                binding.tenderBook.invisible()
                curFeel.novelAuthor = it.author
                curFeel.novelName = it.name
                curFeel.novelUrl = it.bookUrl
                binding.novelAuth.text = it.author
                binding.novelName.text = it.name
                binding.novelUrl.visible()
                binding.comment.visible()
            }
        }
    }

    private fun reVisibleView(){
        binding.tenderBook.visible()
        binding.novelUrl.invisible()
        binding.comment.invisible()
    }

    /**
     * 换一个读后感
     */
    private fun changeOther() {
        FuYouHelp.fuYouHelpPost?.run {
            findReadFeel(lifecycleScope)
                .onSuccess {
                    curFeel=it
                    timeCount=15
                    reVisibleView()
                    bindViewText()
                }
        }
    }


}
