package com.healthcare.yash.preeti.repositories

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.healthcare.yash.preeti.models.ChatRoom
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.utils.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun getOrCreateChatRoom(doctorId: String): Flow<NetworkResult<ChatRoom>> {
        return flow {
            val chatRoomId = getChatRoomId(firebaseAuth.currentUser?.uid!!, doctorId)


            try {
                val getChatRoomReference =
                    firestore.collection("ChatRoom").document(chatRoomId).get().await()
                var chatRoom = ChatRoom(chatRoomId = getChatRoomReference.getString("chatRoomId")?:"",
                    userIds = getChatRoomReference.get("userIds") as Pair<String, String>,
                    lastMessageTimestamp = getChatRoomReference.getTimestamp("lastMessageTimestamp")!!,
                    lastMessageSenderId = getChatRoomReference.getString("lastMessageSenderId") ?: "")

                if(chatRoom==null){
                    chatRoom = ChatRoom(chatRoomId,
                        Pair(firebaseAuth.currentUser!!.uid,doctorId),
                        Timestamp.now(),
                        ""
                    )
                    firestore.collection("ChatRoom").document(chatRoomId).set(chatRoom).await()
                }

                emit(NetworkResult.Success(chatRoom))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message.toString()))
            }
        }.catch {
            NetworkResult.Error(it.message.toString(), null)
        }.flowOn(Dispatchers.IO)
    }

    private fun getChatRoomId(currentUserId: String, doctorId: String): String {
        if (currentUserId.hashCode() < doctorId.hashCode()) {
            return currentUserId + "_" + doctorId
        } else {
            return doctorId + "_" + currentUserId
        }
    }
}