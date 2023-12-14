package com.healthcare.yash.preeti.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class UserAppointment(val status:String, val typeOfConsultation:String,
                           val dateTime:Timestamp, val doctorsReference: DocumentReference
)