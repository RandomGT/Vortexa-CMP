package android.content

open class Context {
    val applicationContext: Context get() = this
    val cacheDir: Any? get() = null
    val resources: Resources get() = Resources

    fun startActivity(intent: Intent) {}
    fun getSystemService(name: String): Any? = null
    fun finish() {}

    companion object {
        const val AUDIO_SERVICE: String = "audio"
    }
}

object Resources {
    val displayMetrics: DisplayMetrics = DisplayMetrics
}

object DisplayMetrics {
    val density: Float = 1f
    val widthPixels: Int = 390
}
