package com.healthcare.yash.preeti.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.ChipItemLayoutBinding

class ServicesChipAdatpter(private val servicesChipList: List<String>) :
    RecyclerView.Adapter<ServicesChipAdatpter.ServicesChipViewHolder>() {
    inner class ServicesChipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ChipItemLayoutBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicesChipViewHolder {
        val viewHolder = ServicesChipViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.chip_item_layout, parent, false)
        )

        return viewHolder
    }

    override fun getItemCount(): Int {
        return servicesChipList.size
    }

    override fun onBindViewHolder(holder: ServicesChipViewHolder, position: Int) {
        val currentChip = servicesChipList[position]

        holder.binding.chip.chipText = currentChip
    }
}