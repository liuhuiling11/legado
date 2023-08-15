package io.legado.app.ui.book.findbook.message.read

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.legado.app.base.BaseViewModel
import io.legado.app.data.entities.fuyou.FyMessageFeel
import io.legado.app.help.FuYouHelp
import io.legado.app.utils.printOnDebug
import io.legado.app.utils.stackTraceStr


class FuyouMessageReadViewModel(application: Application) : BaseViewModel(application) {
    val booksData = MutableLiveData<List<FyMessageFeel>>()
    val errorLiveData = MutableLiveData<String>()
    private var pages: Int = 1
    private val pageSize: Int = 20
    private var curPageNum = 1
    fun initData() {
        execute {
            queryPageMessageRead()
        }
    }

    fun hasNextPage(): Boolean {
        return curPageNum <= pages
    }

    fun queryPageMessageRead() {

        FuYouHelp.fuYouHelpPost?.run {
            queryPageMessageRead(
                viewModelScope,
                curPageNum,
                pageSize
            )
                .onSuccess {
                    val feelList = it.list ?: ArrayList<FyMessageFeel>()
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