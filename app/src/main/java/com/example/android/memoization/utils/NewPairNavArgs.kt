package com.example.android.memoization.utils

import android.os.Parcel
import android.os.Parcelable

sealed class NewPairNavArgs(
    val editMode: Boolean,
    val fromLanguage: String,
    val toLanguage: String
) : Parcelable {
    class EditPair(
        val wordPairId: Long,
        fromLanguage: String,
        toLanguage: String,
    ) :
        NewPairNavArgs(true, fromLanguage, toLanguage) {
        constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeLong(wordPairId)
            parcel.writeString(fromLanguage)
            parcel.writeString(toLanguage)

        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<EditPair> {
            override fun createFromParcel(parcel: Parcel): EditPair {
                return EditPair(parcel)
            }

            override fun newArray(size: Int): Array<EditPair?> {
                return arrayOfNulls(size)
            }
        }
    }

    class NewWordPair(val stackId: Long, fromLanguage: String, toLanguage: String) :
        NewPairNavArgs(false, fromLanguage, toLanguage) {
        constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeLong(stackId)
            parcel.writeString(fromLanguage)
            parcel.writeString(toLanguage)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<NewWordPair> {
            override fun createFromParcel(parcel: Parcel): NewWordPair {
                return NewWordPair(parcel)
            }

            override fun newArray(size: Int): Array<NewWordPair?> {
                return arrayOfNulls(size)
            }
        }
    }
}