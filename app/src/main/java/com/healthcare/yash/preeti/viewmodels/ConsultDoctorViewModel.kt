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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConsultDoctorViewModel @Inject constructor(private val consultDoctorRepository: ConsultDoctorRepository) :
    ViewModel() {

    private val _doctorsListFlow = MutableStateFlow<NetworkResult<List<Doctor>>?>(null)
    val doctorsListFlow: StateFlow<NetworkResult<List<Doctor>>?> = _doctorsListFlow

    init {
        getAllDoctorsListInYourArea()
    }

    fun getAllDoctorsListInYourArea() = viewModelScope.launch {
        _doctorsListFlow.value = NetworkResult.Loading()

        // Fetching the doctor list
        try {
            val result = consultDoctorRepository.fetchDoctorsInYourArea()
            result.catch { e ->
                _doctorsListFlow.value = NetworkResult.Error("Error fetching data: ${e.message}")
            }.collect { doctorList ->
                if (doctorList.isNotEmpty()) {
                    _doctorsListFlow.value = NetworkResult.Success(doctorList)
                } else {
                    _doctorsListFlow.value = NetworkResult.Error("Data is null or empty")
                }
            }
        } catch (e: Exception) {
            _doctorsListFlow.value = NetworkResult.Error("Error fetching data: ${e.message}")
        }

    }
}