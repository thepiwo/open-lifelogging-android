package de.thepiwo.lifelogging.android.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataHandler: DataHandler

    override fun onReceive(context: Context, intent: Intent) {
        dataHandler.startLocationServiceObserver(context)
    }
}

@AndroidEntryPoint
class WakeLockReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataHandler: DataHandler

    override fun onReceive(context: Context, intent: Intent) {
        dataHandler.checkLocationServiceRunning(context)
    }
}
