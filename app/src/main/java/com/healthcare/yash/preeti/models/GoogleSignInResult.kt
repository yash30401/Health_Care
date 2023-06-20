package com.healthcare.yash.preeti.models

data class GoogleSignInResult(val data: UserData?, val errorMessage: String?)

data class UserData(
    val userId:String,
    val userName:String?,
    val profilePictureUrl:String?
)