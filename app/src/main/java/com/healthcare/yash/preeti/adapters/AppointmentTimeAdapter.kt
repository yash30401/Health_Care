package com.healthcare.yash.preeti.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.AppointmentTimeChipsBinding
import com.healthcare.yash.preeti.utils.ConsultDoctorDiffUtil

class AppointmentTimeAdapter(private val requireActivity: FragmentActivity,private val recyclerView: RecyclerView):RecyclerView.Adapter<AppointmentTimeAdapter.AppointmentTimeViewHolder>() {

    private val timingChipList = emptyList<String>()
    private val asynListDiffer = AsyncListDiffer<String>(this,ConsultDoctorDiffUtil())

    var singleSelection = false
    private var selectedTime = ArrayList<String>(1)

    private var lastPosition = RecyclerView.NO_POSITION

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

        holder.binding.chip.setOnClickListener {
                applySelection(holder,currentTimingChip)
        }
    }

    @SuppressLint("ResourceAsColor", "ResourceType")
    private fun applySelection(holder: AppointmentTimeViewHolder, currentTimingChip: String) {
        if(singleSelection==false){
            singleSelection = true
                holder.binding.chip.chipStrokeWidth = 4f
                holder.binding.chip.setChipStrokeColorResource(R.color.primaryColor)
                holder.binding.chip.setChipBackgroundColorResource(R.color.specialistCardBackgroundColor)
                lastPosition = holder.adapterPosition
        }else{
            if(holder.position != lastPosition){
                changeAppearanceOfLastPositionChipToDefault(lastPosition)
                changeAppearanceOfNewChipToNew(holder)
                lastPosition = holder.adapterPosition


            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun changeAppearanceOfNewChipToNew(holder: AppointmentTimeViewHolder) {
        holder.binding.chip.chipStrokeWidth = 4f
        holder.binding.chip.setChipStrokeColorResource(R.color.primaryColor)
        holder.binding.chip.setChipBackgroundColorResource(R.color.specialistCardBackgroundColor)
    }

    @SuppressLint("ResourceAsColor")
    private fun changeAppearanceOfLastPositionChipToDefault(position: Int) {
        if(position!=RecyclerView.NO_POSITION){
            val lastSelectedHolder = recyclerView.findViewHolderForAdapterPosition(position) as? AppointmentTimeViewHolder
            lastSelectedHolder?.let {
                it.binding.chip.chipStrokeWidth = 3f
                it.binding.chip.setChipStrokeColorResource(R.color.cardStrokeColor)
                it.binding.chip.setChipBackgroundColorResource(R.color.white)
            }
        }
    }

    fun setData(newList:List<String>){
        asynListDiffer.submitList(newList)
    }
}