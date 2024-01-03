package com.healthcare.yash.preeti.repositories

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.healthcare.yash.preeti.models.ChatMessage
import com.healthcare.yash.preeti.models.ChatRoom
import com.healthcare.yash.preeti.models.DoctorChatData
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants
import com.healthcare.yash.preeti.other.Constants.RECENTCHATS
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

    lateinit var chatRoom: ChatRoom
    lateinit var chatRoomId: String
    suspend fun getOrCreateChatRoom(doctorId: String): Flow<NetworkResult<ChatRoom>> {
        return flow {
            chatRoomId = getChatRoomId(firebaseAuth.currentUser?.uid!!, doctorId)
            try {
                val getChatRoomReference =
                    firestore.collection("ChatRoom").document(chatRoomId).get().await()

                if (!getChatRoomReference.exists()) {
                    chatRoom = ChatRoom(
                        chatRoomId,
                        Pair(firebaseAuth.currentUser!!.uid, doctorId),
                        Timestamp.now(),
                        "",
                        ""
                    )
                    firestore.collection("ChatRoom").document(chatRoomId).set(chatRoom).await()
                } else {
                    val userIdsRaw = getChatRoomReference.get("userIds")

                    var userIds: Pair<String, String>? = null
                    if (userIdsRaw is Map<*, *>) {
                        // Assuming the userIds are stored in the array
                        val firstUserId = userIdsRaw["first"] as String?
                        val secondUserId = userIdsRaw["second"] as? String

                        if (firstUserId != null && secondUserId != null) {
                            userIds = Pair(firstUserId, secondUserId)
                        }
                    }

                    chatRoom = ChatRoom(
                        chatRoomId = getChatRoomReference.getString("chatRoomId") ?: "",
                        userIds = userIds ?: Pair("", ""),
                        lastMessageTimestamp = getChatRoomReference.getTimestamp("lastMessageTimestamp")!!,
                        lastMessageSenderId = getChatRoomReference.getString("lastMessageSenderId")
                            ?: "",
                        lastMessage = getChatRoomReference.getString("lastMessage")?:""
                    )
                }

                emit(NetworkResult.Success(chatRoom))
            } catch (e: Exception) {
                Log.d(Constants.CHATROOMTESTING, "try Catch block:- ${e.message}")
                emit(NetworkResult.Error(e.message.toString()))
            }
        }.catch {
            Log.d(Constants.CHATROOMTESTING, "Catch block of flow:- ${it.message}")
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


    suspend fun getChatMessages(): Flow<NetworkResult<List<ChatMessage>>> {
        return flow<NetworkResult<List<ChatMessage>>> {
            try {

                val chatRoomMessageReference =
                    firestore.collection("ChatRoom").document(chatRoomId).collection("Chats")
                        .orderBy("timestamp", Query.Direction.DESCENDING).get().await()
                val listOfMessages = mutableListOf<ChatMessage>()

                for (document in chatRoomMessageReference) {
                    if (document.exists()) {
                        val chatMessage = ChatMessage(
                            message = document.getString("message") ?: "",
                            senderId = document.getString("senderId") ?: "",
                            timestamp = document.getTimestamp("timestamp")!!
                        )
                        listOfMessages.add(chatMessage)
                    }
                }
                emit(NetworkResult.Success(listOfMessages))

            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message.toString()))
            }
        }.catch {
            NetworkResult.Error(it.message.toString(), null)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendMessageToTheUser(message: String): Flow<NetworkResult<ChatMessage>> {
        return flow<NetworkResult<ChatMessage>> {
            try {
                chatRoom.lastMessageTimestamp = Timestamp.now()
                chatRoom.lastMessageSenderId = firebaseAuth.currentUser?.uid.toString()
                chatRoom.lastMessage = message
                firestore.collection("ChatRoom").document(chatRoomId).set(chatRoom).await()

                val chatMessage = ChatMessage(
                    message, firebaseAuth.currentUser?.uid.toString(),
                    Timestamp.now()
                )

                val chatRoomMessageReference =
                    firestore.collection("ChatRoom").document(chatRoomId).collection("Chats")

                chatRoomMessageReference.add(chatMessage).await()

                emit(NetworkResult.Success(chatMessage))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message.toString()))
            }
        }.catch {
            NetworkResult.Error(it.message.toString(), null)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getRecentChats(): Flow<NetworkResult<List<Pair<ChatRoom, DoctorChatData>>>> {
        return flow<NetworkResult<List<Pair<ChatRoom, DoctorChatData>>>> {
            try {
                val query1 = firestore.collection("ChatRoom")
                    .whereEqualTo("userIds.first", firebaseAuth.currentUser?.uid.toString())
                    .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING).get().await()

                val query2 = firestore.collection("ChatRoom")
                    .whereEqualTo("userIds.second", firebaseAuth.currentUser?.uid.toString())
                    .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING).get().await()

                val recentChatReference = mutableListOf<DocumentSnapshot>()
                recentChatReference.addAll(query1.documents)
                recentChatReference.addAll(query2.documents)

                val listOfRecentChats = mutableListOf<Pair<ChatRoom, DoctorChatData>>()


                for (document in recentChatReference) {
                    if (document.exists()) {

                        val userIdsRaw = document.get("userIds")
                        var userIds: Pair<String, String>? = null
                        if (userIdsRaw is Map<*, *>) {
                            val firstUserId = userIdsRaw["first"] as String?
                            val secondUserId = userIdsRaw["second"] as? String

                            if (firstUserId != null && secondUserId != null) {
                                userIds = Pair(firstUserId, secondUserId)
                            }
                        }
                        val chatRoom = ChatRoom(
                            chatRoomId = document.getString("chatRoomId") ?: "",
                            userIds = userIds ?: Pair("", ""),
                            lastMessageTimestamp = document.getTimestamp("lastMessageTimestamp")!!,
                            lastMessageSenderId = document.getString("lastMessageSenderId") ?: "",
                            lastMessage = document.getString("lastMessage")?:""
                        )

                        var getDoctorReference: DocumentSnapshot? = null
                        if (userIds?.first == firebaseAuth.currentUser?.uid.toString()) {
                            getDoctorReference =
                                firestore.collection("Doctors").document(userIds.second).get()
                                    .await()
                        } else {
                            getDoctorReference =
                                firestore.collection("Doctors").document(userIds?.first!!).get()
                                    .await()
                        }
                        if (getDoctorReference.exists()) {
                             val doctorChatData = DoctorChatData(
                                profile = getDoctorReference.getString("Profile_Pic") ?: "",
                                name = getDoctorReference.getString("Name") ?: "",
                                specialization = getDoctorReference.getString("Specialization")
                                    ?: ""
                            )
                            listOfRecentChats.add(Pair(chatRoom, doctorChatData))
                        }
                    }
                }

                emit(NetworkResult.Success(listOfRecentChats))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message.toString()))
                Log.d(RECENTCHATS,"try Cathc ${e.message.toString()}")
            }
        }.catch {
            Log.d(RECENTCHATS,"try Cathc ${it.message.toString()}")
            NetworkResult.Error(it.message.toString(), null)
        }.flowOn(Dispatchers.IO)
    }

}