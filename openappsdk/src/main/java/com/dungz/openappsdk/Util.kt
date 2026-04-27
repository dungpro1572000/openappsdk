package com.dungz.openappsdk

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

// Extension function to safely traverse ContextWrapper chain
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}