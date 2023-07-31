package io.legado.app.data.entities.fuyou

import java.util.Date

data class FyFindbook(
    val id: Int?=null,
    val userId: String?=null,
    val content: String?=null,
    val grains: Int =100,
    val froze: Int?=0,
    var readfeelId: Int?=null,
    val numAnswer: Int?=0,
    val labels: String?=null,
    val updateTime: String?=null,
    val createTime: Date?=null,
    ) {

}