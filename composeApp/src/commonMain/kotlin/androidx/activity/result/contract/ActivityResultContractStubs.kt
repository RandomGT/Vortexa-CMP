package androidx.activity.result.contract

import android.net.Uri
import androidx.activity.result.ActivityResult

abstract class ActivityResultContract<I, O>

class PickVisualMediaRequest(val mediaType: Any? = null)

class ActivityResultContracts {
    class PickMultipleVisualMedia(
        val maxItems: Int = 1,
    ) : ActivityResultContract<PickVisualMediaRequest, List<Uri>>()

    class PickVisualMedia : ActivityResultContract<PickVisualMediaRequest, Uri?>() {
        companion object {
            val ImageOnly: Any = "image"
            val VideoOnly: Any = "video"
        }
    }

    class TakePicture : ActivityResultContract<Uri, Boolean>()
    class StartActivityForResult : ActivityResultContract<android.content.Intent, ActivityResult>()
    class RequestMultiplePermissions : ActivityResultContract<Array<String>, Map<String, Boolean>>()
}
