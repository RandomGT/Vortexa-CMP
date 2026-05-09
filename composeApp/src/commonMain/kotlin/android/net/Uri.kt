package android.net

data class Uri(private val raw: String) {
    override fun toString(): String = raw

    companion object {
        fun parse(value: String): Uri = Uri(value)
    }
}

