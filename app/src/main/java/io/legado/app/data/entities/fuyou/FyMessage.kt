package io.legado.app.data.entities.fuyou

data class FyMessage(
    val id: Int?=null,
    val myId: Int?=-1,
    val contentId: Int?=-1,
    val commentId: Int?=-1,
    val userId: String?="",
    val content: String?="",
    val type: Int?=0,
    var createTime: String?=null,
)