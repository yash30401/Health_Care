package com.healthcare.yash.preeti.utils

import com.healthcare.yash.preeti.models.ReviewsAndRatings

fun  List<ReviewsAndRatings>.averageRating():Double{
    var averageRating: Double = 0.0
    forEach {
        val rating = it.rating
        averageRating += rating
    }
    return (averageRating/size)
}