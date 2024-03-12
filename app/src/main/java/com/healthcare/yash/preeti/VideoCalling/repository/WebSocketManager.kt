package com.healthcare.yash.preeti.VideoCalling.repository

import android.util.Log
import com.google.gson.Gson
import com.healthcare.yash.preeti.VideoCalling.models.MessageModel
import com.healthcare.yash.preeti.VideoCalling.models.TYPE
import com.healthcare.yash.preeti.VideoCalling.utils.NewMessageInterface
import com.healthcare.yash.preeti.other.Constants.VIDEOCALLINGWEBRTC
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

class WebSocketManager(private val messageInterface:NewMessageInterface) {
    private var webSocket:WebSocketClient?=null
    private var UID:String?=null
    private val gson = Gson()

    fun initSocket(uid:String){
        UID = uid

        webSocket = object:WebSocketClient(URI("wss://webrtcdeploy-6f29016af3e8.herokuapp.com/")){
            override fun onOpen(handshakedata: ServerHandshake?) {
                sendMessageToSocket(
                    MessageModel(
                        TYPE.STORE_USER,uid,null,null
                    )
                )
                Log.d(VIDEOCALLINGWEBRTC,"HANDSHAKEDATA:- ${handshakedata.toString()}")
            }

            override fun onMessage(message: String?) {
                try {
                    messageInterface.onNewMessage(gson.fromJson(message,MessageModel::class.java))
                    Log.d(VIDEOCALLINGWEBRTC,"ONNEWMESSAGE:- ${message.toString()}")
                }catch (e:Exception){
                    e.printStackTrace()
                    Log.d(VIDEOCALLINGWEBRTC,"EXCEPTION:- ${e.message.toString()}")
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
        Log.d(VIDEOCALLINGWEBRTC,"Connection:- ${webSocket?.socket?.isConnected.toString()}")
    }

    fun sendMessageToSocket(message: MessageModel) {
        try {
            Log.d(VIDEOCALLINGWEBRTC, "sendMessageToSocket: $message")
            webSocket?.send(Gson().toJson(message))
            Log.d(VIDEOCALLINGWEBRTC, "sendMessageToSocket JSON FORMAT: ${Gson().toJson(message)}")
        } catch (e: Exception) {
            Log.d(VIDEOCALLINGWEBRTC, "sendMessageToSocket: $e")
        }
    }

    fun closeConnection(){
        webSocket?.connection?.closeConnection(0,"Close")
        webSocket = null
    }
}