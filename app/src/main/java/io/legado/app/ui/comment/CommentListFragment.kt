package io.legado.app.ui.comment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.data.entities.fuyou.FyComment
import io.legado.app.databinding.DialogCommentViewBinding
import io.legado.app.help.FuYouHelp
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.lib.theme.primaryColor
import io.legado.app.ui.widget.recycler.UpLinearLayoutManager
import io.legado.app.ui.widget.recycler.VerticalDivider
import io.legado.app.utils.DebugLog
import io.legado.app.utils.hideSoftInput
import io.legado.app.utils.setEdgeEffectColor
import io.legado.app.utils.setLayout
import io.legado.app.utils.toastOnUi
import io.legado.app.utils.viewbindingdelegate.viewBinding
import kotlinx.coroutines.Dispatchers
import splitties.init.appCtx


class CommentListFragment() : BaseDialogFragment(R.layout.dialog_comment_view),
    CommentAdapter.Callback{
    private val adapter by lazy { CommentAdapter(requireContext(), this) }
    private val mLayoutManager by lazy { UpLinearLayoutManager(requireContext()) }
    private val binding by viewBinding(DialogCommentViewBinding::bind)
    private var pageNum:Int=0
    private val pageSize:Int=20
    private var readFeelId:Int=-1
    private var pages:Int=0
    private var isLoadingMore:Boolean=true

    constructor(
        readFeelId: String,
        timeCount: Int
    ) : this() {
        arguments = Bundle().apply {
            putString("readFeelId", readFeelId)
            putInt("timeCount", timeCount)

        }
        isCancelable = false
    }

    override fun onStart() {
        super.onStart()
        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 0.9f)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        val timeCount = arguments?.getInt("timeCount")
        readFeelId = requireArguments().getInt("readFeelId")

        binding.recyclerView.setEdgeEffectColor(primaryColor)
        binding.recyclerView.layoutManager = mLayoutManager
        binding.recyclerView.addItemDecoration(VerticalDivider(requireContext()))
        binding.recyclerView.adapter=adapter
        //1,初始化请求评论列表
        queryPageComment(readFeelId, pageNum, pageSize)

        //2，下滑事件分页查询
//        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                //下滑调用分页查询
////                StaggeredGridLayoutManager layoutManager = null ;
////                if(recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager){
////                    layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
////                }else{
////                    return;
////                }
////                int[] positions = null;
////                int[] into = layoutManager.findLastCompletelyVisibleItemPositions(positions);
////                int[] firstInto = layoutManager.findFirstVisibleItemPositions(positions);
////                int lastPositon = Math.max(into[0],into[1]);
////                int firstPositon = Math.max(firstInto[0],firstInto[1]);
////
////                if(!isLoadingMore && dy>0 && layoutManager.getItemCount()-lastPositon<=TOLAST) {
////                    //load more
////                    isLoadingMore = true;
////                }
//
//                if (!recyclerView.canScrollVertically(1)) {
//                    val layoutManager = recyclerView.getLayoutManager();
//                    nextPage()
//                }
//            }
//        })
        //3,发送评论
        binding.sendComment.setOnClickListener {
            binding.tieMyComment.clearFocus()
            binding.tieMyComment.hideSoftInput()
            Coroutine.async(this, Dispatchers.IO) {
                FuYouHelp.fuYouHelpPost?.run {
                    publishComment(
                        lifecycleScope, FyComment(
                            readfeelId = id,
                            content = binding.tieMyComment.text!!.toString(),
                            timeCount = timeCount
                        )
                    ).onSuccess {
                        DebugLog.i(javaClass.name, "评论发布成功！id：${it.id}")
                        binding.tieMyComment.text!!.clear()
                        appCtx.toastOnUi("评论发送成功")
                    }.onError {
                        appCtx.toastOnUi("评论发送失败" + it.localizedMessage)
                    }
                }
            }

        }
    }

    /**
     * 分页请求评论列表
     */
    fun queryPageComment(feelId:Int,pageNum:Int,pageSize:Int){
        Coroutine.async(this, Dispatchers.IO) {
            FuYouHelp.fuYouHelpPost?.run {
                queryPageComment(lifecycleScope, feelId, pageNum, pageSize)
                    .onSuccess {
                        pages=it.pages!!
                        adapter.addItems(it.list!!)
                    }
            }
        }
    }

    override fun nextPage() {
        pageNum=pageNum+1
        if (isLoadingMore && pages>pageNum) {
            queryPageComment(readFeelId, pageNum, pageSize)
        }else{
            isLoadingMore=false
            appCtx.toastOnUi("没有更多了")
        }
    }


}


