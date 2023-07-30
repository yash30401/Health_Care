package com.healthcare.yash.preeti.other

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import com.healthcare.yash.preeti.models.Doctor

class ConsultDoctorDiffUtil<T> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem === newItem
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

}