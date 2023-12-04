package com.healthcare.yash.preeti.models

import com.google.firebase.Timestamp

data class DoctorAppointment(private val status:String, private val typeOfConsultation:String,
                             private val dateTime: Timestamp, private val usersReference:String)