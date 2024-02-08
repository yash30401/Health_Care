package com.healthcare.yash.preeti.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.VideoCalling.RTCClient
import com.healthcare.yash.preeti.VideoCalling.models.IceCandidateModel
import com.healthcare.yash.preeti.VideoCalling.models.MessageModel
import com.healthcare.yash.preeti.VideoCalling.repository.SocketRepository
import com.healthcare.yash.preeti.VideoCalling.utils.NewMessageInterface
import com.healthcare.yash.preeti.VideoCalling.utils.PeerConnectionObserver
import com.healthcare.yash.preeti.VideoCalling.utils.RtcAudioManager
import com.healthcare.yash.preeti.adapters.UpcomingAppointmentsAdapter
import com.healthcare.yash.preeti.databinding.ActivityMainBinding
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants
import com.healthcare.yash.preeti.other.Constants.FIREBASEMESSAGINTOKEN
import com.healthcare.yash.preeti.ui.fragments.DoctorDetailedView
import com.healthcare.yash.preeti.ui.fragments.DoctorDetailedViewArgs
import com.healthcare.yash.preeti.utils.await
import com.healthcare.yash.preeti.viewmodels.AppointmentViewModel
import com.healthcare.yash.preeti.viewmodels.FirebaseMessagingViewModel
import com.permissionx.guolindev.PermissionX
import com.razorpay.Checkout
import com.razorpay.ExternalWalletListener
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NewMessageInterface, UpcomingAppointmentsAdapter.VideoCallClickListner{

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private val firebaseMessagingViewModel by viewModels<FirebaseMessagingViewModel>()
    lateinit var socketRepository: SocketRepository

    lateinit var uid:String
    lateinit var targetUID:String

    private var rtcClient: RTCClient?=null
    private val gson = Gson()
    private var isMute = false
    private var isCameraPause = false
    private val rtcAudioManager by lazy { RtcAudioManager.create(this) }
    private var isSpeakerMode = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView2) as NavHostFragment
        navController = navHostFragment.navController
        uid = firebaseAuth.uid.toString()
        binding.bottomNav.setupWithNavController(navController)
        hideBottomNavOnAuthFragment()

        socketRepository = SocketRepository(this)
        socketRepository.initSocket(firebaseAuth.uid.toString())

        // Preload Razorpay Checkout for better performance
        Checkout.preload(applicationContext)

        // Configure Firebase App Check with Play Integrity provider
        Firebase.initialize(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

        binding.bottomNav.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.mainFragment)
                    true
                }

                R.id.chatFragment -> {
                    navController.navigate(R.id.chatFragment)
                    true
                }

                R.id.profileFrament -> {
                    navController.navigate(R.id.profileFrament)
                    true
                }

                else -> {
                    false
                }
            }
        }
        firebaseAuth = FirebaseAuth.getInstance()

        getFCMToken()

        init()

        getPermissionsForVideoCall()
    }
    private fun init(){
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
                    MessageModel("ice_candidate",uid,targetUID,candidate)
                )
            }

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                p0?.videoTracks?.get(0)?.addSink(binding?.remoteView)
                Log.d(Constants.VIDEOCALLINGWEBRTC, "onAddStream: $p0")
            }
        })

//        rtcClient?.initializeSurfaceView(binding!!.localView)
//        rtcClient?.startLocalVideo(binding!!.localView)
        rtcAudioManager.setDefaultAudioDevice(RtcAudioManager.AudioDevice.SPEAKER_PHONE)


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

    private fun getPermissionsForVideoCall() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            ).request{ allGranted, _ ,_ ->
                if (allGranted){


                } else {
                    Toast.makeText(this,"you should accept all permissions", Toast.LENGTH_LONG).show()
                }
            }
    }


    // Method to hide bottom navigation on specific fragments
    private fun hideBottomNavOnAuthFragment() {
        navController.addOnDestinationChangedListener { _, destination, _ ->

            if (destination.id == R.id.authentication2 || destination.id == R.id.otpFragment || destination.id == R.id.doctorDetailedView || destination.id==R.id.chattingFragment) {
                binding.bottomNav.visibility = View.GONE
            } else {
                binding.bottomNav.visibility = View.VISIBLE
            }
        }
    }

    // Handle back button press
    override fun onBackPressed() {
        val currentDestination = navController.currentDestination
        val isLoggedIn = firebaseAuth.currentUser != null

        Log.d("FragTesting", isLoggedIn.toString())
        Log.d("FragTesting", currentDestination.toString())

        // Check if the user is logged in and on specific fragments
        if (isLoggedIn && currentDestination?.id == R.id.mainFragment || isLoggedIn && currentDestination?.id == R.id.chatFragment || isLoggedIn && currentDestination?.id == R.id.profileFrament) {
            showExitConfirmationDialog()
        } else {
            super.onBackPressed()
        }


    }

    private fun showExitConfirmationDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Exit Confirmation")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Exit") { _, _ ->
                finishAffinity()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getFCMToken() {
        firebaseMessagingViewModel.getFCMToken()

        lifecycleScope.launch{
            firebaseMessagingViewModel.token.collect{
                when(it){
                    is NetworkResult.Error -> {
                        Log.d(FIREBASEMESSAGINTOKEN,"Error Block:- ${it.message}")
                    }
                    is NetworkResult.Loading -> {
                        Log.d(FIREBASEMESSAGINTOKEN,"Loading Block:- ${it.message}")
                    }
                    is NetworkResult.Success -> {
                        Log.d(FIREBASEMESSAGINTOKEN,"Success Block:- ${it.data.toString()}")
                    }
                    else -> {}
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        rtcClient?.endCall() // Close any existing WebRTC connections
        rtcClient = null
        socketRepository.closeConnection()
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
                            rtcClient?.initializeSurfaceView(binding.localView)
                            rtcClient?.initializeSurfaceView(binding.remoteView)
                            rtcClient?.startLocalVideo(binding.localView)
                            rtcClient?.call(targetUID)
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
                Log.d("OFEERWEBRTC","Recived")
                runOnUiThread {
                    setIncomingCallLayoutVisible()
                    binding?.incomingNameTV?.text = "${message.name.toString()} is calling you"
                    binding?.acceptButton?.setOnClickListener {
                        setIncomingCallLayoutGone()
                        setCallLayoutVisible()

                        binding?.apply {
                            rtcClient?.initializeSurfaceView(localView)
                            rtcClient?.initializeSurfaceView(binding.remoteView)
                            rtcClient?.startLocalVideo(localView)
                        }
                        val session = SessionDescription(
                            SessionDescription.Type.OFFER,
                            message.data.toString()
                        )
                        Log.d("OFEERWEBRTC","UID:- ${message.name}")
                        rtcClient?.onRemoteSessionReceived(session)
                        rtcClient?.answer(message.name!!)
                        targetUID = message.name!!
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

    override fun onclick(userUid: String) {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            ).request{ allGranted, _ ,_ ->
                if (allGranted){
                    targetUID = userUid
                    socketRepository?.sendMessageToSocket(
                        MessageModel(
                            "start_call",uid,targetUID,null
                        )
                    )
                } else {
                    Toast.makeText(this,"you should accept all permissions", Toast.LENGTH_LONG).show()
                }
            }
    }



}