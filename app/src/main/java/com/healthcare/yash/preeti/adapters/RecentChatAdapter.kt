package com.healthcare.yash.preeti.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.RecentChatItemLayoutBinding
import com.healthcare.yash.preeti.models.ChatRoom
import com.healthcare.yash.preeti.models.DoctorChatData
import com.healthcare.yash.preeti.utils.ConsultDoctorDiffUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentChatAdapter : RecyclerView.Adapter<RecentChatAdapter.RecentChatViewHolder>() {

    private val asyncListDiffer =
        AsyncListDiffer<Pair<ChatRoom, DoctorChatData>>(this, ConsultDoctorDiffUtil())

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

        Glide.with(holder.itemView).load(recentChat.second.profile.toUri())
            .into(holder.binding.ivDoctor)

        holder.binding.tvDoctorName.text = recentChat.second.name
        holder.binding.tvDoctorSpecialization.text = recentChat.second.specialization
        holder.binding.tvLastMesaage.text = recentChat.first.lastMessage
        holder.binding.tvLastTimeStamp.text =
            convertTimestampToTimeString(recentChat.first.lastMessageTimestamp.toDate().time)

    }

    fun convertTimestampToTimeString(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }

    fun setNewRecentChat(newList: List<Pair<ChatRoom, DoctorChatData>>) {
        asyncListDiffer.submitList(newList)
    }
}