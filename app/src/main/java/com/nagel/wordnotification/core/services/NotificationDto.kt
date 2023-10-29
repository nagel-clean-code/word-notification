package com.nagel.wordnotification.core.services

import android.os.Parcel
import android.os.Parcelable

data class NotificationDto(
    var text: String?,
    val date: Long
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeLong(date)
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