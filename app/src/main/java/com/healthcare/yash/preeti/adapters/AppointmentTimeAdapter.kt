package com.healthcare.yash.preeti.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.AppointmentTimeChipsBinding
import com.healthcare.yash.preeti.databinding.AppointmentTimingDialogBinding
import com.healthcare.yash.preeti.utils.ConsultDoctorDiffUtil

class AppointmentTimeAdapter():RecyclerView.Adapter<AppointmentTimeAdapter.AppointmentTimeViewHolder>() {

    private val timingChipList = emptyList<String>()
    private val asynListDiffer = AsyncListDiffer<String>(this,ConsultDoctorDiffUtil())
    inner class AppointmentTimeViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val binding = AppointmentTimeChipsBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentTimeViewHolder {
        val viewHolder = AppointmentTimeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.appointment_time_chips,parent,false))
        return viewHolder
    }

    override fun getItemCount(): Int {
        return asynListDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: AppointmentTimeViewHolder, position: Int) {
        val currentTimingChip = asynListDiffer.currentList[position]

        holder.binding.chip.chipText = currentTimingChip
    }

    fun setData(newList:List<String>){
        asynListDiffer.submitList(newList)
    }
}