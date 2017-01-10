package de.thepiwo.lifelogging.android.util

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.os.Bundle
import android.util.Log
import com.mcxiaoke.koi.ext.getLocationManager
import de.thepiwo.lifelogging.android.api.LoggingApiService
import de.thepiwo.lifelogging.android.api.models.logentities.CoordEntity
import de.thepiwo.lifelogging.android.api.models.LogEntryInsert
import de.thepiwo.lifelogging.android.api.models.logentities.WifiEntity
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

    fun createLogItem(logEntryInsert: LogEntryInsert) {
        loggingApiService.createLogItem(logEntryInsert)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            Log.i("DataHandler", "logItem sent $logEntryInsert")
                        },
                        { error ->
                            error.printStackTrace()
                            Log.e("DataHandler", "logItem error: ${error.message}")
                        }
                )
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
            val logCoordEntity = CoordEntity(null, null, location.latitude, location.longitude, location.altitude, location.accuracy)
            createLogItem(LogEntryInsert(logCoordEntity))
        }
    }

    fun startLocationService(context: Context): Boolean {
        Log.i("DataHandler", "sessionIsAuthorized ${authHelper.sessionIsAuthorized()}")
        Log.i("DataHandler", "getLocationAllowed ${authHelper.getLocationAllowed()}")

        if (authHelper.sessionIsAuthorized() && authHelper.getLocationAllowed()) {
            val lm = context.getLocationManager()
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 60 * 10, 0f, locationListener)
            Log.i("DataHandler", "started location listener")
            return true
        } else {
            return false
        }
    }

    fun handleWifiInfo(connectionInfo: WifiInfo) {

        if (connectionInfo.supplicantState == SupplicantState.COMPLETED || connectionInfo.supplicantState == SupplicantState.DISCONNECTED) {

            Log.i("DataHandler", "handleWifiInfo: $connectionInfo")
            val logWifiEntity = WifiEntity(null, null, connectionInfo.ssid, connectionInfo.linkSpeed, connectionInfo.supplicantState.name)
            createLogItem(LogEntryInsert(logWifiEntity))
        }
    }
}
