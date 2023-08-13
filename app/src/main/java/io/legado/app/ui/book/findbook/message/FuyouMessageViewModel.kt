package io.legado.app.ui.book.findbook.message

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.legado.app.base.BaseViewModel
import io.legado.app.data.entities.fuyou.FyFindbook
import io.legado.app.data.entities.fuyou.FyMessage
import io.legado.app.help.FuYouHelp
import io.legado.app.utils.printOnDebug
import io.legado.app.utils.stackTraceStr


class FuyouMessageViewModel(application: Application) : BaseViewModel(application) {
    val booksData = MutableLiveData<List<FyMessage>>()
    val errorLiveData = MutableLiveData<String>()
    private var findBook: FyFindbook? = null
    private var pages: Int = 1
    private val pageSize: Int = 20
    private var curPageNum = 1
    var bestAnswer: FyMessage? = null
    fun initData(findId: Int, findContent: String) {
        execute {
            if (findBook == null && findId != 0) {
                findBook = FyFindbook(id = findId, content = findContent)
            }
            queryPageMessage()
        }
    }

    fun hasNextPage(): Boolean {
        return curPageNum <= pages
    }

    fun queryPageMessage() {
        val findbook = findBook
        if (findbook != null) {
            FuYouHelp.fuYouHelpPost?.run {
                queryPageMessage(
                    viewModelScope,
                    curPageNum,
                    pageSize,
                    FyMessage()
                )
                    .onSuccess {
                        val feelList = it.list ?: ArrayList<FyMessage>()
                        pages = it.pages
                        booksData.postValue(feelList)
                        if (curPageNum < pages) {
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