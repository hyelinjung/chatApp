package com.example.fire.data

import com.google.firebase.database.Exclude

data class ChatRoomSendData(val message:String="",val sender:String="",val time:String?="",var connect:Int=0,val nickname:String="",val hour:String=""){
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "message" to message,
            "sender" to sender,
            "time" to time,
            "connect" to connect,
            "nickname" to nickname,
            "hour" to hour,

        )
    }
}
