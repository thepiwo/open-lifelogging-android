package de.thepiwo.lifelogging.android.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.thepiwo.lifelogging.android.Application
import javax.inject.Inject


class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataProxy: DataHandler

    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as Application).component.inject(this)
        dataProxy.startLocationService(context)
    }
}

class LocationChangedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataProxy: DataHandler

    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as Application).component.inject(this)
        dataProxy.startLocationService(context)
    }
}