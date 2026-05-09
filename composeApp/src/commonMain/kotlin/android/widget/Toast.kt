package android.widget

import android.content.Context

class Toast {
    fun show() {}

    companion object {
        const val LENGTH_SHORT: Int = 0
        const val LENGTH_LONG: Int = 1
        fun makeText(context: Context?, text: CharSequence?, duration: Int): Toast = Toast()
    }
}

