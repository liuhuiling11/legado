package io.legado.app.ui.widget.dialog

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookSource
import io.legado.app.data.entities.fuyou.FeelBehave
import io.legado.app.databinding.DialogReadfeelViewBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.help.source.SourceHelp
import io.legado.app.lib.theme.primaryColor
import io.legado.app.ui.book.info.BookInfoActivity
import io.legado.app.utils.DebugLog
import io.legado.app.utils.GSON
import io.legado.app.utils.applyTint
import io.legado.app.utils.fromJsonObject
import io.legado.app.utils.setLayout
import io.legado.app.utils.startActivity
import io.legado.app.utils.viewbindingdelegate.viewBinding


class ReadFeelDialog() : BaseDialogFragment(R.layout.dialog_readfeel_view) {

    enum class Mode {
        MD, HTML, TEXT
    }

    constructor(
        title: String,
        content: String?,
        mode: Mode = Mode.TEXT,
        id:Int,
        timeCount:Int
    ) : this() {
        arguments = Bundle().apply {
            putString("title", title)
            putString("content", content)
            putInt("id", id )
            putInt("timeCount", timeCount)
            putString("mode", mode.name)

        }
        isCancelable = false
    }

    private val binding by viewBinding(DialogReadfeelViewBinding::bind)
    private var author:String=""
    private var name:String=""
    private var url:String=""
    private var id:Int?=null
    private var timeCount:Int=0



    override fun onStart() {
        super.onStart()
        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 0.9f)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolBar.setBackgroundColor(primaryColor)
        binding.toolBar.inflateMenu(R.menu.dialog_text)
        binding.toolBar.menu.applyTint(requireContext())
        binding.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_close -> dismiss()
            }
            true
        }

        arguments?.let { ait ->
            binding.toolBar.title = ait.getString("title")
            val content = ait.getString("content") ?: ""
            id = ait.getInt("id")
            timeCount = ait.getInt("timeCount")
            binding.textView.text = content

            binding.tenderBook.setOnClickListener{
                DebugLog.i(javaClass.name,"蜉蝣采书")
                FuYouHelp.fuYouHelpPost?.run {
                    tenderBook(
                        lifecycleScope, FeelBehave(
                            id!!, "5", timeCount)
                    ).onSuccess{
                        if (it.novelName!=null) {
                            binding.novelName.setText(it.novelName)
                        }
                        author=it.novelAuthor!!
                        name=it.novelName!!
                        url=it.novelUrl!!
                        binding.novelAuth.setText(it.novelAuthor)
                        binding.novelUrl.isVisible=true
                        binding.comment.isVisible=true

                        //1，写入书籍数据
                        //1.1 写入书源数据
                        var feelSource=GSON.fromJsonObject<BookSource>(it.sourceJson).getOrThrow()
                        //先检查是否存在
                        val source = appDb.bookSourceDao.getBookSource(feelSource.bookSourceUrl)
                        if(source==null) {
                            SourceHelp.insertBookSource(feelSource)
                        }else{
                            feelSource=source
                        }
                        //1.2 构造书籍对象
                        val book= Book(
                            name = it.novelName,
                            author = it.novelAuthor,
                            bookUrl = it.novelUrl,
                            origin = feelSource.bookSourceUrl,
                            originName = feelSource.bookSourceName,
                            coverUrl = it.novelPhoto,
                            intro = it.novelIntroduction,
                            tocUrl = it.listChapterUrl,
                            originOrder = feelSource.customOrder
                        )
                        //1.3写入查询书记录
                        appDb.searchBookDao.insert(book.toSearchBook())
                        //1.4 写入书籍
                        appDb.bookDao.insert(book)
                    }
                }
            }


            //2，开启书籍详情
            binding.novelUrl.setOnClickListener{
                startActivity<BookInfoActivity> {
                    putExtra("name", name)
                    putExtra("author", author)
                    putExtra("bookUrl",url )
                    putExtra("originType",2)//来源类型
                }
            }


        }

    }


}
