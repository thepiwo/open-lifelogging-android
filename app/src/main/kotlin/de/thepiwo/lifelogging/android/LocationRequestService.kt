package de.thepiwo.lifelogging.android

import android.annotation.TargetApi
import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import de.thepiwo.lifelogging.android.util.AuthHelper
import de.thepiwo.lifelogging.android.util.DataHandler
import de.thepiwo.lifelogging.android.util.LocationChangedReceiver
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


    @TargetApi(16)
    override fun onHandleIntent(intent: Intent?) {

        (applicationContext as Application).component.inject(this)

        Log.i("LocationRequestService", "Service triggered")

        if (authHelper.sessionIsAuthorized() && authHelper.getLocationAllowed()) {
            Log.i("LocationRequestService", "trying to start location listener")

            val gApi = dataHandler.getGoogleApiClient(this)

            Log.i("LocationRequestService", "location listener maybe running; restarting")
            val locationRequest = LocationRequest()
                    .setInterval(TimeUnit.MINUTES.toMillis(10))
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)

            val locationIntent = Intent(this, LocationChangedReceiver::class.java)
            val locationPIntent = PendingIntent.getBroadcast(this, 0, locationIntent, PendingIntent.FLAG_CANCEL_CURRENT)

            LocationServices.FusedLocationApi.requestLocationUpdates(gApi, locationRequest, locationPIntent)
            Log.i("LocationRequestService", "started location listener")
        }
    }
}