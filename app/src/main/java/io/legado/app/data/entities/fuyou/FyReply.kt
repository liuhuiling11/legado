package io.legado.app.data.entities.fuyou

import java.util.Date

data class FyReply(
    val id: Int?=null,
    val commentId: Int?=-1,
    val userId: String?="",
    val content: String?="",
    val fatherId: Int?=null,
    val timeCount: Int?=0,
    var createTime: Date?=null,
    val like:Int?=0,
    val replay:Int?=0
)