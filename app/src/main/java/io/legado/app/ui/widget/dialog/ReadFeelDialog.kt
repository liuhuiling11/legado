package io.legado.app.ui.widget.dialog

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.databinding.DialogReadfeelViewBinding
import io.legado.app.databinding.DialogTextViewBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.lib.theme.primaryColor
import io.legado.app.ui.book.info.BookInfoActivity
import io.legado.app.utils.DebugLog
import io.legado.app.utils.applyTint
import io.legado.app.utils.setHtml
import io.legado.app.utils.setLayout
import io.legado.app.utils.startActivity
import io.legado.app.utils.viewbindingdelegate.viewBinding
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import splitties.views.onClick


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
            val id = ait.getInt("id")
            val timeCount = ait.getInt("timeCount")
            binding.textView.text = content

            binding.tenderBook.setOnClickListener{
                DebugLog.i(javaClass.name,"蜉蝣采书")
                FuYouHelp.fuYouHelpPost?.run {
                    tenderBook(
                        lifecycleScope, FuYouHelp.FeelBehave(
                            id, "5", timeCount)
                    ).onSuccess{
                        if (it.novelName!=null) {
                            binding.novelName.setText(it.novelName)
                        }
                        author=it.novelAuthor!!
                        name=it.novelName!!
                        url=it.novelUrl!!
                        binding.novelAuthor.setText(it.novelAuthor)
                        binding.novelUrl.isVisible=true
                        binding.comment.isVisible=true
                    }
                }
            }

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
