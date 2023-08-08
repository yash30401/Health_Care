package com.healthcare.yash.preeti.adapters

import android.util.Log
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
import com.healthcare.yash.preeti.other.Constants.CONSULTDOCTORFRAGTESTTAG
import com.healthcare.yash.preeti.other.ConsultDoctorDiffUtil
import com.healthcare.yash.preeti.other.OnConsultDoctorClickListner

class ConsultDoctorAdapter(val doctorClickListner: OnConsultDoctorClickListner) :
    RecyclerView.Adapter<ConsultDoctorAdapter.ConsultDoctorViewHolder>() {

    private val doctorsList = emptyList<Doctor>()
    private val asyncListDiffer = AsyncListDiffer<Doctor>(this, ConsultDoctorDiffUtil())

    inner class ConsultDoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = DoctorItemLayoutBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsultDoctorViewHolder {
        val viewHolder = ConsultDoctorViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.doctor_item_layout, parent, false)
        )

        viewHolder.binding.itemCardView.setOnClickListener {
            doctorClickListner.onClick(asyncListDiffer.currentList[viewHolder.adapterPosition])
        }

        return viewHolder
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: ConsultDoctorViewHolder, position: Int) {
        val doctor = asyncListDiffer.currentList[position]

        Log.d(CONSULTDOCTORFRAGTESTTAG, "Doctor Name: ${doctor.Name}")
        Glide.with(holder.itemView).load(doctor.Profile_Pic.toUri()).diskCacheStrategy(
            DiskCacheStrategy.DATA
        ).into(holder.binding.ivDoctor)

        holder.binding.tvDoctorName.text = doctor.Name
        holder.binding.tvDoctorBio.text = doctor.About

        var averageRating: Double = 0.0
        var itemNo = 0
        doctor.Reviews_And_Ratings.forEach {
            val rating = it.rating.toDouble()
            averageRating += rating
            itemNo++
        }

        val formattedRating = String.format("%.1f", averageRating / doctor.Reviews_And_Ratings.size)
        holder.binding.tvDoctorRating.text = formattedRating
        Log.d("SIZECHECK","${doctor.Name}: "+itemNo.toString())

        if (doctor.Specialization.length > 10) {
            holder.binding.tvDoctorSpecialization.text =
                "${doctor.Specialization.subSequence(0, 10)}..."
        } else {
            holder.binding.tvDoctorSpecialization.text = doctor.Specialization
        }

    }

    fun setData(newList: List<Doctor>) {
        asyncListDiffer.submitList(newList)
    }

}