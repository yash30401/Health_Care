package com.healthcare.yash.preeti.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.repositories.SlotsRepository
import com.healthcare.yash.preeti.ui.fragments.DoctorDetailedViewArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SlotViewModel @Inject constructor(
    private var slotsRepository: SlotsRepository
):ViewModel(){

    private val _allSlotFlow = MutableStateFlow<NetworkResult<List<Long>>?>(null)
    val allSlotFlow:StateFlow<NetworkResult<List<Long>>?> = _allSlotFlow

    fun getAllSlots(args: DoctorDetailedViewArgs) = viewModelScope.launch {
_allSlotFlow.value = NetworkResult.Loading()
val result =slotsRepository.getAllSlots(args)
        result.collect{
            when(it){
                is NetworkResult.Error -> {
                    _allSlotFlow.value = NetworkResult.Error(it.message.toString())
                }
                is NetworkResult.Loading -> {
                    _allSlotFlow.value = NetworkResult.Loading()
                }
                is NetworkResult.Success -> {
                    _allSlotFlow.value = NetworkResult.Success(it.data?.toList()!!)
                }
            }
        }
    }

}