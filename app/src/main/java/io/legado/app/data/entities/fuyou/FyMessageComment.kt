package io.legado.app.data.entities.fuyou

import java.util.Date

data class FyMessageComment(
    val id: Int?=null,
    val content: String?=null,
    var updateTime: Date?=null,
    val commentNum:Int=0,
    val likeNum:Int=0,
)