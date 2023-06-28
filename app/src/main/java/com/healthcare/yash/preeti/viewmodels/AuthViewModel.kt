package com.healthcare.yash.preeti.viewmodels

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.CallbackManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.healthcare.yash.preeti.models.GoogleSignInResult
import com.healthcare.yash.preeti.models.UserData
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val _loginFlow = MutableStateFlow<NetworkResult<FirebaseUser>?>(null)
    val loginFlow: StateFlow<NetworkResult<FirebaseUser>?> = _loginFlow

    private val _googleSinginState = MutableStateFlow<NetworkResult<UserData>?>(null)
    val googleSignInState: StateFlow<NetworkResult<UserData>?> = _googleSinginState

    private val _facebookSigninState = MutableStateFlow<NetworkResult<FirebaseUser>?>(null)
    val facebookSigninState: StateFlow<NetworkResult<FirebaseUser>?> = _facebookSigninState

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

    fun signInWithFacebook(callbackManager: CallbackManager, fragment: Fragment) =
        viewModelScope.launch {
            _facebookSigninState.value = NetworkResult.Loading()
            val result = repository.signInWithFacebook(callbackManager, fragment)
            _facebookSigninState.value = NetworkResult.Success(result.data!!)
        }


    fun logout() {
        repository.logout()
        _loginFlow.value = null
    }
}