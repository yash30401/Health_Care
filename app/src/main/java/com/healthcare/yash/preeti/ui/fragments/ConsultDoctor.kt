package com.healthcare.yash.preeti.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.adapters.ConsultDoctorAdapter
import com.healthcare.yash.preeti.databinding.FragmentConsultDoctorBinding
import com.healthcare.yash.preeti.models.Doctor
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants.CONSULTDOCTORFRAGTESTTAG
import com.healthcare.yash.preeti.viewmodels.ConsultDoctorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


@AndroidEntryPoint
class ConsultDoctor : Fragment(R.layout.fragment_consult_doctor) {

    private var _binding: FragmentConsultDoctorBinding? = null
    private val binding get() = _binding!!

    private val consultDoctorViewModel by viewModels<ConsultDoctorViewModel>()
    private lateinit var consultDoctorAdapter: ConsultDoctorAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_consult_doctor, container, false)


        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentConsultDoctorBinding.bind(view)

        setupRecylerView()
    }

    private fun setupRecylerView() {
        consultDoctorAdapter = ConsultDoctorAdapter()
        binding.recyclerViewDoctor.apply {
            adapter = consultDoctorAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        lifecycleScope.launch {
            fetchDoctors()
        }
    }

    private suspend fun fetchDoctors() {
        suspendCancellableCoroutine{
            val data= consultDoctorViewModel.getAllDoctorsListInYourArea()
            it.resume(data)
        }
        consultDoctorViewModel.doctorsListFlow.collect{
            when(it){
                is NetworkResult.Error -> {
                    Log.d(CONSULTDOCTORFRAGTESTTAG,"Error:- "+it.message.toString())
                    Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Loading -> {

                }
                is NetworkResult.Success -> {
                    consultDoctorAdapter.setData(it.data!!)
                }
                else -> {
                    Log.d(CONSULTDOCTORFRAGTESTTAG,"ELSE:- "+it?.message.toString())
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}