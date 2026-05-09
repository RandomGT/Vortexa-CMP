package android.util

object Log {
    fun d(tag: String, msg: String): Int = print("D", tag, msg, null)
    fun d(tag: String, msg: String, tr: Throwable?): Int = print("D", tag, msg, tr)
    fun i(tag: String, msg: String): Int = print("I", tag, msg, null)
    fun i(tag: String, msg: String, tr: Throwable?): Int = print("I", tag, msg, tr)
    fun w(tag: String, msg: String): Int = print("W", tag, msg, null)
    fun w(tag: String, msg: String, tr: Throwable?): Int = print("W", tag, msg, tr)
    fun e(tag: String, msg: String): Int = print("E", tag, msg, null)
    fun e(tag: String, msg: String, tr: Throwable?): Int = print("E", tag, msg, tr)

    private fun print(level: String, tag: String, msg: String, tr: Throwable?): Int {
        val suffix = tr?.let { "\n${it.stackTraceToString()}" }.orEmpty()
        writePlatformLog("$level/$tag: $msg$suffix")
        return 0
    }
}
