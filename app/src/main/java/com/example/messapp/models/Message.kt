package com.example.messapp.models

 data class Message(
     public var messageID: String = "",
     public val message: String = "",
     public val senderID: String = "",
     public val timestamp: Long = 0L,
     public var react: Int = -1,
     public var imageUrl: String = ""
 )