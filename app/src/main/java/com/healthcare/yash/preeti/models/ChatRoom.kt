package com.healthcare.yash.preeti.models

import com.google.firebase.Timestamp

data class ChatRoom(
    val chatRoomId: String,
    val userIds: Pair<String, String>,
    var lastMessageTimestamp: Timestamp,
    var lastMessageSenderId: String,
    val lastMessage:String
)