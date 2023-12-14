package com.healthcare.yash.preeti.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.healthcare.yash.preeti.models.DetailedUserAppointment
import com.healthcare.yash.preeti.models.Doctor
import com.healthcare.yash.preeti.models.DoctorAppointment
import com.healthcare.yash.preeti.models.UserAppointment
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.utils.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AppointmentRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun addAppointmentToTheFirebase(
        userAppointment: UserAppointment, doctorUid: String,
        doctorAppointment: DoctorAppointment
    ): Flow<NetworkResult<out String>> {
        return flow {


            try {
                firestore.collection("Users").document(firebaseAuth.currentUser?.uid.toString())
                    .collection("Appointments").add(userAppointment).await()

                firestore.collection("Doctors").document(doctorUid).collection("Appointments")
                    .add(doctorAppointment)
                    .await()

                emit(NetworkResult.Success("Data Added"))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message.toString(), null))
            } finally {
                Log.d("Repository", "Exiting addAppointmentToTheFirebase")
            }

        }.catch {
            Log.e("Repository", "Error in addAppointmentToTheFirebase: ${it.message}")
            emit(NetworkResult.Error(it.message.toString(), null))
        }.flowOn(Dispatchers.IO)

    }

    suspend fun getAllUpcomingAppointments(): Flow<NetworkResult<out MutableList<DetailedUserAppointment>>> {
        return flow {
            val appointmentCollectionRef =
                firestore.collection("Users").document(firebaseAuth.currentUser?.uid.toString())
                    .collection("Appointments")

            try {
                val querySnapshot = appointmentCollectionRef.get().await()
                val listOfAppointments = mutableListOf<DetailedUserAppointment>()

                for (document in querySnapshot) {
                    if (document.exists()) {
                        val status = document.getString("status") ?: ""
                        val typeOfConsultation = document.getString("typeOfConsultation") ?: ""
                        val dateTime = document.getTimestamp("dateTime")!!
                        val doctorsReference =
                            document.getDocumentReference("doctorsReference")


                        val docRefQuerySnapshot = doctorsReference!!.get().await()
                        if (docRefQuerySnapshot.exists()) {
                            val detailedUserAppointment = DetailedUserAppointment(
                                profileImage = docRefQuerySnapshot.getString("Profile_Pic") ?: "",
                                name = docRefQuerySnapshot.getString("Name") ?: "",
                                specialization = docRefQuerySnapshot.getString("Specialization") ?: "",
                                status,
                                typeOfConsultation,
                                dateTime
                            )

                            listOfAppointments.add(detailedUserAppointment)
                        }
                    }
                }
                // Ensure that the loading state emits a non-null value
                emit(NetworkResult.Success(listOfAppointments))
            } catch (e: Exception) {
                // Ensure that the loading state emits a non-null value
                emit(NetworkResult.Error(e.message.toString(), null))
            }
        }.catch {
            // Ensure that the loading state emits a non-null value
            emit(NetworkResult.Error(it.message.toString(), null))
        }.flowOn(Dispatchers.IO)
    }

}