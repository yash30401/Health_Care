package com.healthcare.yash.preeti.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.yash.preeti.models.ChatRoom
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.repositories.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(private val chatRepository: ChatRepository):ViewModel() {
    private val _getOrCreateChatRoom = MutableStateFlow<NetworkResult<ChatRoom>?>(null)
    val getOrCreateChatRoom:StateFlow<NetworkResult<ChatRoom>?> = _getOrCreateChatRoom

    fun getOrCreateChatRoom(doctorId:String) = viewModelScope.launch {
        _getOrCreateChatRoom.value = NetworkResult.Loading()

        try {
            val result = chatRepository.getOrCreateChatRoom(doctorId)
            result.collect{
                when(it){
                    is NetworkResult.Error ->  _getOrCreateChatRoom.value = NetworkResult.Error(it.message)
                    is NetworkResult.Loading -> _getOrCreateChatRoom.value = NetworkResult.Loading()
                    is NetworkResult.Success -> _getOrCreateChatRoom.value = NetworkResult.Success(it.data!!)
                }
            }
        }catch (e:Exception){
            _getOrCreateChatRoom.value = NetworkResult.Error(e.message)
        }
    }
}