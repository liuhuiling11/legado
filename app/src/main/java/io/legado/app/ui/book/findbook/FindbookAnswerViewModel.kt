package io.legado.app.ui.book.findbook

import android.app.Application
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.legado.app.base.BaseViewModel
import io.legado.app.data.entities.fuyou.FyFeel
import io.legado.app.data.entities.fuyou.FyFindbook
import io.legado.app.help.FuYouHelp
import io.legado.app.utils.printOnDebug
import io.legado.app.utils.stackTraceStr


class FindbookAnswerViewModel(application: Application) : BaseViewModel(application) {
    val booksData = MutableLiveData<List<FyFeel>>()
    val errorLiveData = MutableLiveData<String>()
    private var findBook: FyFindbook? = null
    private var findContent: String? = null
    private var pages: Int = 1
    private val pageSize: Int = 20
    private var curPageNum = 1

    fun initData(intent: Intent) {
        execute {
            val findId = intent.getIntExtra("findId", 0)
            findContent = intent.getStringExtra("findContent")
            if (findBook == null && findId != 0) {
                findBook = FyFindbook(id = findId, content = findContent)
            }
            queryAnswer()
        }
    }

    fun queryAnswer() {
        val findbook = findBook
        if (findbook != null) {
            FuYouHelp.fuYouHelpPost?.run {
                queryPageReadFeel(
                    viewModelScope,
                    curPageNum,
                    pageSize,
                    FyFeel(findId = findbook.id, type = 1)
                )
                    .onSuccess {
                        val feelList = it.list
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