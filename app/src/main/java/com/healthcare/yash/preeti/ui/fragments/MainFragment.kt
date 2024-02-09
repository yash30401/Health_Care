package com.healthcare.yash.preeti.ui.fragments


import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
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
import com.healthcare.yash.preeti.databinding.FragmentMainBinding
import com.healthcare.yash.preeti.models.DetailedUserAppointment
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.ChatClickListner
import com.healthcare.yash.preeti.other.Constants
import com.healthcare.yash.preeti.other.Constants.FETCHAPPOINTMENTS
import com.healthcare.yash.preeti.other.Constants.HEADERLAYOUTTAG
import com.healthcare.yash.preeti.other.Constants.MAINFRAGMENTTAG
import com.healthcare.yash.preeti.ui.CallActivity
import com.healthcare.yash.preeti.ui.MainActivity
import com.healthcare.yash.preeti.utils.BottomNavigationVisibilityListener
import com.healthcare.yash.preeti.viewmodels.AppointmentViewModel
import com.healthcare.yash.preeti.viewmodels.AuthViewModel
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import javax.inject.Inject


@AndroidEntryPoint
class MainFragment() : Fragment(),ChatClickListner,UpcomingAppointmentsAdapter.VideoCallClickListner,NewMessageInterface {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private val viewModel by viewModels<AuthViewModel>()
    private val appointmentViewModel by viewModels<AppointmentViewModel>()
    lateinit var upcomingAppointmentsAdapter: UpcomingAppointmentsAdapter

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    lateinit var uid:String
    lateinit var targetUID:String

    private var rtcClient: RTCClient?=null
    private val gson = Gson()
    private var isMute = false
    private var isCameraPause = false
    private val rtcAudioManager by lazy { RtcAudioManager.create(requireContext()) }
    private var isSpeakerMode = true

    lateinit var socketRepository: SocketRepository

