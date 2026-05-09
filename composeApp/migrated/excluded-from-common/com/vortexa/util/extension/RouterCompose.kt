package com.vortexa.util.extension

import android.content.Context
import android.content.Intent
import android.os.Bundle


fun Context.routeToPage( clz: Class<*>, bundle: Bundle = Bundle()) {
    val intent = Intent(this, clz)
    intent.putExtras(bundle)
    this.startActivity(intent)
}


