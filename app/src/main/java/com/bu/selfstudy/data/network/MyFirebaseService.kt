package com.bu.selfstudy.data.network

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.bu.selfstudy.tool.log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseService: FirebaseMessagingService(){
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

    }
}