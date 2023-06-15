package com.healthcare.yash.preeti.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken


data class ResendTokenModelClass(val resendingToken: ForceResendingToken?) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readParcelable<PhoneAuthProvider.ForceResendingToken>(PhoneAuthProvider.ForceResendingToken::class.java.classLoader)!!){
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(resendingToken, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ResendTokenModelClass> {
        override fun createFromParcel(parcel: Parcel): ResendTokenModelClass {
            return ResendTokenModelClass(parcel)
        }

        override fun newArray(size: Int): Array<ResendTokenModelClass?> {
            return arrayOfNulls(size)
        }
    }
}