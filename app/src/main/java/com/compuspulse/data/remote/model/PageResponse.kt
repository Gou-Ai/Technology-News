package com.compuspulse.data.remote.model

class PageResponse<T> {
    private var curPage: Int = 0
    private var datas: List<T>? = null
    private var offset: Int = 0
    private var over: Boolean = false
    private var pageCount: Int = 0
    private var size: Int = 0
    private var total: Int = 0

    fun getCurPage(): Int = curPage

    fun getDatas(): List<T> = datas ?: ArrayList()

    fun getOffset(): Int = offset

    fun isOver(): Boolean = over

    fun getPageCount(): Int = pageCount

    fun getSize(): Int = size

    fun getTotal(): Int = total
}
