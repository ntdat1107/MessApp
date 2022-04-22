package com.example.messapp.models

data class UserStatus(
    var name: String,
    var profileImage: String,
    var lastUpdate: Long,
    var statusList: ArrayList<Status> = ArrayList(),
)