    private var bottomNavigationVisibilityListener: BottomNavigationVisibilityListener? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_main, container, false)
        val toolbar: Toolbar = rootView.findViewById(R.id.toolbar)
        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.setDisplayShowHomeEnabled(true)

        drawerLayout = rootView.findViewById(R.id.drawerLayout)
        navigationView = rootView.findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            activity, drawerLayout, toolbar, 0, R.string.app_name
        )

        uid = firebaseAuth.uid.toString()

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            // Handle navigation item clicks here
            when (menuItem.itemId) {
                R.id.home -> {
                    true
                }

                R.id.aboutApp -> {
                    Log.d(MAINFRAGMENTTAG, "About App")
                    true
                }

                R.id.privacyPolicy -> {
                    Log.d(MAINFRAGMENTTAG, "Privacy Policy")
                    true
                }

                R.id.contactUs -> {
                    Log.d(MAINFRAGMENTTAG, "Contact Us")
                    true
                }

                R.id.logout -> {
                    //Logout User

                    // Slide Bottom Bottom Nav For Better UX.
                    val bottomNav =
                        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
                    val bottomAnim =
                        AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom)
                    bottomNav.startAnimation(bottomAnim)

                    viewModel.logout()
                    findNavController().navigate(R.id.action_mainFragment_to_authentication2)
                    true
                }
                // Add more navigation items and their handling
                else -> false
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainBinding.bind(view)

        setupNavigationHeader()

        binding.btnConsult.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_consultDoctor)
        }

        setupUpcomingAppointmentsRecylerView()

        init()

        getPermissionsForVideoCall()
    }
    private fun setupUpcomingAppointmentsRecylerView() {
        upcomingAppointmentsAdapter = UpcomingAppointmentsAdapter(this,this)
        binding.rvUpcomingAppointments.apply {
            adapter = upcomingAppointmentsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
        fetchUpcomingAppointments()
    }

    private fun fetchUpcomingAppointments() {
        lifecycleScope.launch {
            appointmentViewModel.upcomingAppointments.collect {
                when (it) {
                    is NetworkResult.Error -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Problem in fetching Upcoming Appointments",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.pbAppointment.visibility = View.GONE
                            Log.d(FETCHAPPOINTMENTS, "Error:- "+it.message.toString())
                        }
                    }

                    is NetworkResult.Loading -> {
                        withContext(Dispatchers.Main){
                            binding.pbAppointment.visibility = View.VISIBLE
                        }
                        Log.d(FETCHAPPOINTMENTS, "Loading:- "+it.message.toString())
                    }

                    is NetworkResult.Success -> withContext(Dispatchers.Main) {
                        binding.pbAppointment.visibility = View.GONE
                        upcomingAppointmentsAdapter.setData(it.data?.toList()!!)
                        Log.d(FETCHAPPOINTMENTS, "Success")
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun setupNavigationHeader() {
        val headerLayout = navigationView.getHeaderView(0)
        val userPic = headerLayout.findViewById<ImageView>(R.id.ivUserPic)
        val name = headerLayout.findViewById<TextView>(R.id.tvName)
        val email = headerLayout.findViewById<TextView>(R.id.tvEmail)

        val currentUser = firebaseAuth.currentUser

        Log.d(HEADERLAYOUTTAG, "Display Name:- ${currentUser?.displayName.toString()}")
        Log.d(HEADERLAYOUTTAG, "Photo Url:- ${currentUser?.photoUrl.toString()}")
        Log.d(HEADERLAYOUTTAG, "Phone Number:- ${currentUser?.phoneNumber.toString()}")



        if (currentUser?.phoneNumber.toString() == "" || currentUser?.phoneNumber == null) {
            Glide.with(this).load(currentUser?.photoUrl).centerCrop().into(userPic)
        } else {

            val hiddenPhoneNumberText =
                "+91${currentUser?.phoneNumber?.get(3)}${currentUser?.phoneNumber?.get(4)}******${
                    currentUser?.phoneNumber?.get(
                        11
                    )
                }${
                    currentUser?.phoneNumber?.get(12)
                }"
            name.text = hiddenPhoneNumberText
            binding.tvPersonName.text = hiddenPhoneNumberText
        }


        if (currentUser?.displayName.toString() == "" || currentUser?.displayName == null) {
            Log.d(HEADERLAYOUTTAG, "User Logged In Using Phone Number")
        } else {
            name.text = currentUser?.displayName.toString()
            binding.tvPersonName.text = currentUser?.displayName.toString()
        }

        firebaseAuth.currentUser?.email?.let {
            email.text = it
        }


    }


    override fun onClick(userAppointment: DetailedUserAppointment) {
        val action = MainFragmentDirections.actionMainFragmentToChattingFragment(userAppointment)
        findNavController().navigate(action)
    }

    private fun init(){
        socketRepository = SocketRepository(this)
        uid?.let { socketRepository?.initSocket(it) }
        rtcClient = RTCClient(activity?.application!!,uid!!,socketRepository!!, object : PeerConnectionObserver() {
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
                    Toast.makeText(requireContext(),"you should accept all permissions", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onclick(doctorUid: String) {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            ).request{ allGranted, _ ,_ ->
                if (allGranted){
                    targetUID = doctorUid
                    socketRepository?.sendMessageToSocket(MessageModel(
                        "start_call",uid,targetUID,null
                    ))
                    bottomNavigationVisibilityListener?.setBottomNavigationVisibility(false)
                } else {
                    Toast.makeText(requireContext(),"you should accept all permissions", Toast.LENGTH_LONG).show()
                }
            }
    }


    override fun onNewMessage(message: MessageModel) {
        when(message.type){
            "call_response"->{
                if (message.data == "user is not online"){
                    //user is not reachable
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(),"user is not reachable", Toast.LENGTH_LONG).show()

                        }
                    }
                }else{
                    //we are ready for call, we started a call
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main){
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
            }
            "answer_received" ->{

                val session = SessionDescription(
                    SessionDescription.Type.ANSWER,
                    message.data.toString()
                )
                rtcClient?.onRemoteSessionReceived(session)
                lifecycleScope.launch {
                    withContext(Dispatchers.Main){
                        binding?.remoteViewLoading?.visibility = View.GONE
                    }
                }
            }
            "offer_received" ->{
                Log.d("OFEERWEBRTC","Recived")

                lifecycleScope.launch {
                    withContext(Dispatchers.Main){
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationVisibilityListener) {
            bottomNavigationVisibilityListener = context
        } else {
            throw RuntimeException("$context must implement BottomNavigationVisibilityListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        bottomNavigationVisibilityListener = null
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        rtcClient?.endCall() // Close any existing WebRTC connections
        rtcClient = null
        socketRepository.closeConnection()
    }
}
