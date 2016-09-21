package de.thepiwo.lifelogging.android.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.util.Log
import de.thepiwo.lifelogging.android.Application
import javax.inject.Inject


class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataHandler: DataHandler

    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as Application).component.inject(this)
        dataHandler.startLocationService(context)
    }
}

class LocationChangedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataHandler: DataHandler

    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as Application).component.inject(this)
        dataHandler.startLocationService(context)
    }
}

class WifiChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataHandler: DataHandler

    override fun onReceive(context: Context, intent: Intent) {
        val wifi = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        dataHandler.handleWifiInfo(wifi.connectionInfo)
    }
}