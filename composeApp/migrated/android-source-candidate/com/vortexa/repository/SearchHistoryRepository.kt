package com.vortexa.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray

/**
 * 搜索历史本地持久化，使用 SharedPreferences 存储，最多 [MAX_SIZE] 条，FIFO 淘汰。
 *
 * @param context 用于获取 SharedPreferences，建议使用 applicationContext
 */
class SearchHistoryRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * 获取全部搜索历史（按时间从新到旧）。
     *
     * @return 历史记录列表，最新一条在索引 0
     */
    fun getHistory(): List<String> {
        return try {
            val json = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
            val arr = JSONArray(json)
            List(arr.length()) { arr.getString(it) }.reversed()
        } catch (e: Exception) {
            Log.e(TAG, "getHistory parse error", e)
            emptyList()
        }
    }

    /**
     * 添加一条搜索记录。若已存在则先移除再插入到最新；超过 [MAX_SIZE] 时删除最旧的。
     *
     * @param keyword 搜索关键词，会做 trim，空串不写入
     */
    fun add(keyword: String) {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) return
        val list = getHistory().reversed().toMutableList() // 旧到新
        list.remove(trimmed)
        list.add(trimmed)
        val toSave = list.takeLast(MAX_SIZE)
        val arr = JSONArray(toSave)
        prefs.edit().putString(KEY_HISTORY, arr.toString()).apply()
        Log.d(TAG, "add history: $trimmed, size=${toSave.size}")
    }

    /**
     * 清空搜索历史。
     */
    fun clear() {
        prefs.edit().remove(KEY_HISTORY).apply()
        Log.d(TAG, "clear history")
    }

    companion object {
        private const val TAG = "SearchHistoryRepo"
        private const val PREFS_NAME = "search_history_prefs"
        private const val KEY_HISTORY = "history"
        private const val MAX_SIZE = 20
    }
}
