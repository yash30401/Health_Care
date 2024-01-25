package com.healthcare.yash.preeti.VideoCalling.models

data class MessageModel(
    val type: String,
    val uid: String? = null,
    val targetUid: String? = null,
    val data:Any?=null
)