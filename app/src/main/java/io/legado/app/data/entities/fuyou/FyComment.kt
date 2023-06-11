package io.legado.app.data.entities.fuyou

import java.util.Date

data class FyComment(
    val id: Int?=null,
    val readfeelId: Int?=-1,
    val userId: String?="",
    val content: String?="",
    val timeCount: Int?=0,
    val createTime: Date?=null,
    val like:Int?=0,
    val replay:Int?=0
) {
}