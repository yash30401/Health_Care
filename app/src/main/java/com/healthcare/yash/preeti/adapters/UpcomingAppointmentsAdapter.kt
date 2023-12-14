package com.healthcare.yash.preeti.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.AppointmentItemLayoutBinding
import com.healthcare.yash.preeti.models.DetailedUserAppointment
import com.healthcare.yash.preeti.models.UserAppointment
import com.healthcare.yash.preeti.utils.ConsultDoctorDiffUtil

class UpcomingAppointmentsAdapter:RecyclerView.Adapter<UpcomingAppointmentsAdapter.UpcomingAppointmentsViewHolder>() {

    private val asyncListDiffer = AsyncListDiffer<DetailedUserAppointment>(this,ConsultDoctorDiffUtil())

    inner class UpcomingAppointmentsViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val binding = AppointmentItemLayoutBinding.bind(itemView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UpcomingAppointmentsViewHolder {
        val viewHolder = UpcomingAppointmentsViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.appointment_item_layout,parent,false))

        return viewHolder
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: UpcomingAppointmentsViewHolder, position: Int) {
        val userAppointment = asyncListDiffer.currentList[position]

        Glide.with(holder.itemView).load(userAppointment.profileImage.toUri()).diskCacheStrategy(
            DiskCacheStrategy.DATA
        ).into(holder.binding.ivDoctorImageInAppointment)

        holder.binding.tvDoctorNameInAppointment = us
    }

    fun setData(newList:List<DetailedUserAppointment>){
        asyncListDiffer.submitList(newList)
    }
}