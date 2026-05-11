package com.vortexa.ui.page.search

import android.util.Log
import com.vortexa.util.sp.SpHelper
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class SearchHistoryRepository(context: Any? = null) {
    private val serializer = ListSerializer(String.serializer())

    fun getHistory(): List<String> =
        runCatching {
            Json.decodeFromString(serializer, SpHelper.getString(KEY_HISTORY, "[]"))
        }.onFailure {
            Log.e(TAG, "getHistory parse error", it)
        }.getOrDefault(emptyList())

    fun add(keyword: String) {
        val value = keyword.trim()
        if (value.isEmpty()) return
        val items = getHistory().toMutableList()
        items.remove(value)
        items.add(0, value)
        SpHelper.putString(KEY_HISTORY, Json.encodeToString(serializer, items.take(MAX_SIZE)))
        Log.d(TAG, "add history: $value")
    }

    fun clear() {
        SpHelper.remove(KEY_HISTORY)
        Log.d(TAG, "clear history")
    }

    private companion object {
        const val TAG = "SearchHistoryRepo"
        const val KEY_HISTORY = "search_history.history"
        const val MAX_SIZE = 20
    }
}
