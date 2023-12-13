package com.healthcare.yash.preeti.models

import com.google.firebase.Timestamp

data class DoctorAppointment( val status:String,  val typeOfConsultation:String,
                              val dateTime: Timestamp,  val usersReference:String)