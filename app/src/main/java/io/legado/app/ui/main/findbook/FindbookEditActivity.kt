package io.legado.app.ui.main.findbook

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import io.legado.app.R
import io.legado.app.base.VMBaseActivity
import io.legado.app.data.entities.fuyou.FyFindbook
import io.legado.app.databinding.ActivityFindbookEditBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.utils.DebugLog
import io.legado.app.utils.hideSoftInput
import io.legado.app.utils.toastOnUi
import io.legado.app.utils.viewbindingdelegate.viewBinding

class FindbookEditActivity :
    VMBaseActivity<ActivityFindbookEditBinding, FindBookViewModel>(fullScreen = false) {

    override val binding by viewBinding(ActivityFindbookEditBinding::inflate)


    override val viewModel by viewModels<FindBookViewModel>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
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

    private fun publishData() = binding.run {
        tieFindbook.clearFocus()
        tieFindbook.hideSoftInput()
        FuYouHelp.fuYouHelpPost?.run {
            publishFindBook(lifecycleScope, FyFindbook(
                content =tieFindbook.text?.toString(),
                grains = tvGrains.text.toString().toInt(),
                labels = lbKind.lableList.joinToString(" ")
            )
            ).onSuccess {
                DebugLog.i(javaClass.name,"发布找书贴成功！id：${it.id}")
                setResult(Activity.RESULT_OK)
                finish()
            }.onError {
                DebugLog.i(javaClass.name,"发布找书贴失败！msg：${it.localizedMessage}")
                viewModel.context.toastOnUi("找书贴发布失败！${it.localizedMessage}")
            }
        }

    }


}