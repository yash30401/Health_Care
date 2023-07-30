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
import com.healthcare.yash.preeti.databinding.DoctorItemLayoutBinding
import com.healthcare.yash.preeti.models.Doctor
import com.healthcare.yash.preeti.other.ConsultDoctorDiffUtil

class ConsultDoctorAdapter : RecyclerView.Adapter<ConsultDoctorAdapter.ConsultDoctorViewHolder>() {

    private val doctorsList = emptyList<Doctor>()
    private val asyncListDiffer = AsyncListDiffer<Doctor>(this,ConsultDoctorDiffUtil())
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
        return asyncListDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: ConsultDoctorViewHolder, position: Int) {
        val doctor = asyncListDiffer.currentList[position]


        Glide.with(holder.itemView).load(doctor.profilePic.toUri()).diskCacheStrategy(
            DiskCacheStrategy.DATA
        ).into(holder.binding.ivDoctor)

        holder.binding.tvDoctorName.text = doctor.name
        holder.binding.tvDoctorBio.text = doctor.about
        holder.binding.tvDoctorRating.text = doctor.reviewsAndRatings[position].rating.toString()
        holder.binding.tvDoctorSpecialization.text = doctor.specialization
    }

    fun setData(newList:List<Doctor>){
        asyncListDiffer.submitList(newList)
    }

}