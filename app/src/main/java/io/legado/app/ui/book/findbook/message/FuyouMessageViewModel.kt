package io.legado.app.ui.book.findbook.message

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.legado.app.base.BaseViewModel
import io.legado.app.data.appDb
import io.legado.app.data.entities.FyParams
import io.legado.app.data.entities.fuyou.FyMessage
import io.legado.app.help.FuYouHelp
import io.legado.app.utils.StringUtils
import io.legado.app.utils.printOnDebug
import io.legado.app.utils.stackTraceStr
import java.util.Date


class FuyouMessageViewModel(application: Application) : BaseViewModel(application) {
    val booksData = MutableLiveData<List<FyMessage>>()
    val errorLiveData = MutableLiveData<String>()
    val likeNum = MutableLiveData<Int>()
    val tenderNum = MutableLiveData<Int>()
    val readNum = MutableLiveData<Int>()
    private var pages: Int = 1
    private val pageSize: Int = 20
    private var curPageNum = 1
    private val LAST_LIKE_MESSATE_TIME_TAG = "lastLikeMessageTime"
    private val LAST_TENDER_MESSATE_TIME_TAG = "lastTenderMessageTime"
    private val LAST_READ_MESSATE_TIME_TAG = "lastReadMessageTime"

    fun initData() {
        execute {
            initFyParams()
            queryPageMessage()
        }
    }

    /**
     * 初始化消息参数
     */
    private fun initFyParams() {

        initNumMessage()
    }

    private fun getTimeParams(timeTypeTag:String):String {
        val value = appDb.fyParamsDao.get(timeTypeTag)?.value
        return if (value == null) {
            val dateFormat = StringUtils.dateFormat(Date())
            appDb.fyParamsDao.insert(
                FyParams(
                    name = timeTypeTag,
                    value = dateFormat,
                    info = "消息最近查看时间",
                    type = "Date"
                )
            )
            dateFormat
        } else {
            value
        }
    }

    /**
     * 查询数量消息
     */
    private fun initNumMessage() {
        //采纳消息数
        initTenderNum()

        //阅读消息数
        initReadNum()

        //点赞消息数
        initLikeNum()
    }

    private fun initTenderNum() {
        FuYouHelp.fuYouHelpPost?.run {
            getMessageNum(
                viewModelScope,
                getTimeParams(LAST_TENDER_MESSATE_TIME_TAG),
                5
            ).onSuccess {
                appDb.fyParamsDao.updateValue(LAST_TENDER_MESSATE_TIME_TAG,StringUtils.dateFormat(Date()))
                tenderNum.postValue(it)
            }
        }
    }

    private fun initReadNum() {
        FuYouHelp.fuYouHelpPost?.run {
            getMessageNum(
                viewModelScope,
                getTimeParams(LAST_READ_MESSATE_TIME_TAG),
                1
            ).onSuccess {
                appDb.fyParamsDao.updateValue(LAST_READ_MESSATE_TIME_TAG,StringUtils.dateFormat(Date()))
                readNum.postValue(it)
            }
        }
    }

    private fun initLikeNum() {
        FuYouHelp.fuYouHelpPost?.run {
            getMessageNum(
                viewModelScope,
                getTimeParams(LAST_LIKE_MESSATE_TIME_TAG),
                3
            ).onSuccess {
                appDb.fyParamsDao.updateValue(LAST_LIKE_MESSATE_TIME_TAG,StringUtils.dateFormat(Date()))
                likeNum.postValue(it)
            }
        }
    }

    fun hasNextPage(): Boolean {
        return curPageNum <= pages
    }

    fun queryPageMessage() {
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