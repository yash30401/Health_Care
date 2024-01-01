package com.healthcare.yash.preeti.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class DetailedUserAppointment(
    val profileImage:String, val name:String,
    val specialization:String,
    val status:String, val typeOfConsultation:String,
    val dateTime: Timestamp?,
    val doctorId:String,
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readString().toString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(profileImage)
        parcel.writeString(name)
        parcel.writeString(specialization)
        parcel.writeString(status)
        parcel.writeString(typeOfConsultation)
        parcel.writeParcelable(dateTime, flags)
        parcel.writeString(doctorId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DetailedUserAppointment> {
        override fun createFromParcel(parcel: Parcel): DetailedUserAppointment {
            return DetailedUserAppointment(parcel)
        }

        override fun newArray(size: Int): Array<DetailedUserAppointment?> {
            return arrayOfNulls(size)
        }
    }
}