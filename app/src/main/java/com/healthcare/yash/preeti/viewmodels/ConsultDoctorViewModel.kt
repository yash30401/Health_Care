package com.healthcare.yash.preeti.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.yash.preeti.models.Doctor
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants
import com.healthcare.yash.preeti.repositories.ConsultDoctorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConsultDoctorViewModel @Inject constructor(private val consultDoctorRepository: ConsultDoctorRepository):ViewModel() {

    private val _doctorsListFlow = MutableStateFlow<NetworkResult<List<Doctor>>?>(null)
    val doctorsListFlow:StateFlow<NetworkResult<List<Doctor>>?> = _doctorsListFlow

    fun getAllDoctorsListInYourArea() = viewModelScope.launch{
        _doctorsListFlow.value = NetworkResult.Loading()
        val result = consultDoctorRepository.fetchDoctorsInYourArea().data
        Log.d(Constants.CONSULTDOCTORFRAGTESTTAG,"DATA:- ${result.toString()}")
        if(result!=null){
            _doctorsListFlow.value = NetworkResult.Success(result!!)
        }else{
           _doctorsListFlow.value= NetworkResult.Error("Data is null or empty")
        }


    }
}