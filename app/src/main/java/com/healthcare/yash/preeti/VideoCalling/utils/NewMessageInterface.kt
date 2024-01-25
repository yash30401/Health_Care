package com.healthcare.yash.preeti.VideoCalling.utils

import com.healthcare.yash.preeti.VideoCalling.models.MessageModel


interface NewMessageInterface {
    fun onNewMessage(message: MessageModel)
}