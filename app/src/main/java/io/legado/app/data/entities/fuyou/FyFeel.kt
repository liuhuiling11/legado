package io.legado.app.data.entities.fuyou

import java.util.Date

data class FyFeel(
    val id: Int?=null,
    val userId: String?=null,
    val novelName: String?=null,
    val novelUrl: String?=null,
    val novelAuthor: String?=null,
    val novelPhoto: String?=null,
    val content: String?=null,
    val labels: String?=null,
    val updateTime: String?=null,
    val createTime: Date?=null,
    val sourceJson: String?=null,
    var listChapterUrl: String?=null,
    val novelIntroduction: String?=null,
    val source: String?=null,
    val commentUser: String?=null,
    val commentContent: String?=null,
    val findId:Int?=null,
    val type:Int?=0
) {

}