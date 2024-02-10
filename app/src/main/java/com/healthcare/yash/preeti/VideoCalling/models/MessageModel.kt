package com.healthcare.yash.preeti.VideoCalling.models

data class MessageModel(
    val type: TYPE,
    val name: String? = null,
    val target: String? = null,
    val data:Any?=null
)