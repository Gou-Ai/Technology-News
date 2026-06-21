package com.compuspulse.data.remote.model

class ProjectCategory() {
    private var id: Int = 0
    private var name: String? = null

    constructor(id: Int, name: String?) : this() {
        this.id = id
        this.name = name
    }

    fun getId(): Int = id

    fun getName(): String = name ?: ""
}
