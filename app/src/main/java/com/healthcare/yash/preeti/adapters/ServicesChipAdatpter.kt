package com.healthcare.yash.preeti.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.ChipItemLayoutBinding
import com.healthcare.yash.preeti.other.ConsultDoctorDiffUtil

class ServicesChipAdatpter() :
    RecyclerView.Adapter<ServicesChipAdatpter.ServicesChipViewHolder>() {

    private val servicesChipList = emptyList<String>()
    private val asyncListDiffer = AsyncListDiffer<String>(this,ConsultDoctorDiffUtil())
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
        return asyncListDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: ServicesChipViewHolder, position: Int) {
        val currentChip = asyncListDiffer.currentList[position]

        holder.binding.chip.chipText = currentChip
    }

    fun setData(newList:List<String>){
        asyncListDiffer.submitList(newList)
    }
}