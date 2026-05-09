package com.vortexa.ui.page.search

class SearchHistoryRepository(context: Any? = null) {
    private val items = mutableListOf<String>()

    fun getHistory(): List<String> = items.toList()

    fun add(keyword: String) {
        val value = keyword.trim()
        if (value.isEmpty()) return
        items.remove(value)
        items.add(0, value)
    }

    fun clear() {
        items.clear()
    }
}

