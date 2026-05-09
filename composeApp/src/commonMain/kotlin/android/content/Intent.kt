package android.content

import kotlin.reflect.KClass

class Intent(
    val context: Context? = null,
    val target: Any? = null,
) {
    var flags: Int = 0

    fun putExtra(name: String, value: Boolean): Intent = this
    fun putExtra(name: String, value: String): Intent = this
    fun putExtra(name: String, value: Long): Intent = this
    fun putExtra(name: String, value: Int): Intent = this
    fun putExtra(name: String, value: Enum<*>): Intent = this
    fun getBooleanExtra(name: String, defaultValue: Boolean): Boolean = defaultValue
    fun getStringExtra(name: String): String? = null
    fun getLongExtra(name: String, defaultValue: Long): Long = defaultValue
    fun getIntExtra(name: String, defaultValue: Int): Int = defaultValue
    fun addFlags(flags: Int): Intent {
        this.flags = this.flags or flags
        return this
    }

    companion object {
        const val FLAG_ACTIVITY_CLEAR_TOP: Int = 1
        const val FLAG_ACTIVITY_SINGLE_TOP: Int = 1 shl 1
        const val FLAG_ACTIVITY_NEW_TASK: Int = 1 shl 2
        const val FLAG_ACTIVITY_CLEAR_TASK: Int = 1 shl 3
    }
}
