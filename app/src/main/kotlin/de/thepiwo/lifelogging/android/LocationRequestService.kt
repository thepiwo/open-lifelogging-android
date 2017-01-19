package de.thepiwo.lifelogging.android

import android.annotation.TargetApi
import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import de.thepiwo.lifelogging.android.api.models.LogEntryInsert
import de.thepiwo.lifelogging.android.api.models.logentities.CoordEntity
import de.thepiwo.lifelogging.android.util.AuthHelper
import de.thepiwo.lifelogging.android.util.DataHandler
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class LocationRequestService : IntentService("location-request-service") {

    @Inject
    lateinit var dataHandler: DataHandler

    @Inject
    lateinit var authHelper: AuthHelper

    override fun onCreate() {
        super.onCreate()
    }

    var lastLocationUpdate = 0L

    @TargetApi(16)
    override fun onHandleIntent(intent: Intent?) {

        (applicationContext as Application).component.inject(this)

        Log.i("LocationRequestService", "Service triggered")

        if (authHelper.sessionIsAuthorized() && authHelper.getLocationAllowed()) {
            Log.i("LocationRequestService", "trying to start location listener")

            if (lastLocationUpdate + TimeUnit.MINUTES.toMillis(30) < System.currentTimeMillis()) {
                val gApi = dataHandler.getGoogleApiClient(this)

                Log.i("LocationRequestService", "location listener maybe running; restarting")
                LocationServices.FusedLocationApi.removeLocationUpdates(gApi, locationListener)
                val locationRequest = LocationRequest()
                        .setInterval(TimeUnit.MINUTES.toMillis(10))
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)

                LocationServices.FusedLocationApi.requestLocationUpdates(gApi, locationRequest, locationListener)
                Log.i("LocationRequestService", "started location listener")
                dataHandler.showBasicNotification(this, "started location listener")
            } else {
                Log.i("LocationRequestService", "location listener is apparently running")
            }
        }
    }

    val locationListener = LocationListener { location ->
        Log.i("LocationListener", "onLocationChanged: $location")
        lastLocationUpdate = System.currentTimeMillis()
        val logCoordEntity = CoordEntity(null, null, location.latitude, location.longitude, location.altitude, location.accuracy)
        dataHandler.createLogItem(LogEntryInsert(logCoordEntity))
    }
}