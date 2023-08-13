package io.legado.app.data.entities.fuyou

import java.util.Date

data class FyFeel(
    val id: Int?=null,
    val userId: String?=null,
    var novelName: String?=null,
    var novelUrl: String?=null,
    var novelAuthor: String?=null,
    val novelPhoto: String?=null,
    val content: String?=null,
    val labels: String?=null,
    val updateTime: String?=null,
    val createTime: Date?=null,
    val sourceJson: String?=null,
    var listChapterUrl: String="",
    val novelIntroduction: String?=null,
    val source: String?=null,
    val commentUser: String?=null,
    val commentContent: String?=null,
    val findId:Int?=null,
    val type:Int?=0,
    val commentNum:Int=0,
    val tenderNum:Int=0
) {

}