package com.nagel.wordnotification.core.services

import android.os.Parcel
import android.os.Parcelable

data class NotificationDto(
    var text: String?,
    var translation: String?,
    val date: Long,
    var uniqueId: Int,
    var step: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeString(translation)
        parcel.writeLong(date)
        parcel.writeInt(uniqueId)
        parcel.writeInt(step)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NotificationDto> {
        override fun createFromParcel(parcel: Parcel): NotificationDto {
            return NotificationDto(parcel)
        }

        override fun newArray(size: Int): Array<NotificationDto?> {
            return arrayOfNulls(size)
        }
    }
}