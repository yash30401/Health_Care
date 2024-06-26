package com.healthcare.yash.preeti.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.yash.preeti.models.DetailedUserAppointment
import com.healthcare.yash.preeti.models.DoctorAppointment
import com.healthcare.yash.preeti.models.UserAppointment
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.repositories.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor(private val appointmentRepository: AppointmentRepository):
    ViewModel() {


    private val _addAppointment = MutableStateFlow<NetworkResult<String>?>(null)
    val addAppointment: StateFlow<NetworkResult<String>?> = _addAppointment

    private val _upcomingAppointments = MutableStateFlow<NetworkResult<List<DetailedUserAppointment>>?>(null)
    val upcomingAppointments:StateFlow<NetworkResult<List<DetailedUserAppointment>>?> = _upcomingAppointments

    init {
        getAllUpcomingAppointments()
    }

    fun addAppointmentToTheFirebase(
        userAppointment: UserAppointment,
        doctorUid:String,
        doctorAppointment: DoctorAppointment
    ) = viewModelScope.launch {
        _addAppointment.value = NetworkResult.Loading()
        Log.d("BLOCKCHECK","Function Calling")
        val result = appointmentRepository.addAppointmentToTheFirebase(userAppointment,doctorUid,doctorAppointment)
        result.collect{
            when(it){
                is NetworkResult.Error -> {_addAppointment.value = NetworkResult.Error(it.message.toString())
                Log.d("BLOCKCHECK","ViewModel:- Error")
                }
                is NetworkResult.Loading -> {_addAppointment.value = NetworkResult.Loading()
                    Log.d("BLOCKCHECK","ViewModel:- Loading")
                }
                is NetworkResult.Success -> {_addAppointment.value = NetworkResult.Success(it.data.toString())
                    Log.d("BLOCKCHECK","ViewModel:- Success     ")
                }
            }
        }

    }

    private fun getAllUpcomingAppointments() = viewModelScope.launch {
        _upcomingAppointments.value = NetworkResult.Loading()

        try {
            val result = appointmentRepository.getAllUpcomingAppointments()

            result.collect{
                when(it){
                    is NetworkResult.Error -> _upcomingAppointments.value = NetworkResult.Error(it.message.toString())
                    is NetworkResult.Loading -> _upcomingAppointments.value = NetworkResult.Loading()
                    is NetworkResult.Success -> _upcomingAppointments.value = NetworkResult.Success(it.data!!)
                }
            }
        }catch (e:Exception){
            _upcomingAppointments.value = NetworkResult.Error(e.message.toString())
        }

    }

}
