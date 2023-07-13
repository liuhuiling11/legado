package io.legado.app.ui.main.findbook

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.fuyou.FyFindbook
import io.legado.app.databinding.ItemFuyouFindBinding
import io.legado.app.utils.StringUtils
import kotlinx.coroutines.CoroutineScope


class FindBookAdapter(context: Context, val callback: FindBookAdapter.Callback) :
    RecyclerAdapter<FyFindbook, ItemFuyouFindBinding>(context){

    private val recyclerPool = RecyclerView.RecycledViewPool()
    private var pages:Int=0
    private var curPageNum:Int=1
    private val pageSize: Int = 10
    private var hasMore:Boolean=false
    override fun getViewBinding(parent: ViewGroup): ItemFuyouFindBinding {
        return ItemFuyouFindBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemFuyouFindBinding,
        item: FyFindbook,
        payloads: MutableList<Any>
    ) {
        val bundle = payloads.getOrNull(0) as? String
        if (bundle == null) {
            bind(binding, item)
        } else {
            bindChange(binding, item, bundle)
        }

    }

    private fun bindChange(binding: ItemFuyouFindBinding, item: FyFindbook, bundle: String) {
        binding.run {
            when (bundle) {
                "isInCommentList" -> {
                    tvAnswers.text = item.numAnswer.toString()
                }
            }

        }
    }

    private fun bind(
        binding: ItemFuyouFindBinding,
        item: FyFindbook
    ) {
        binding.tvContent.text = item.content
        binding.tvCreateTime.text =
            StringUtils.dateConvert(item.createTime)
        binding.tvUserName.text = StringUtils.getUserName(item.userId!!)
        binding.tvAnswers.text = item.numAnswer.toString()
        binding.tvGrains.text=item.grains.toString()
        if (item.readfeelId!=null) {
            binding.tvHadBestAnswer.text="已有最佳答案"
            binding.tvHadBestAnswer.setTextColor(Color.parseColor("#d3321b"))
        }
    }

    @SuppressLint("SetTextI18n")
    override fun registerListener(holder: ItemViewHolder, binding: ItemFuyouFindBinding) {

        binding.run {
            var i=0;
            tvFuyouFind.setOnClickListener{
                //点击查看找书贴

            }
        }
    }




    interface Callback {
        val scope: CoroutineScope
    }


}