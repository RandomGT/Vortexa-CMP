package com.vortexa.net.auth

import com.vortexa.lib_net.exception.LoginRequiredException

fun Throwable.isLoginRequired(): Boolean =
    this is LoginRequiredException || cause is LoginRequiredException
