package com.google.gson.annotations

annotation class SerializedName(
    val value: String,
    val alternate: Array<String> = [],
)
