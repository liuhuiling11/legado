package io.legado.app.data.entities.fuyou

data class FyComment(
    val id: Int?=null,
    val readfeelId: Int?=null,
    val userId: String?="",
    val content: String?="",
    val timeCount: Int?=0,
) {

}