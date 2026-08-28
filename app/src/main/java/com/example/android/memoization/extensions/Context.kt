package com.example.android.memoization.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast

infix fun Context.showToast(stringId: Int?) {
    if (stringId != null) {
        Toast.makeText(this, this.resources.getText(stringId), Toast.LENGTH_SHORT).show()
    }
}

fun Context.showToast(message: String) {
    if (message.isNotBlank()) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

/** The activity a composable is drawn in, unwrapped from the theme wrappers Compose adds. */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
