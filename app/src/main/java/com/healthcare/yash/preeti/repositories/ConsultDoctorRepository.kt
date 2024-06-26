package com.healthcare.yash.preeti.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.healthcare.yash.preeti.models.ContactInfo
import com.healthcare.yash.preeti.models.Doctor
import com.healthcare.yash.preeti.models.ReviewsAndRatings
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants.CONSULTDOCTORFRAGTESTTAG
import com.healthcare.yash.preeti.utils.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

import javax.inject.Inject


class ConsultDoctorRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) {
    suspend fun fetchDoctorsInYourArea(): Flow<List<Doctor>> {
        return flow<List<Doctor>> {

            // Collection Ref of Firestore Collection of Doctors
            val doctorsCollectionRef = firebaseFirestore.collection("Doctors")
            val querySnapshot =
                doctorsCollectionRef.get().await() // Await for the result on completelistener
            val doctorsList = mutableListOf<Doctor>()


            for (document in querySnapshot) {
                if (document.exists()) {
                    val doctor = Doctor(
                        Id= document.getString("Id")?:"",
                        Name = document.getString("Name") ?: "",
                        About = document.getString("About") ?: "",
                        clinic_visit = document.getLong("clinic_visit")?.toInt() ?: 0,
                        Address = document.getString("Address") ?: "",
                        City = document.getString("City") ?: "",
                        Experience = document.getLong("Experience")?.toInt() ?: 0,
                        Profile_Pic = document.getString("Profile_Pic") ?: "",
                        Services = document.get("Services") as List<String>,
                        Specialization = document.getString("Specialization") ?: "",
                        Working_Hours = document.getString("Working_Hours") ?: "",
                        video_consult = document.getLong("video_consult")?.toInt() ?: 0,
                    )  // Mapping into Doctor data class

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
                    } // Mapping into Contact data class and then set to contact info field

                    if (reviewsAndRatingsData != null) {
                        val reviewsAndRatings = reviewsAndRatingsData.mapNotNull {
                            if (it is HashMap<*, *>) {
                                val date = it["date"] as? String
                                val name = it["name"] as? String
                                val rating = it["rating"]
                                val review = it["review"] as? String
                                if (date != null && name != null && rating != null && review != null) {
                                    ReviewsAndRatings(
                                        date,
                                        name,
                                        rating.toString().toDouble(),
                                        review
                                    )
                                } else {
                                    null
                                }
                            } else {
                                null
                            }
                        }
                        Log.d(
                            "REVIEWSANDRATING",
                            "date:- ${reviewsAndRatings.get(0)?.date}\name:- ${
                                reviewsAndRatings.get(0)?.name
                            }"
                        )
                        doctor.Reviews_And_Ratings = reviewsAndRatings
                    }else{
                        Log.d(
                            "REVIEWSANDRATING",
                            "Reviews Data is empty"
                        )
                    }

                    doctorsList.add(doctor)
                }
            }

            emit(doctorsList) // Emitting the doctors list
        }.flowOn(Dispatchers.IO)
    }
}