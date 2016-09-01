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
import de.thepiwo.lifelogging.android.dagger.ForApplication
import javax.inject.Inject

@ForApplication
class DataHandler
@Inject
constructor(val gson: Gson,
            val balanceApiService: LoggingApiService,
            val authHelper: AuthHelper) {


    private val locationListener = object : LocationListener {
        override fun onProviderDisabled(p0: String?) {
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        }

        override fun onProviderEnabled(p0: String?) {
        }

        override fun onLocationChanged(location: Location) {
            Log.i("locationListener", "location: $location")
        }
    }

    fun startLocationService(context: Context) {
        if (authHelper.sessionIsAvailable() && authHelper.getLocationAllowed()) {
            /*        val serviceIntent = Intent(context, LocationService::class.java)

                    val cal = Calendar.getInstance()
                    val pIntent = PendingIntent.getService(context, 0, serviceIntent, 0)
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val updateTime = (1 * 60 * 1000).toLong()
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, updateTime, pIntent)
                    context.startService(serviceIntent)

                    Log.i("DataHandler", "started location service")
                }*/

            val lm = context.getLocationManager()
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 5, 5f, locationListener)
            Log.i("DataHandler", "started location listener")

        }
    }
}
