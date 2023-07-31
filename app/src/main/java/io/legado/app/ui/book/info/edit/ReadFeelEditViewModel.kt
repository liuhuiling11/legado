package io.legado.app.ui.book.info.edit

import android.app.Application
import androidx.lifecycle.MutableLiveData
import io.legado.app.base.BaseViewModel
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book

class ReadFeelEditViewModel(application: Application) : BaseViewModel(application) {
    var feelType: Int=0
    var book: Book? = null
    val bookData = MutableLiveData<Book>()
    var findId:Int?=null
    fun loadBook(bookUrl: String) {
        execute {
            book = appDb.bookDao.getBook(bookUrl)
            book?.let {
                bookData.postValue(it)
            }
        }
    }

}