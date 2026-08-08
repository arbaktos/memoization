package com.example.android.memoization.extensions

import android.content.Context
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
