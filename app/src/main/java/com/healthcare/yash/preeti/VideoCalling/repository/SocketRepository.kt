package com.healthcare.yash.preeti.VideoCalling.repository

import android.util.Log
import com.google.gson.Gson
import com.healthcare.yash.preeti.VideoCalling.models.MessageModel
import com.healthcare.yash.preeti.VideoCalling.utils.NewMessageInterface
import com.healthcare.yash.preeti.other.Constants.VIDEOCALLINGWEBRTC
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

class SocketRepository(private val messageInterface:NewMessageInterface) {
    private var webSocket:WebSocketClient?=null
    private var UID:String?=null
    private val TAG = "SocketRepository"
    private val gson = Gson()

    fun initSocket(uid:String){
        UID = uid

        webSocket = object:WebSocketClient(URI("ws://192.168.29.194:3000")){
            override fun onOpen(handshakedata: ServerHandshake?) {
                sendMessageToSocket(
                    MessageModel(
                        "store_user",uid,null,null
                    )
                )
            }

            override fun onMessage(message: String?) {
                try {
                    messageInterface.onNewMessage(gson.fromJson(message,MessageModel::class.java))

                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(VIDEOCALLINGWEBRTC, "onClose: $reason")
            }

            override fun onError(ex: Exception?) {
                Log.d(VIDEOCALLINGWEBRTC, "onError: $ex")
            }

        }
        webSocket?.connect()

    }

    fun sendMessageToSocket(message: MessageModel) {
        try {
            Log.d(VIDEOCALLINGWEBRTC, "sendMessageToSocket: $message")
            webSocket?.send(Gson().toJson(message))
        } catch (e: Exception) {
            Log.d(VIDEOCALLINGWEBRTC, "sendMessageToSocket: $e")
        }
    }
}