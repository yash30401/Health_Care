package com.healthcare.yash.preeti.viewmodels

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.healthcare.yash.preeti.models.GoogleSignInResult
import com.healthcare.yash.preeti.models.UserData
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val _loginFlow = MutableStateFlow<NetworkResult<FirebaseUser>?>(null)
    val loginFlow: StateFlow<NetworkResult<FirebaseUser>?> = _loginFlow

    private val _googleSinginState = MutableStateFlow<NetworkResult<UserData>?>(null)
    val googleSignInState: StateFlow<NetworkResult<UserData>?> = _googleSinginState

    //Getting Current User
    val currentUser: FirebaseUser?
        get() = repository.currentUser

    //If current user not null then passing NetworkResult.Success()
    init {
        if (repository.currentUser != null) {
            _loginFlow.value = NetworkResult.Success(repository.currentUser)
        }
    }

    fun signinWithPhoneNumber(credential: PhoneAuthCredential) = viewModelScope.launch {
        _loginFlow.value = NetworkResult.Loading()
        val result = repository.signinWithPhoneNo(credential)
        _loginFlow.value = result
    }

    fun onGoogleSignInResult(result: GoogleSignInResult) = viewModelScope.launch {
        _googleSinginState.value = NetworkResult.Loading()
        val result = result.data
        _googleSinginState.value = NetworkResult.Success(result!!)
    }



    fun logout() {
        repository.logout()
        _loginFlow.value = null
    }
}