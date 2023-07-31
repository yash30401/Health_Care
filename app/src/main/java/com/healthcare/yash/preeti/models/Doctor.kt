package com.healthcare.yash.preeti.models

data class Doctor(
    val About: String = "",
    val Address: String = "",
    val City: String = "",
    val Consultation_Fee: Int? = 0,
    var Contact_Info: ContactInfo? = null,
    val Experience: Int = 0,
    val Name: String = "",
    val Profile_Pic: String = "",
    var Reviews_And_Ratings: List<ReviewsAndRatings> = emptyList(),
    val Services: List<String> = emptyList(),
    val Specialization: String = "",
    val Working_Hours: String = ""
)

data class ContactInfo(
    val email: String = "",
    val phone_number: String = "",
    val website: String = ""
)

data class ReviewsAndRatings(
    val rating: Double = 0.0,
    val review: String = ""
)
