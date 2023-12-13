package com.healthcare.yash.preeti.models

import com.google.firebase.Timestamp

data class UserAppointment( val status:String,  val typeOfConsultation:String,
                            val dateTime:Timestamp,  val doctorsReference:String)