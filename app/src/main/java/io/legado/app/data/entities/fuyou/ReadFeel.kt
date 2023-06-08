package io.legado.app.data.entities.fuyou

data class ReadFeel(
    val id: Int?=null,
    val userId: String?=null,
    val novelName: String?,
    val novelUrl: String?,
    val novelAuthor: String?,
    val novelPhoto: String?,
    val content: String?,
    val labels: String?=null,
    val updateTime: String?=null,
    val sourceJson: String?,
    var listChapterUrl: String = "",
    val novelIntroduction: String?,
    val source: String?
) {

}