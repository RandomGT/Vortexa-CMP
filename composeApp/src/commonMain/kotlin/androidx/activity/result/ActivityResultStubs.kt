package androidx.activity.result

class ActivityResult(val resultCode: Int)

class ActivityResultLauncher<I> {
    fun launch(input: I) {}
}

