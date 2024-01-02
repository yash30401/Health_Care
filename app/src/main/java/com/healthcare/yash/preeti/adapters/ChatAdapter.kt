package com.healthcare.yash.preeti.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.ChatMessageItemLayoutBinding
import com.healthcare.yash.preeti.models.ChatMessage
import com.healthcare.yash.preeti.utils.ConsultDoctorDiffUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val currentUserId:String) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    val asynListDiffer = AsyncListDiffer<ChatMessage>(this, ConsultDoctorDiffUtil())

    inner class ChatViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        val binding = ChatMessageItemLayoutBinding.bind(itemview)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val viewHolder = ChatViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_message_item_layout, parent, false)
        )

        return viewHolder
    }

    override fun getItemCount(): Int {
        return asynListDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val currentChat = asynListDiffer.currentList[position]

        if(currentChat.senderId == currentUserId){
            holder.binding.llLeft.visibility = View.GONE
            holder.binding.llRight.visibility = View.VISIBLE
            holder.binding.tvRightMessage.text = currentChat.message

            val timestamp = currentChat.timestamp.toDate().time
            val formattedTime = convertTimestampToTimeString(timestamp)
            holder.binding.tvRightTimestamp.text = formattedTime
        }else{
            holder.binding.llRight.visibility = View.GONE
            holder.binding.llLeft.visibility = View.VISIBLE
            holder.binding.tvLeftMessage.text = currentChat.message

            val timestamp = currentChat.timestamp.toDate().time
            val formattedTime = convertTimestampToTimeString(timestamp)
            holder.binding.tvLeftTimestamp.text = formattedTime
        }
    }

    fun setMessage(newMessage:List<ChatMessage>){
        asynListDiffer.submitList(newMessage)
    }

    fun convertTimestampToTimeString(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }
}