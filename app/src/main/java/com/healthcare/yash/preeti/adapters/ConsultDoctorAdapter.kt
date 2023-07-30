package com.healthcare.yash.preeti.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.DoctorItemLayoutBinding
import com.healthcare.yash.preeti.models.Doctor

class ConsultDoctorAdapter : RecyclerView.Adapter<ConsultDoctorAdapter.ConsultDoctorViewHolder>() {

    private val doctorsList = emptyList<Doctor>()

    inner class ConsultDoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = DoctorItemLayoutBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsultDoctorViewHolder {
        val viewHolder = ConsultDoctorViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.doctor_item_layout, parent, false)
        )

        return viewHolder
    }

    override fun getItemCount(): Int {
        return doctorsList.size
    }

    override fun onBindViewHolder(holder: ConsultDoctorViewHolder, position: Int) {
        val doctor = doctorsList[position]


    }

}