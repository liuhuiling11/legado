package io.legado.app.data.entities.fuyou

import java.util.Date

data class FyMessageFeel(
    val id: Int?=null,
    val feelId: Int?=null,
    val userId: String?="",
    var novelName: String?=null,
    var novelUrl: String?=null,
    var novelAuthor: String?=null,
    val novelPhoto: String?=null,
    val content: String?=null,
    var createTime: Date?=null,
    var updateTime: Date?=null,
    val numComment:Int=0,
    val numTender:Int=0,
    val numRead:Int=0,
)