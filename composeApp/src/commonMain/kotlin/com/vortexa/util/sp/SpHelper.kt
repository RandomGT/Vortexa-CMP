package com.vortexa.util.sp

import com.russhwolf.settings.Settings
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

object SpHelper {
    private const val STRING_SET_PREFIX = "__string_set__:"

    private val settings: Settings by lazy { Settings() }
    private val json = Json

    fun getString(key: String, defValue: String = ""): String = settings.getString(key, defValue)
    fun getInt(key: String, defValue: Int = 0): Int = settings.getInt(key, defValue)
    fun getLong(key: String, defValue: Long = 0L): Long = settings.getLong(key, defValue)
    fun getFloat(key: String, defValue: Float = 0f): Float = settings.getFloat(key, defValue)
    fun getBoolean(key: String, defValue: Boolean = false): Boolean = settings.getBoolean(key, defValue)

    fun getStringSet(key: String, defValue: Set<String> = emptySet()): Set<String> =
        runCatching {
            val raw = settings.getString(key, "")
            if (raw.startsWith(STRING_SET_PREFIX)) {
                json.decodeFromString(ListSerializer(String.serializer()), raw.removePrefix(STRING_SET_PREFIX)).toSet()
            } else {
                defValue
            }
        }.getOrDefault(defValue)

    fun putString(key: String, value: String?) {
        if (value == null) {
            settings.remove(key)
        } else {
            settings.putString(key, value)
        }
    }
    fun putInt(key: String, value: Int) { settings.putInt(key, value) }
    fun putLong(key: String, value: Long) { settings.putLong(key, value) }
    fun putFloat(key: String, value: Float) { settings.putFloat(key, value) }
    fun putBoolean(key: String, value: Boolean) { settings.putBoolean(key, value) }
    fun putStringSet(key: String, value: Set<String>) {
        settings.putString(
            key,
            STRING_SET_PREFIX + json.encodeToString(ListSerializer(String.serializer()), value.toList())
        )
    }
    fun remove(key: String) { settings.remove(key) }
    fun clear() { settings.clear() }

    fun putAll(block: Writer.() -> Unit) {
        Writer().block()
    }

    class Writer {
        fun putString(key: String, value: String?) { SpHelper.putString(key, value) }
        fun putInt(key: String, value: Int) { SpHelper.putInt(key, value) }
        fun putLong(key: String, value: Long) { SpHelper.putLong(key, value) }
        fun putFloat(key: String, value: Float) { SpHelper.putFloat(key, value) }
        fun putBoolean(key: String, value: Boolean) { SpHelper.putBoolean(key, value) }
        fun putStringSet(key: String, value: Set<String>) { SpHelper.putStringSet(key, value) }
    }
}
