package io.legado.app.ui.main.findbook

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.fuyou.FyFindbook
import io.legado.app.databinding.ItemFuyouFindBinding
import io.legado.app.utils.StringUtils
import io.legado.app.utils.gone
import io.legado.app.utils.invisible
import io.legado.app.utils.visible
import kotlinx.coroutines.CoroutineScope


class FindBookAdapter(context: Context, val callback: FindBookAdapter.Callback) :
    RecyclerAdapter<FyFindbook, ItemFuyouFindBinding>(context){
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
                    tvAnswers.text = item.numAnswer.toString()+" 答"
                }
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun bind(
        binding: ItemFuyouFindBinding,
        item: FyFindbook
    ) {
        binding.tvContent.text = item.content
        binding.tvCreateTime.text =
            StringUtils.dateConvert(item.createTime)
        binding.tvUserName.text = StringUtils.getUserName(item.userId!!)
        binding.tvAnswers.text = "${item.numAnswer} 答"
        if (item.labels != null && item.labels != "") {
            val kinds = item.labels!!.split(" ")
            if (kinds.isEmpty()) {
                binding.tvLabels.gone()
            } else {
                binding.tvLabels.visible()
                binding.tvLabels.setLabels(kinds)
            }
        }
        val multiplyNum = item.grains / 10
        when (multiplyNum){
            1 ->binding.tvMultiply.invisible()
            5 ->binding.tvMultiply.text="五倍"
            10 ->binding.tvMultiply.text="十倍"
            else -> binding.tvMultiply.text="X ${multiplyNum}"
        }
        binding.tvGrains.text=item.grains.toString()
        if (item.readfeelId!=null) {
            binding.tvHadBestAnswer.text="已有最佳答案"
            binding.tvHadBestAnswer.setTextColor(Color.parseColor("#d3321b"))
        }
    }

    @SuppressLint("SetTextI18n")
    override fun registerListener(holder: ItemViewHolder, binding: ItemFuyouFindBinding) {

        binding.run {
            tvFuyouFind.setOnClickListener{
                //点击查看找书贴
                getItem(holder.layoutPosition)?.let {
                    callback.openAnswers(it.id!!,it.content!!,it.readfeelId,it.userId)
                }
            }
        }
    }




    interface Callback {
        val scope: CoroutineScope
        fun openAnswers(findId:Int, findContent:String, bestAnswerId:Int?, findUserId: String?)
    }


}