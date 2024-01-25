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
import com.healthcare.yash.preeti.other.ChatClickListner
import com.healthcare.yash.preeti.utils.ConsultDoctorDiffUtil
import java.text.SimpleDateFormat
import java.util.Locale

class UpcomingAppointmentsAdapter(private val chatClickListner: ChatClickListner, private val videoCallClickListner: VideoCallClickListner):RecyclerView.Adapter<UpcomingAppointmentsAdapter.UpcomingAppointmentsViewHolder>() {

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

        holder.binding.tvDoctorNameInAppointment.text = userAppointment.name
        holder.binding.tvDoctorSpecializationInAppointment.text = userAppointment.specialization

        holder.binding.tvAppointmentStatus.text = "Status - ${userAppointment.status}"
        holder.binding.tvAppointmentConsultText.text = userAppointment.typeOfConsultation

        if(userAppointment.typeOfConsultation == "Clinic Visit") {
            holder.binding.cvVideoCall.visibility = View.GONE
            holder.binding.cvChat.visibility = View.VISIBLE
        }

        val timeStampDate = userAppointment.dateTime?.toDate()
        val simpleDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val formattedDate = simpleDateFormat.format(timeStampDate)

        val timeStampTime = userAppointment.dateTime?.toDate()?.time
        val simpleDateFormatTime = SimpleDateFormat("h a", Locale.getDefault())
        val formattedTime = simpleDateFormatTime.format(timeStampTime)

        holder.binding.tvAppointmentDate.text = formattedDate.toString()
        holder.binding.tvAppointmentTime.text = formattedTime.toString()

        holder.binding.cvChat.setOnClickListener {
            chatClickListner.onClick(userAppointment)
        }

        holder.binding.cvVideoCall.setOnClickListener {
            videoCallClickListner.onclick(userAppointment.doctorUId)
        }
    }

    fun setData(newList:List<DetailedUserAppointment>){
        asyncListDiffer.submitList(newList)
    }

    interface VideoCallClickListner{
        fun onclick(doctorUid: String)
    }
}

