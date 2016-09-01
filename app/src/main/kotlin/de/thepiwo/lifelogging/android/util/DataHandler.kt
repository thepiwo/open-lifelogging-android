package de.thepiwo.lifelogging.android.util

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.mcxiaoke.koi.ext.getLocationManager
import de.thepiwo.lifelogging.android.api.LoggingApiService
import de.thepiwo.lifelogging.android.api.models.LogCoordEntity
import de.thepiwo.lifelogging.android.api.models.LogEntryInsert
import de.thepiwo.lifelogging.android.dagger.ForApplication
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

@ForApplication
class DataHandler
@Inject
constructor(val loggingApiService: LoggingApiService,
            val authHelper: AuthHelper) {


    fun login(): Observable<Boolean> {
        if (authHelper.sessionIsAvailable()) {
            val login = loggingApiService.login(authHelper.getLoginData()!!)
            return login.flatMap { t ->
                authHelper.setToken(t)
                return@flatMap Observable.just(true)
            }
        } else {
            return Observable.error(LoggingApiService.AuthorizationException())
        }
    }

    private val locationListener = object : LocationListener {
        override fun onProviderDisabled(p0: String?) {
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        }

        override fun onProviderEnabled(p0: String?) {
        }

        override fun onLocationChanged(location: Location) {
            Log.i("locationListener", "location: $location")
            val logCoordEntity = LogCoordEntity(null, null, location.latitude, location.longitude, location.altitude, location.accuracy)

            loggingApiService.createLogItem(LogEntryInsert(logCoordEntity))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                Log.i("DataHandler", "location sent")
                            },
                            { error ->
                                error.printStackTrace()
                                Log.e("DataHandler", "location error: ${error.message}")
                            }
                    )
        }
    }

    fun startLocationService(context: Context) {
        Log.i("DataHandler", "sessionIsAuthorized ${authHelper.sessionIsAuthorized()}")
        Log.i("DataHandler", "getLocationAllowed ${authHelper.getLocationAllowed()}")

        if (authHelper.sessionIsAuthorized() && authHelper.getLocationAllowed()) {
            val lm = context.getLocationManager()
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 60 * 10, 2000f, locationListener)
            Log.i("DataHandler", "started location listener")

        }
    }
}
