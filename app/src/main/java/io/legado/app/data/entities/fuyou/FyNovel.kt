package io.legado.app.data.entities.fuyou

data class FyNovel(
    val novelName: String,
    val novelAuthor: String,
    val novelIntroduction: String?,
    val novelUrl: String,
    val novelPhoto: String?,
    val listChapterUrl: String?,
    val labels: String?,
    val originType:Number?
) {

}