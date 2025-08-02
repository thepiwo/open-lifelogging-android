package de.thepiwo.lifelogging.android.util

import android.annotation.SuppressLint
import android.content.Context
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
        Log.i("DataHandler", "🚀 === STARTING LOCATION DATA TRANSMISSION ===")
        Log.i("DataHandler", "📍 Location data to send: $logEntryInsert")
        Log.i("DataHandler", "🔐 Auth status: ${authHelper.sessionIsAuthorized()}")
        Log.i("DataHandler", "🎫 Token available: ${authHelper.getToken() != null}")
        
        if (authHelper.getToken() != null) {
            Log.i("DataHandler", "🎫 Token preview: ${authHelper.getToken()?.token?.take(10)}...")
        }
        
        
        Log.i("DataHandler", "🌐 API URL: ${authHelper.getApiUrl()}")
        Log.i("DataHandler", "📡 Starting API call to create log item...")
        
        loggingApiService.createLogItem(logEntryInsert)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { 
                    Log.i("DataHandler", "🔄 API call subscribed - request in progress...")
                }
                .subscribe(
                        { response ->
                            Log.i("DataHandler", "✅ === LOCATION DATA SENT SUCCESSFULLY ===")
                            Log.i("DataHandler", "📊 API Response: $response")
                            Log.i("DataHandler", "📍 Original data: $logEntryInsert")
                            Log.i("DataHandler", "🎉 Location should now appear in the app's log list!")
                        },
                        { error ->
                            Log.e("DataHandler", "❌ === LOCATION DATA TRANSMISSION FAILED ===")
                            Log.e("DataHandler", "💥 Error message: ${error.message}")
                            Log.e("DataHandler", "🔍 Error type: ${error.javaClass.simpleName}")
                            Log.e("DataHandler", "📍 Failed data: $logEntryInsert")
                            
                            // Detailed error analysis
                            when (error) {
                                is retrofit2.HttpException -> {
                                    Log.e("DataHandler", "🌐 HTTP Error - Code: ${error.code()}, Message: ${error.message()}")
                                    when (error.code()) {
                                        401 -> Log.e("DataHandler", "🔐 UNAUTHORIZED - Token may be expired or invalid")
                                        403 -> Log.e("DataHandler", "🚫 FORBIDDEN - Access denied")
                                        404 -> Log.e("DataHandler", "🔍 NOT FOUND - API endpoint may be incorrect")
                                        500 -> Log.e("DataHandler", "🔥 SERVER ERROR - Backend issue")
                                        else -> Log.e("DataHandler", "🌐 HTTP ${error.code()} - Check API documentation")
                                    }
                                }
                                is java.net.UnknownHostException -> {
                                    Log.e("DataHandler", "🌐 NETWORK ERROR - Cannot resolve host, check internet connection")
                                }
                                is java.net.SocketTimeoutException -> {
                                    Log.e("DataHandler", "⏰ TIMEOUT ERROR - Request took too long")
                                }
                                is java.net.ConnectException -> {
                                    Log.e("DataHandler", "🔌 CONNECTION ERROR - Cannot connect to server")
                                }
                                else -> {
                                    Log.e("DataHandler", "❓ UNKNOWN ERROR - ${error.javaClass.simpleName}")
                                    error.printStackTrace()
                                }
                            }
                            
                            if (errorFileLog != null) {
                                try {
                                    FileOutputStream(errorFileLog, true).use { stream ->
                                        val json = GsonBuilder().create().toJson(logEntryInsert)
                                        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                                        stream.write("[$timestamp] ${error.javaClass.simpleName}: ${error.message}\n$json\n\n".toByteArray(Charsets.UTF_8))
                                        Log.i("DataHandler", "📝 Saved failed location data to file: $errorFileLog")
                                    }
                                } catch (fileError: Exception) {
                                    Log.e("DataHandler", "💾 Failed to write to error file: ${fileError.message}")
                                }
                            }
                        }
                )
    }

    fun checkLocationServiceRunning(context: Context) {
        Log.i("DataHandler", "Checking and starting location service if needed")
        if (authHelper.sessionIsAuthorized() && authHelper.getLocationAllowed()) {
            LocationRequestService.startService(context)
        } else {
            Log.w("DataHandler", "Not starting location service - not authorized or location not allowed")
        }
    }

    fun startLocationServiceObserver(context: Context): Boolean {
        Log.i("DataHandler", "sessionIsAuthorized ${authHelper.sessionIsAuthorized()}")
        Log.i("DataHandler", "getLocationAllowed ${authHelper.getLocationAllowed()}")

        return if (authHelper.sessionIsAuthorized() && authHelper.getLocationAllowed()) {
            LocationRequestService.startService(context)
            Log.i("DataHandler", "started location request foreground service")
            true
        } else {
            Log.w("DataHandler", "Cannot start location service - not authorized or location not allowed")
            false
        }
    }

    fun stopLocationServiceObserver(context: Context) {
        LocationRequestService.stopService(context)
        Log.i("DataHandler", "stopped location request service")
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
