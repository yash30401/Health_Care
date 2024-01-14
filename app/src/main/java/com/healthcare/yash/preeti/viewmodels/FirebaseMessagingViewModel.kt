package com.healthcare.yash.preeti.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.repositories.FirebaseMessagingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class FirebaseMessagingViewModel @Inject constructor(
    private val firebaseMessagingRepository: FirebaseMessagingRepository
):ViewModel() {

    private val _token = MutableStateFlow<NetworkResult<String>?>(null)
    val token: StateFlow<NetworkResult<String>?> = _token

    private val _apiCall = MutableStateFlow<NetworkResult<String>?>(null)
    val apiCall:StateFlow<NetworkResult<String>?> = _apiCall
    fun getFCMToken() = viewModelScope.launch {
        _token.value = NetworkResult.Loading()

        try {
            val result = firebaseMessagingRepository.getFCMToken()
            result.collect{
                when(it){
                    is NetworkResult.Error -> _token.value = NetworkResult.Error(it.message)
                    is NetworkResult.Loading -> _token.value = NetworkResult.Loading()
                    is NetworkResult.Success -> _token.value = NetworkResult.Success(it.data!!)
                }
            }
        }catch (
        e:Exception){
            _token.value = NetworkResult.Error(e.message)
        }
    }

    fun callApi(jsonObject: JSONObject)=viewModelScope.launch{
        _apiCall.value = NetworkResult.Loading()
        try {
            val result = firebaseMessagingRepository.callApi(jsonObject)
            result.collect{
                when(it){
                    is NetworkResult.Error -> _apiCall.value = NetworkResult.Error(it.message)
                    is NetworkResult.Loading -> _apiCall.value = NetworkResult.Loading()
                    is NetworkResult.Success -> _apiCall.value = NetworkResult.Success(it.data!!)
                }
            }
        }catch (e:Exception){
            _apiCall.value = NetworkResult.Error(e.message)
        }
    }
}