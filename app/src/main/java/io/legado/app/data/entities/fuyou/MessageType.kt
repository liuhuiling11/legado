package io.legado.app.data.entities.fuyou

enum class MessageType(
    val code:Int,
    val info:String,
) {
    ANSWER(1,"找书贴被回答"),
    COMMENT(2,"读后感被评论"),
    REPLY(3,"评论被回复"),
    AGAIN_REPLY(4,"回复被追复");

}