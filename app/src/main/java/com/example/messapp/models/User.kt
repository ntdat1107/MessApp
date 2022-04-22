package com.example.messapp.models

data class User(
    public val uid: String = "",
    public val name: String = "",
    public val profileImage: String = "",
    public val completed: Boolean = false
)