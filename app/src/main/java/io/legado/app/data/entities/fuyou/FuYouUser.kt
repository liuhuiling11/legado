package io.legado.app.data.entities.fuyou

data class FuYouUser(
    val username: String,
    val password: String,
    val token: String?,
    val access_token: String,
    val userId: String?=null,
) {

}