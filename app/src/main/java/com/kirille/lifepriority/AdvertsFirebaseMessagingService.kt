package com.kirille.lifepriority

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class AdvertsFirebaseMessagingService : FirebaseMessagingService(){
    private val firebaseServiceTag = "FirebaseServiceTag"
    private val tagMsg = "MsgRag"

    override fun onNewToken(p0: String) {
        Log.d(firebaseServiceTag, "Refreshed token: $p0")

    }

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.d(tagMsg, "From: ${p0.from}")
    }
}