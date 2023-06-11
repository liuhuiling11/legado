package io.legado.app.data.entities.fuyou

data class PageResponse<T>(
    var totalCount: Int? = 0,
    var pages: Int? = 0,
    var list: List<T>? = null
){
}