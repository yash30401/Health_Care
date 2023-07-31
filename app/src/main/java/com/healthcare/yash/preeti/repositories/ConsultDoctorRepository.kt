package com.healthcare.yash.preeti.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.healthcare.yash.preeti.models.ContactInfo
import com.healthcare.yash.preeti.models.Doctor
import com.healthcare.yash.preeti.models.ReviewsAndRatings
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants.CONSULTDOCTORFRAGTESTTAG
import com.healthcare.yash.preeti.utils.await
import javax.inject.Inject

class ConsultDoctorRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) {

    suspend fun fetchDoctorsInYourArea(): NetworkResult<List<Doctor>> {
        return try {
            val doctorsCollectionRef = firebaseFirestore.collection("Doctors")
            val querySnapshot = doctorsCollectionRef.get().await()
            val doctorsList = mutableListOf<Doctor>()

            for (document in querySnapshot) {
                if (document.exists()) {
                    val doctor = Doctor(
                        Name = document.getString("Name") ?: "",
                        About = document.getString("About") ?: "",
                        Consultation_Fee = document.getLong("Consultation_Fee")?.toInt() ?: 0,
                        Address = document.getString("Address") ?: "",
                        City = document.getString("City") ?: "",
                        Experience = document.getLong("Experience")?.toInt() ?: 0,
                        Profile_Pic = document.getString("Profile_Pic") ?: "",
                        Services = document.get("Services") as List<String>,
                        Specialization = document.getString("Specialization") ?: "",
                        Working_Hours = document.getString("Working_Hours") ?: ""
                    )

                    val contactInfoData = document.get("Contact_Info") as? HashMap<*, *>
                    val reviewsAndRatingsData = document.get("Reviews_and_Ratings") as? ArrayList<*>
                    Log.d(CONSULTDOCTORFRAGTESTTAG, "Doctor Name: ${doctor.Name}")
                    if (contactInfoData != null) {
                        val contactInfo = ContactInfo(
                            email = contactInfoData["email"] as? String ?: "",
                            phone_number = contactInfoData["phone_number"] as? String ?: "",
                            website = contactInfoData["website"] as? String ?: ""
                        )
                        doctor.Contact_Info = contactInfo
                    }

                    if (reviewsAndRatingsData != null) {
                        val reviewsAndRatings = reviewsAndRatingsData.mapNotNull {
                            if (it is HashMap<*, *>) {
                                val rating = it["rating"] as? Double
                                val review = it["review"] as? String
                                if (rating != null && review != null) {
                                    ReviewsAndRatings(rating, review)
                                } else {
                                    null
                                }
                            } else {
                                null
                            }
                        }
                        doctor.Reviews_And_Ratings = reviewsAndRatings
                    }

                    doctorsList.add(doctor)
                }
            }

            NetworkResult.Success(doctorsList)
        } catch (e: Exception) {
            Log.d(CONSULTDOCTORFRAGTESTTAG,"EXCEPTION:- ${e.message.toString()}")
            NetworkResult.Error(e.message)
        }
    }
}