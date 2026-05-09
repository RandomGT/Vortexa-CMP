package androidx.core.content

import android.content.Context
import android.net.Uri

object ContextCompat {
    fun checkSelfPermission(context: Context, permission: String): Int = 0
}

object FileProvider {
    fun getUriForFile(context: Context, authority: String, file: Any): Uri = Uri.parse("")
}

