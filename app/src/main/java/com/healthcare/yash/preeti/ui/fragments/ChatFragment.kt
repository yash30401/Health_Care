package com.healthcare.yash.preeti.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.adapters.RecentChatAdapter
import com.healthcare.yash.preeti.databinding.FragmentChatBinding
import com.healthcare.yash.preeti.models.DetailedUserAppointment
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants.RECENTCHATS
import com.healthcare.yash.preeti.other.OnRecentChatClickListner
import com.healthcare.yash.preeti.viewmodels.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.fragment_chat),OnRecentChatClickListner {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    lateinit var recentChatAdapter: RecentChatAdapter

    private val chatViewModel by viewModels<ChatViewModel>()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBinding.bind(view)

        setupRecentChatRecylerView()

    }

    private fun setupRecentChatRecylerView() {
        recentChatAdapter = RecentChatAdapter(this,firebaseAuth)
        binding.rvChats.apply {
            adapter = recentChatAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        getRecentChats()
    }

    private fun getRecentChats() {
        Log.d(RECENTCHATS,"Entering Function")
        lifecycleScope.launch {
            chatViewModel.getRecentChats()
            chatViewModel.recentChats.collect {
                when (it) {
                    is NetworkResult.Error -> {
                        Log.d(RECENTCHATS, "Error Block:- ${it.message.toString()}")
                    }

                    is NetworkResult.Loading -> {
                        Log.d(RECENTCHATS, "Loading Block:- ${it.message.toString()}")
                    }

                    is NetworkResult.Success -> {
                        Log.d(RECENTCHATS, "Success Block:- ${it.data.toString()}")
                       withContext(Dispatchers.Main){
                           recentChatAdapter.setNewRecentChat(it.data!!)
                       }
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

    override fun onClick(detailedUserAppointment: DetailedUserAppointment?) {
        val action = ChatFragmentDirections.actionChatFragmentToChattingFragment(detailedUserAppointment!!)
        findNavController().navigate(action)
    }
}