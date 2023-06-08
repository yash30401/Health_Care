package com.healthcare.yash.preeti.viewmodels

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.healthcare.yash.preeti.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val repository:AuthRepository):ViewModel() {


    fun sendVerificationCodeToPhoneNumber(phoneNo: String) = viewModelScope.launch {
//        repository.sendVerificationCodeToPhoneNumber(phoneNo)

    }
}