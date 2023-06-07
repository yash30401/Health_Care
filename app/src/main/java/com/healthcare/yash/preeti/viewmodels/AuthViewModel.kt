package com.healthcare.yash.preeti.viewmodels

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.healthcare.yash.preeti.repositories.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val context:Context,private val firebaseAuth:FirebaseAuth,private val activity:Activity):ViewModel() {

    private lateinit var repository:AuthRepository

    init {
        repository = AuthRepository(context, firebaseAuth, activity)
    }

    fun sendVerificationCodeToPhoneNumber(phoneNo: String) = viewModelScope.launch {
        repository.sendVerificationCodeToPhoneNumber(phoneNo)
    }
}