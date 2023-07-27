package io.legado.app.ui.book.findbook

import android.app.Application
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.legado.app.base.BaseViewModel
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookSource
import io.legado.app.data.entities.fuyou.FeelBehave
import io.legado.app.data.entities.fuyou.FyFeel
import io.legado.app.data.entities.fuyou.FyFindbook
import io.legado.app.databinding.ItemReadfeelFindBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.help.source.SourceHelp
import io.legado.app.utils.GSON
import io.legado.app.utils.fromJsonObject
import io.legado.app.utils.printOnDebug
import io.legado.app.utils.stackTraceStr
import io.legado.app.utils.toastOnUi
import splitties.init.appCtx


class FindbookAnswerViewModel(application: Application) : BaseViewModel(application) {
    val booksData = MutableLiveData<List<FyFeel>>()
    val errorLiveData = MutableLiveData<String>()
    var timeCount: Int = 0
    private var findBook: FyFindbook? = null
    private var pages: Int = 1
    private val pageSize: Int = 20
    private var curPageNum = 1
    var bestAnswer :FyFeel?=null
    fun initData(findId: Int,findContent:String) {
        execute {
            if (findBook == null && findId != 0) {
                findBook = FyFindbook(id = findId, content = findContent)
            }
            queryPageAnswer()
        }
    }

    fun queryPageAnswer() {
        val findbook = findBook
        if (findbook != null) {
            if (curPageNum <= pages) {
                FuYouHelp.fuYouHelpPost?.run {
                    queryPageReadFeel(
                        viewModelScope,
                        curPageNum,
                        pageSize,
                        FyFeel(findId = findbook.id, type = 1)
                    )
                        .onSuccess {
                            val feelList = it.list
                            pages = it.pages
                            if (feelList != null) {
                                booksData.postValue(feelList!!)
                                curPageNum++
                            }
                        }.onError {
                            it.printOnDebug()
                            errorLiveData.postValue(it.stackTraceStr)
                        }
                }
            }
        }
    }

    fun tenderBook(feel: FyFeel, binding: ItemReadfeelFindBinding) {
        FuYouHelp.fuYouHelpPost?.run {
            tenderBook(
                viewModelScope, FeelBehave(
                    feel.id!!, "5", timeCount
                )
            ).onSuccess {
                if (it.novelName != null) {
                    binding.novelName.setText(it.novelName)
                }

                feel.novelUrl = it.novelUrl!!
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
                    originOrder = feelSource.customOrder
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

}