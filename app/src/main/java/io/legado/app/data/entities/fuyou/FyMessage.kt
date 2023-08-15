package io.legado.app.data.entities.fuyou

import java.util.Date

data class FyMessage(
    val id: Int?=null,
    val myId: Int?=-1,
    val contentId: Int?=-1,
    val userId: String?="",
    val content: String?="",
    val type: Int?=0,
    var createTime: Date?=null,
)