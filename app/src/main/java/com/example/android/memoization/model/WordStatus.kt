package com.example.android.memoization.model

sealed class WordStatus {
    abstract val frequency: Int
    override fun toString(): String {
        return frequency.toString()
    }

    class Level1: WordStatus() {
        override val frequency: Int = 1
    }
    class Level2:WordStatus() {
        override val frequency: Int = 2
    }
    class Level3:WordStatus() {
        override val frequency: Int = 7
    }
    class Level4:WordStatus() {
        override val frequency: Int = 14
    }
    class Learned:WordStatus() {
        override val frequency: Int = 30
    }

}

enum class Frequency {
    Daily, OneInTwo, Week, BiWeek, Month
}