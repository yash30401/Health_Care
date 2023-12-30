package com.healthcare.yash.preeti.other

import com.healthcare.yash.preeti.models.DetailedUserAppointment

interface ChatClickListner {
    fun onClick(userAppointment: DetailedUserAppointment)
}