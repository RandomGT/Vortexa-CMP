package android.app

import android.content.Context
import android.content.Intent

open class Activity : Context() {
    val intent: Intent = Intent()
    fun setResult(resultCode: Int) {}

    companion object {
        const val RESULT_OK: Int = -1
    }
}
