package com.healthcare.yash.preeti.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    suspend fun addAppointmentToTheFirebase(userAppointment: UserAppointment,doctorUid:String,
                                            doctorAppointment: DoctorAppointment): Flow<NetworkResult<String>>{
        return flow {

            firestore.collection("Users").document(firebaseAuth.currentUser?.uid.toString())
                .collection("Appointments").add(userAppointment).await()

            firestore.collection("Doctors").document(doctorUid).collection("Appointments").add(doctorAppointment)
                .await()

            emit(NetworkResult.Success("Data Added"))
        }.catch {
            NetworkResult.Error(it.message,null)
        }.flowOn(Dispatchers.IO)
    }

}