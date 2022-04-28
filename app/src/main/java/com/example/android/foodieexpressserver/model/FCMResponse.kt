package com.example.android.foodieexpressserver.model

class FCMResponse {

    val multicast_Id : Long? = 0
    var success : Int = 0
    var failure :Int = 0
    var canonical_ids :Int = 0
    var results :List<FCMResult>? = null
    var message_id : Long = 0

}