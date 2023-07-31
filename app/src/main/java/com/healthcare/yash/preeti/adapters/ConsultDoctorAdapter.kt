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

        Log.d(CONSULTDOCTORFRAGTESTTAG, "Doctor Name: ${doctor.Name}")
        Glide.with(holder.itemView).load(doctor.Profile_Pic.toUri()).diskCacheStrategy(
            DiskCacheStrategy.DATA
        ).into(holder.binding.ivDoctor)

        holder.binding.tvDoctorName.text = doctor.Name
        holder.binding.tvDoctorBio.text = doctor.About
        if (doctor.Reviews_And_Ratings.isNotEmpty()) {
            // If there are reviews and ratings for the doctor, display the rating.
            // You can choose to display the first rating, the average rating, etc.
            val firstRating = doctor.Reviews_And_Ratings[0].rating
            holder.binding.tvDoctorRating.text = firstRating.toString()
        } else {
            // If there are no reviews and ratings, display a default value or a message.
            holder.binding.tvDoctorRating.text = "N/A"
        }
        holder.binding.tvDoctorSpecialization.text = doctor.Specialization
    }

    fun setData(newList:List<Doctor>){
        asyncListDiffer.submitList(newList)
    }

}