package de.thepiwo.lifelogging.android.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.gson.GsonBuilder
import com.mcxiaoke.koi.ext.getNotificationManager
import com.mcxiaoke.koi.ext.newNotification
import de.thepiwo.lifelogging.android.LocationRequestService
import de.thepiwo.lifelogging.android.R
import de.thepiwo.lifelogging.android.api.LoggingApiService
import de.thepiwo.lifelogging.android.api.models.LogEntryInsert
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DataHandler
@Inject
constructor(private val loggingApiService: LoggingApiService,
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

    @SuppressLint("CheckResult")
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

    fun checkLocationServiceRunning(context: Context) {
        val serviceIntent = Intent(context, LocationRequestService::class.java)
        val pIntent = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_IMMUTABLE)
        pIntent.send()
    }

    fun startLocationServiceObserver(context: Context): Boolean {
        Log.i("DataHandler", "sessionIsAuthorized ${authHelper.sessionIsAuthorized()}")
        Log.i("DataHandler", "getLocationAllowed ${authHelper.getLocationAllowed()}")

        return if (authHelper.sessionIsAuthorized() && authHelper.getLocationAllowed()) {

            val serviceIntent = Intent(context, LocationRequestService::class.java)

            val pIntent = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_IMMUTABLE)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), TimeUnit.MINUTES.toMillis(60), pIntent)
            context.startService(serviceIntent)

            Log.i("DataHandler", "started location request service")

            true
        } else {
            false
        }
    }

    fun copyFile(context: Context, uri: Uri): File {
        val destination = File(context.getExternalFilesDir(null), File(uri.path!!).name)

        context.contentResolver.openInputStream(uri).use { input ->
            val outputStream = FileOutputStream(destination)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                while (true) {
                    val byteCount = input!!.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }

        return destination
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
