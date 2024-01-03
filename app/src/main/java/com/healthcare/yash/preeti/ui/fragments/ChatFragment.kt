package com.healthcare.yash.preeti.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.adapters.RecentChatAdapter
import com.healthcare.yash.preeti.databinding.FragmentChatBinding
import com.healthcare.yash.preeti.models.ChatRoom
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.viewmodels.ChatViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private var _binding:FragmentChatBinding?=null
    private val binding get()=_binding!!

    lateinit var recentChatAdapter: RecentChatAdapter

    private val chatViewModel by viewModels<ChatViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBinding.bind(view)

        setupRecentChatRecylerView()
    }

    private fun setupRecentChatRecylerView() {
        val recentChats = getRecentChats()
        recentChatAdapter = RecentChatAdapter()
        binding.rvChats.apply {
            adapter = recentChatAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun getRecentChats(): List<ChatRoom> {
        lifecycleScope.launch{
            chatViewModel.getChatMessages()
            chatViewModel.recentChats.collect{
                when(it){
                    is NetworkResult.Error -> TODO()
                    is NetworkResult.Loading -> TODO()
                    is NetworkResult.Success -> TODO()
                    null -> TODO()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}