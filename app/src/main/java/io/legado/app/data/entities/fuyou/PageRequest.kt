package io.legado.app.data.entities.fuyou

data class PageRequest<T>(
    var pageNum: Int,
    var pageSize: Int,
    var requestVO: T
)