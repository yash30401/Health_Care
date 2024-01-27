package com.healthcare.yash.preeti.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.VideoCalling.RTCClient
import com.healthcare.yash.preeti.VideoCalling.models.IceCandidateModel
import com.healthcare.yash.preeti.VideoCalling.models.MessageModel
import com.healthcare.yash.preeti.VideoCalling.repository.SocketRepository
import com.healthcare.yash.preeti.VideoCalling.utils.NewMessageInterface
import com.healthcare.yash.preeti.VideoCalling.utils.PeerConnectionObserver
import com.healthcare.yash.preeti.VideoCalling.utils.RtcAudioManager
import com.healthcare.yash.preeti.databinding.ActivityCallBinding
import com.healthcare.yash.preeti.other.Constants
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription

class CallActivity : AppCompatActivity(),NewMessageInterface {

    private var _binding: ActivityCallBinding?=null
    val binding get() = _binding!!
    private var uid:String?=null
    private var socketRepository: SocketRepository?=null
    private var rtcClient: RTCClient?=null
    private var targetUid:String = ""
    private val gson = Gson()
    private var isMute = false
    private var isCameraPause = false
    private val rtcAudioManager by lazy { RtcAudioManager.create(this) }
    private var isSpeakerMode = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init(){
        uid = intent.getStringExtra("useruid")
        targetUid = intent.getStringExtra("doctorUid").toString()
        socketRepository = SocketRepository(this)
        uid?.let { socketRepository?.initSocket(it) }
        rtcClient = RTCClient(application,uid!!,socketRepository!!, object : PeerConnectionObserver() {
            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                rtcClient?.addIceCandidate(p0)
                val candidate = hashMapOf(
                    "sdpMid" to p0?.sdpMid,
                    "sdpMLineIndex" to p0?.sdpMLineIndex,
                    "sdpCandidate" to p0?.sdp
                )

                socketRepository?.sendMessageToSocket(
                    MessageModel("ice_candidate",uid,targetUid,candidate)
                )
            }

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                p0?.videoTracks?.get(0)?.addSink(binding?.remoteView)
                Log.d(Constants.VIDEOCALLINGWEBRTC, "onAddStream: $p0")
            }
        })
        rtcClient?.initializeSurfaceView(binding.localView)
        rtcClient?.startLocalVideo(binding.localView)
        rtcAudioManager.setDefaultAudioDevice(RtcAudioManager.AudioDevice.SPEAKER_PHONE)

        socketRepository?.sendMessageToSocket(
            MessageModel(
                "start_call",uid,targetUid,null
            )
        )
        binding?.switchCameraButton?.setOnClickListener {
            rtcClient?.switchCamera()
        }

        binding?.micButton?.setOnClickListener {
            if (isMute){
                isMute = false
                binding!!.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24)
            }else{
                isMute = true
                binding!!.micButton.setImageResource(R.drawable.ic_baseline_mic_24)
            }
            rtcClient?.toggleAudio(isMute)
        }

        binding?.videoButton?.setOnClickListener {
            if (isCameraPause){
                isCameraPause = false
                binding!!.videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24)
            }else{
                isCameraPause = true
                binding!!.videoButton.setImageResource(R.drawable.ic_baseline_videocam_24)
            }
            rtcClient?.toggleCamera(isCameraPause)
        }

        binding?.audioOutputButton?.setOnClickListener {
            if (isSpeakerMode){
                isSpeakerMode = false
                binding!!.audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24)
                rtcAudioManager.setDefaultAudioDevice(RtcAudioManager.AudioDevice.EARPIECE)
            }else{
                isSpeakerMode = true
                binding!!.audioOutputButton.setImageResource(R.drawable.ic_baseline_speaker_up_24)
                rtcAudioManager.setDefaultAudioDevice(RtcAudioManager.AudioDevice.SPEAKER_PHONE)

            }

        }

        binding?.endCallButton?.setOnClickListener {
            setCallLayoutGone()
            setIncomingCallLayoutGone()
            rtcClient?.endCall()
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onNewMessage(message: MessageModel) {
        when(message.type){
            "call_response"->{
                if (message.data == "user is not online"){
                    //user is not reachable
                    runOnUiThread {
                        Toast.makeText(this,"user is not reachable", Toast.LENGTH_LONG).show()

                    }
                }else{
                    //we are ready for call, we started a call
                    runOnUiThread {
                        setCallLayoutVisible()
                        binding?.apply {
                            rtcClient?.initializeSurfaceView(localView)
                            rtcClient?.initializeSurfaceView(remoteView)
                            rtcClient?.startLocalVideo(localView)
                            rtcClient?.call(targetUid)
                        }


                    }

                }
            }
            "answer_received" ->{

                val session = SessionDescription(
                    SessionDescription.Type.ANSWER,
                    message.data.toString()
                )
                rtcClient?.onRemoteSessionReceived(session)
                runOnUiThread {
                    binding?.remoteViewLoading?.visibility = View.GONE
                }
            }
            "offer_received" ->{
                runOnUiThread {
                    setIncomingCallLayoutVisible()
                    binding?.incomingNameTV?.text = "${message.uid.toString()} is calling you"
                    binding?.acceptButton?.setOnClickListener {
                        setIncomingCallLayoutGone()
                        setCallLayoutVisible()

                        binding?.apply {
                            rtcClient?.initializeSurfaceView(localView)
                            rtcClient?.initializeSurfaceView(remoteView)
                            rtcClient?.startLocalVideo(localView)
                        }
                        val session = SessionDescription(
                            SessionDescription.Type.OFFER,
                            message.data.toString()
                        )
                        rtcClient?.onRemoteSessionReceived(session)
                        rtcClient?.answer(message.uid!!)
                        targetUid = message.uid!!
                        binding!!.remoteViewLoading.visibility = View.GONE

                    }
                    binding?.rejectButton?.setOnClickListener {
                        setIncomingCallLayoutGone()
                    }

                }

            }


            "ice_candidate"->{
                try {
                    val receivingCandidate = gson.fromJson(gson.toJson(message.data),
                        IceCandidateModel::class.java)
                    rtcClient?.addIceCandidate(
                        IceCandidate(receivingCandidate.sdpMid,
                        Math.toIntExact(receivingCandidate.sdpMLineIndex.toLong()),receivingCandidate.sdpCandidate)
                    )
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setIncomingCallLayoutGone(){
        binding?.incomingCallLayout?.visibility = View.GONE
    }
    private fun setIncomingCallLayoutVisible() {
        binding?.incomingCallLayout?.visibility = View.VISIBLE
    }

    private fun setCallLayoutGone() {
        binding?.callLayout?.visibility = View.GONE
    }

    private fun setCallLayoutVisible() {
        binding?.callLayout?.visibility = View.VISIBLE
    }
}