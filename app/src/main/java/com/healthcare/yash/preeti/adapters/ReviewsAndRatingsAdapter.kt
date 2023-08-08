package com.healthcare.yash.preeti.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.ReviewsItemLayoutBinding
import com.healthcare.yash.preeti.models.ReviewsAndRatings
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class ReviewsAndRatingsAdapter(private val reviewsAndRatingsList: List<ReviewsAndRatings>) :
    RecyclerView.Adapter<ReviewsAndRatingsAdapter.ReviewsAndRatingViewHolder>() {

    inner class ReviewsAndRatingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ReviewsItemLayoutBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsAndRatingViewHolder {
        val viewHolder = ReviewsAndRatingViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.reviews_item_layout, parent, false)
        )

        return viewHolder
    }

    override fun getItemCount(): Int {
        return reviewsAndRatingsList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ReviewsAndRatingViewHolder, position: Int) {
        val currentReview = reviewsAndRatingsList[position]

        holder.binding.tvReviewPersonName.text = currentReview.name
        holder.binding.reviewRatingCard.tvDoctorRating.text = currentReview.rating.toString()

        val date = formatDateDifference(currentReview.date)
        holder.binding.tvDaysAgo.text = date


        holder.binding.tvReview.text = currentReview.review
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDateDifference(dateString: String): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val parsedDate = LocalDate.parse(dateString, formatter)
        val currentDate = LocalDate.now()

        val period = Period.between(parsedDate, currentDate)
        val years = period.years
        val months = period.months
        val days = period.days

        return when {
            years > 0 -> "$years years ago"
            months > 0 -> "$months months ago"
            days > 0 -> "$days days ago"
            else -> "Today"
        }
    }

}