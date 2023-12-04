package com.healthcare.yash.preeti.models

import com.google.firebase.Timestamp

data class UserAppointment(private val status:String, private val typeOfConsultation:String,
                           private val dateTime:Timestamp, private val doctorsReference:String)