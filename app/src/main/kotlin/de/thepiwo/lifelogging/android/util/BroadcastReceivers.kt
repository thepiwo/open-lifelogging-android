package de.thepiwo.lifelogging.android.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationResult
import dagger.hilt.android.AndroidEntryPoint
import de.thepiwo.lifelogging.android.api.models.LogEntryInsert
import de.thepiwo.lifelogging.android.api.models.logentities.CoordEntity
import java.io.File
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

@AndroidEntryPoint
class LocationChangedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataHandler: DataHandler

    override fun onReceive(context: Context, intent: Intent) {
        val filePath = File(context.getExternalFilesDir(null), "location-error-log.json")

        if (LocationResult.hasResult(intent)) {
            val locations = LocationResult.extractResult(intent)?.locations
            locations?.forEach { location ->
                Log.i("LocationChangedReceiver", "onLocationChanged: $location")
                val logCoordEntity = CoordEntity(location.latitude, location.longitude, location.altitude, location.accuracy)
                dataHandler.createLogItem(LogEntryInsert(logCoordEntity), filePath)
            }
        }
    }
}