package com.example.android.memoization.utils

import android.os.Parcel
import android.os.Parcelable

sealed class NewPairNavArgs(val editMode: Boolean): Parcelable {
    class EditPair(val wordPairId: Long, editMode: Boolean = true): NewPairNavArgs(editMode) {
        constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readByte() != 0.toByte()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeLong(wordPairId)
            parcel.writeByte(1)
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

    class NewWordPair(val stackId: Long, editMode: Boolean = false): NewPairNavArgs(editMode) {
        constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readByte() != 0.toByte()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeLong(stackId)
            parcel.writeByte(0)
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