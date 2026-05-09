package com.vortexa.util.sp

object SpHelper {
    private val values = mutableMapOf<String, Any?>()

    fun getString(key: String, defValue: String = ""): String = values[key] as? String ?: defValue
    fun getInt(key: String, defValue: Int = 0): Int = values[key] as? Int ?: defValue
    fun getLong(key: String, defValue: Long = 0L): Long = values[key] as? Long ?: defValue
    fun getFloat(key: String, defValue: Float = 0f): Float = values[key] as? Float ?: defValue
    fun getBoolean(key: String, defValue: Boolean = false): Boolean = values[key] as? Boolean ?: defValue
    fun getStringSet(key: String, defValue: Set<String> = emptySet()): Set<String> =
        (values[key] as? Set<*>)?.filterIsInstance<String>()?.toSet() ?: defValue

    fun putString(key: String, value: String?) { values[key] = value }
    fun putInt(key: String, value: Int) { values[key] = value }
    fun putLong(key: String, value: Long) { values[key] = value }
    fun putFloat(key: String, value: Float) { values[key] = value }
    fun putBoolean(key: String, value: Boolean) { values[key] = value }
    fun putStringSet(key: String, value: Set<String>) { values[key] = value }
    fun remove(key: String) { values.remove(key) }
    fun clear() { values.clear() }

    fun putAll(block: Writer.() -> Unit) {
        Writer().block()
    }

    class Writer {
        fun putString(key: String, value: String?) { values[key] = value }
        fun putInt(key: String, value: Int) { values[key] = value }
        fun putLong(key: String, value: Long) { values[key] = value }
        fun putFloat(key: String, value: Float) { values[key] = value }
        fun putBoolean(key: String, value: Boolean) { values[key] = value }
        fun putStringSet(key: String, value: Set<String>) { values[key] = value }
    }
}
