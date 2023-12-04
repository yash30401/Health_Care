package com.healthcare.yash.preeti.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.yash.preeti.models.DoctorAppointment
import com.healthcare.yash.preeti.models.UserAppointment
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.repositories.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppointmentViewModel @Inject constructor(private val appointmentRepository: AppointmentRepository):
    ViewModel() {

    private val _addAppointment = MutableStateFlow<NetworkResult<String>?>(null)
    val addAppointment: StateFlow<NetworkResult<String>?> = _addAppointment

    fun addAppointmentToTheFirebase(
        userAppointment: UserAppointment,
        doctorUid:String,
        doctorAppointment: DoctorAppointment
    ) = viewModelScope.launch {
        _addAppointment.value = NetworkResult.Loading()

        val result = appointmentRepository.addAppointmentToTheFirebase(userAppointment,doctorUid,doctorAppointment)
        result.collect{
            when(it){
                is NetworkResult.Error -> _addAppointment.value = NetworkResult.Error(it.message.toString())
                is NetworkResult.Loading -> _addAppointment.value = NetworkResult.Loading()
                is NetworkResult.Success -> _addAppointment.value = NetworkResult.Success(it.data.toString())
            }
        }

    }
}
