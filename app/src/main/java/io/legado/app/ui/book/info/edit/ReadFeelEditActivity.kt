package io.legado.app.ui.book.info.edit

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import io.legado.app.R
import io.legado.app.base.VMBaseActivity
import io.legado.app.constant.BookType
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookSource
import io.legado.app.databinding.ActivityReadFeelEditBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.help.book.isLocal
import io.legado.app.utils.DebugLog
import io.legado.app.utils.GSON
import io.legado.app.utils.viewbindingdelegate.viewBinding

class ReadFeelEditActivity :
    VMBaseActivity<ActivityReadFeelEditBinding, ReadFeelEditViewModel>(fullScreen = false) {

    override val binding by viewBinding(ActivityReadFeelEditBinding::inflate)
    override val viewModel by viewModels<ReadFeelEditViewModel>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        viewModel.bookData.observe(this) { upView(it) }
        if (viewModel.bookData.value == null) {
            intent.getStringExtra("bookUrl")?.let {
                viewModel.loadBook(it)
            }
        }

    }

    override fun onCompatCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.book_info_edit, menu)
        return super.onCompatCreateOptionsMenu(menu)
    }

    override fun onCompatOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> publishData()
        }
        return super.onCompatOptionsItemSelected(item)
    }

    private fun upView(book: Book) = binding.run {
        tieBookName.setText(book.name)
        tieBookAuthor.setText(book.author)

        upCover()
    }

    private fun upCover() {
        viewModel.book.let {
            binding.ivCover.load(it?.getDisplayCover(), it?.name, it?.author)
        }
    }

    private fun publishData() = binding.run {
        viewModel.book?.let { book ->
            if (!book.isLocal) {
                val source = appDb.bookSourceDao.getBookSource(book.origin)
                if (source != null) {
                    FuYouHelp.fuYouHelpPost?.run {
                        publishFeel(lifecycleScope, FuYouHelp.ReadFeel(
                            novelName=book.name,
                            novelUrl=book.bookUrl,
                            novelAuthor=book.author,
                            novelPhoto=book.coverUrl,
                            content=tieReadFeel.text?.toString(),
                            sourceJson= GSON.toJson(source),
                            tocUrl=book.tocUrl,
                            intro = book.intro,
                            origin = book.origin
                        )).onSuccess {
                            DebugLog.i(javaClass.name,"发布读后感成功！id：${it.id}")
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }
                }
            }
        }
        setResult(Activity.RESULT_FIRST_USER)
        finish()
    }


}