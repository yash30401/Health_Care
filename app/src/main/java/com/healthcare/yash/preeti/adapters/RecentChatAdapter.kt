package com.healthcare.yash.preeti.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.RecentChatItemLayoutBinding
import com.healthcare.yash.preeti.models.ChatRoom
import com.healthcare.yash.preeti.utils.ConsultDoctorDiffUtil

class RecentChatAdapter : RecyclerView.Adapter<RecentChatAdapter.RecentChatViewHolder>() {

    private val asyncListDiffer = AsyncListDiffer<ChatRoom>(this, ConsultDoctorDiffUtil())

    inner class RecentChatViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        val binding = RecentChatItemLayoutBinding.bind(itemview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentChatViewHolder {
        val viewHolder = RecentChatViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recent_chat_item_layout, parent, false)
        )

        return viewHolder
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: RecentChatViewHolder, position: Int) {
        val recentChat = asyncListDiffer.currentList[position]

//        Glide.with(holder.itemView).load(recentChat.)

    }
}