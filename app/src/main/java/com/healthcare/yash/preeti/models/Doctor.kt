package com.healthcare.yash.preeti.models

import com.google.firebase.firestore.PropertyName

data class Doctor(
    @get:PropertyName("About") val about: String = "",
    @get:PropertyName("Address") val address: String = "",
    @get:PropertyName("City") val city:String =  "",
    @get:PropertyName("Consultation_Fee") val consultationFee:Int = 0,
    @get:PropertyName("Contact_Info") val contactInfo: ContactInfo? = null,
    @get:PropertyName("Experience") val experience:Int = 0,
    @get:PropertyName("Name") val name:String = "",
    @get:PropertyName("Reviews_and_Ratings") val reviewsAndRatings:List<ReviewsAndRatings> = emptyList(),
    @get:PropertyName("Services") val services:List<String> = emptyList(),
    @get:PropertyName("Specialization") val specialization: String = "",
    @get:PropertyName("Working_Hours") val workingHours: String = ""
)

data class ContactInfo(
    @get:PropertyName("email") val email:String = "",
    @get:PropertyName("phone_number") val phoneNumber: String = "",
    @get:PropertyName("website") val website: String = ""
)

data class ReviewsAndRatings(
    @get:PropertyName("rating") val rating: Double = 0.0,
    @get:PropertyName("review") val review: String = ""
)