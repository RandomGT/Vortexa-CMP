package java.io

class File private constructor(private val path: String) {
    override fun toString(): String = path

    companion object {
        fun createTempFile(prefix: String, suffix: String, directory: Any?): File =
            File(prefix + suffix)
    }
}

