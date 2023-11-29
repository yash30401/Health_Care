package com.healthcare.yash.preeti.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.ui.fragments.DoctorDetailedViewArgs
import com.healthcare.yash.preeti.utils.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SlotsRepository @Inject constructor(
private val firebaseAuth: FirebaseAuth,
    private val firestore:FirebaseFirestore
) {

    suspend fun getAllSlots(args: DoctorDetailedViewArgs): Flow<NetworkResult<MutableList<String>>> {
        return flow {
            val slotCollectionRef =
                firestore.collection("Doctors").document(args.doctor.Id.toString())
                    .collection("Slots")

            val querySnapshot = slotCollectionRef.get().await()
            val listOfSlots = mutableListOf<String>()

            for (document in querySnapshot) {
                if (document.exists()) {
                    val slotList = document.getString("timings") ?: ""

                    listOfSlots.add(slotList)
                }
            }
            emit(NetworkResult.Success(listOfSlots))
        }.catch {
            NetworkResult.Error(it.message, null)
        }.flowOn(Dispatchers.IO)
    }
}