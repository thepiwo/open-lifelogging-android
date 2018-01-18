package de.thepiwo.lifelogging.android.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.gson.GsonBuilder
import com.mcxiaoke.koi.ext.getNotificationManager
import com.mcxiaoke.koi.ext.newNotification
import de.thepiwo.lifelogging.android.LocationRequestService
import de.thepiwo.lifelogging.android.R
import de.thepiwo.lifelogging.android.api.LoggingApiService
import de.thepiwo.lifelogging.android.api.models.LogEntryInsert
import de.thepiwo.lifelogging.android.api.models.logentities.WifiEntity
import de.thepiwo.lifelogging.android.dagger.ForApplication
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
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

    fun createLogItem(logEntryInsert: LogEntryInsert, errorFileLog: File? = null) {
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
                            if (errorFileLog != null) {
                                FileOutputStream(errorFileLog, true).use { stream ->
                                    val json = GsonBuilder().create().toJson(logEntryInsert)
                                    stream.write(json.toByteArray(Charsets.UTF_8))
                                    Log.i("DataHandler", "write $json in file $errorFileLog")
                                }
                            }
                        }
                )
    }

    //too hacky...
    fun getGoogleApiClient(context: Context): GoogleApiClient? {
        val gApi = GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .build()
        gApi.connect()
        Log.i("DataHandler", "gApi.isConnected: ${gApi.isConnected}")
        while (!gApi.isConnected) {
            Log.i("DataHandler", "gApi.isConnected: ${gApi.isConnected}")
            Thread.sleep(100)
        }
        return gApi
    }

    fun checkLocationServiceRunning(context: Context) {
        val serviceIntent = Intent(context, LocationRequestService::class.java)
        val pIntent = PendingIntent.getService(context, 0, serviceIntent, 0)
        pIntent.send()
    }

    fun startLocationServiceObserver(context: Context): Boolean {
        Log.i("DataHandler", "sessionIsAuthorized ${authHelper.sessionIsAuthorized()}")
        Log.i("DataHandler", "getLocationAllowed ${authHelper.getLocationAllowed()}")

        if (authHelper.sessionIsAuthorized() && authHelper.getLocationAllowed()) {

            val serviceIntent = Intent(context, LocationRequestService::class.java)

            val pIntent = PendingIntent.getService(context, 0, serviceIntent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), TimeUnit.MINUTES.toMillis(60), pIntent)
            context.startService(serviceIntent)

            Log.i("DataHandler", "started location request service")

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

    fun showBasicNotification(context: Context, text: String) {
        Log.i("DataHandler", "showBasicNotification: $text")

        val n = context.newNotification {
            this.setContentTitle("Lifelogging")
                    .setContentText(text)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true).build()
        }

        context.getNotificationManager().notify(0, n)
    }
}
