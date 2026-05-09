package com.vortexa.util.sp

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.vortexa.VortexaApplication

/**
 * SharedPreferences 工具类，用于快速读写、更新临时/零散数据。
 * Context 内部从 [VortexaApplication.instance] 的 applicationContext 获取，调用方无需传入。
 *
 * 使用 apply() 异步写入，避免阻塞主线程。
 *
 * @author LuXin
 */
object SpHelper {

    private const val TAG = "SpHelper"
    private const val DEFAULT_NAME = "vortexa_temp_prefs"

    @Volatile
    private var prefs: SharedPreferences? = null

    /**
     * 获取 SharedPreferences 实例，使用 applicationContext 避免内存泄漏。
     * 线程安全懒加载。
     */
    private fun getPrefs(): SharedPreferences {
        return prefs ?: synchronized(this) {
            prefs ?: run {
                val ctx = VortexaApplication.instance.applicationContext
                ctx.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE).also {
                    prefs = it
                    Log.d(TAG, "SpHelper initialized with name=$DEFAULT_NAME")
                }
            }
        }
    }

    // --------------- 读取 ---------------

    fun getString(key: String, default: String = ""): String =
        getPrefs().getString(key, default) ?: default

    fun getInt(key: String, default: Int = 0): Int =
        getPrefs().getInt(key, default)

    fun getLong(key: String, default: Long = 0L): Long =
        getPrefs().getLong(key, default)

    fun getFloat(key: String, default: Float = 0f): Float =
        getPrefs().getFloat(key, default)

    fun getBoolean(key: String, default: Boolean = false): Boolean =
        getPrefs().getBoolean(key, default)

    fun getStringSet(key: String, default: Set<String>? = null): Set<String>? =
        getPrefs().getStringSet(key, default)

    // --------------- 写入 / 更新 ---------------

    /**
     * 写入 String，异步 apply。
     */
    fun putString(key: String, value: String) {
        getPrefs().edit { putString(key, value) }
    }

    fun putInt(key: String, value: Int) {
        getPrefs().edit { putInt(key, value) }
    }

    fun putLong(key: String, value: Long) {
        getPrefs().edit { putLong(key, value) }
    }

    fun putFloat(key: String, value: Float) {
        getPrefs().edit { putFloat(key, value) }
    }

    fun putBoolean(key: String, value: Boolean) {
        getPrefs().edit { putBoolean(key, value) }
    }

    fun putStringSet(key: String, value: Set<String>?) {
        getPrefs().edit { putStringSet(key, value) }
    }

    /**
     * 批量更新：在单次 edit 内执行多个 put，减少 I/O。
     */
    fun putAll(block: SharedPreferences.Editor.() -> Unit) {
        getPrefs().edit(commit = false, block)
    }

    /**
     * 移除指定 key。
     */
    fun remove(key: String) {
        getPrefs().edit { remove(key) }
    }

    /**
     * 清空当前 SP 文件内所有数据。
     */
    fun clear() {
        getPrefs().edit { clear() }
        Log.d(TAG, "SpHelper cleared")
    }

    /**
     * 是否包含 key。
     */
    fun contains(key: String): Boolean = getPrefs().contains(key)
}
