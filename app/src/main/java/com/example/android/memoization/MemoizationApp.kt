package com.example.android.memoization

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MemoizationApp @Inject constructor(): Application() {
}