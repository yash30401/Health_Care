package com.healthcare.yash.preeti.models

import com.google.firebase.Timestamp

data class DetailedUserAppointment(val profileImage:String, val name:String,val specialization:String,val status:String, val typeOfConsultation:String,
                                   val dateTime: Timestamp)