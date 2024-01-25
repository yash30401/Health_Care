package com.healthcare.yash.preeti.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.adapters.ChatAdapter
import com.healthcare.yash.preeti.databinding.FragmentChattingBinding
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants.CHATMESSAGE
import com.healthcare.yash.preeti.other.Constants.CHATROOMTESTING
import com.healthcare.yash.preeti.viewmodels.ChatViewModel
import com.healthcare.yash.preeti.viewmodels.FirebaseMessagingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class ChattingFragment : Fragment(R.layout.fragment_chatting) {

    private var _binding:FragmentChattingBinding?=null
    private val binding get()= _binding!!

    private val args:ChattingFragmentArgs by navArgs<ChattingFragmentArgs>()

    private val chatViewModel by viewModels<ChatViewModel>()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var chatAdapter: ChatAdapter

    private val firebaseMessagingViewModel by viewModels<FirebaseMessagingViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChattingBinding.bind(view)

        val toolbarLayout = binding.toolbar
        val toolbar:Toolbar  = toolbarLayout.toolbar


        activity?.setActionBar(toolbar)
        activity?.actionBar?.setDisplayShowTitleEnabled(false)
        activity?.actionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.actionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)

        val ivProfilePicture:ImageView = toolbarLayout.ivProfilePicture
        Glide.with(this).load(args.detailedUserAppointment.profileImage.toUri()).into(ivProfilePicture)

        toolbarLayout.tvDoctoreName.text = args.detailedUserAppointment.name

       toolbar.setNavigationOnClickListener {
           activity?.onBackPressed()
       }

        getOrCreateChatRoom()

        setupChatRecylerView()
        binding.ibSend.setOnClickListener {
            val message = binding.tilSendMessage.editText?.text.toString()
            if(message.isEmpty()){
                Toast.makeText(requireContext(), "Please Write Message!", Toast.LENGTH_SHORT).show()
            }else{
                sendMessageToTheUser(message)
            }
        }
    }

    private fun getOrCreateChatRoom() {
        lifecycleScope.launch {
            chatViewModel.getOrCreateChatRoom(args.detailedUserAppointment.doctorUId)
            chatViewModel.getOrCreateChatRoom.collect{
                when(it){
                    is NetworkResult.Error -> {
                        Log.d(CHATROOMTESTING,"Error Block:- ${it.message.toString()}")
                    }
                    is NetworkResult.Loading -> {
                        Log.d(CHATROOMTESTING,"Loading Block:- ${it.message.toString()}")
                    }
                    is NetworkResult.Success -> {
                        Log.d(CHATROOMTESTING,"Success Block:- ${it.data.toString()}")
                    }
                    else -> {}
                }
            }
        }
    }

    private fun sendMessageToTheUser(message: String) {
        lifecycleScope.launch {
            chatViewModel.sendMessageToTheUser(message)
            chatViewModel.sendMessage.collect {
                when (it) {
                    is NetworkResult.Error -> {
                        Log.d(CHATMESSAGE, "Error Block:- ${it.message.toString()}")
                    }

                    is NetworkResult.Loading -> {
                        Log.d(CHATMESSAGE, "Loading Block:- ${it.message.toString()}")
                    }

                    is NetworkResult.Success -> {
                        withContext(Dispatchers.Main) {
                            binding.tilSendMessage.editText?.text?.clear()
                            chatViewModel.getChatMessages()
                            sendNotification(message)
                        }
                        Log.d(CHATMESSAGE, "Success Block:- ${it.data.toString()}")
                    }

                    else -> {}
                }
            }
        }
    }

    private fun sendNotification(message: String) {
        try {
          val jsonObject = JSONObject()

          val notificationObj = JSONObject()
          notificationObj.put("title",args.detailedUserAppointment.name)
            notificationObj.put("body",message)

            val dataObj = JSONObject()
            dataObj.put("userId",firebaseAuth.currentUser?.uid.toString())

            jsonObject.put("notifcation",notificationObj)
            jsonObject.put("data",dataObj)
            jsonObject.put("to","eL2VrJyaSlmoB-SilCFmAQ:APA91bEZwe0oOiPOLkepSpQ63fU-O-7ipIwVuCnW9-9j0T3dvz1gR07siFuxDaLtVb5cR8W8xb-EaeAClbv9mbS3YPQslNwQAYDoEafv3lFy7yLL9q3_7_3U3FZOKfSOCOYNAQK8MiGA")

            firebaseMessagingViewModel.callApi(jsonObject)
            lifecycleScope.launch {
                firebaseMessagingViewModel.apiCall.collect{
                    when(it){
                        is NetworkResult.Error -> {
                            Log.d("APICALLTESTING","Error Block:- ${it.message}")
                        }
                        is NetworkResult.Loading -> {
                            Log.d("APICALLTESTING","Loading Block:- ${it.message}")
                        }
                        is NetworkResult.Success -> {
                            Log.d("APICALLTESTING","Success Block:- ${it.data}")
                        }
                        else -> {}
                    }
                }
            }

        }catch (e:Exception){
            Log.d("APICALLTESTING","JSON BLOCK:- ${e.message}")
        }
    }

    private fun setupChatRecylerView() {
        chatAdapter = ChatAdapter(firebaseAuth.currentUser?.uid.toString())
        binding.recyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,true)
        }

        chatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                binding.recyclerView.smoothScrollToPosition(0)
            }
        })

        lifecycleScope.launch{
            chatViewModel.getChatMessages()
            chatViewModel.chatMessages.collect{
                when(it){
                    is NetworkResult.Error -> {
                        Log.d(CHATMESSAGE, "Error Block:- ${it.message.toString()}")
                    }
                    is NetworkResult.Loading -> {
                        Log.d(CHATMESSAGE, "Loading Block:- ${it.message.toString()}")
                    }
                    is NetworkResult.Success -> {
                        withContext(Dispatchers.Main) {
                            chatAdapter.setMessage(it.data!!)
                        }
                        Log.d(CHATMESSAGE, "Success Block:- ${it.data.toString()}")
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}