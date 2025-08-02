package de.thepiwo.lifelogging.android

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import de.thepiwo.lifelogging.android.activities.MainActivity
import de.thepiwo.lifelogging.android.api.models.LogEntryInsert
import de.thepiwo.lifelogging.android.api.models.logentities.CoordEntity
import de.thepiwo.lifelogging.android.util.AuthHelper
import de.thepiwo.lifelogging.android.util.DataHandler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class LocationRequestService : Service() {

    @Inject
    lateinit var dataHandler: DataHandler

    @Inject
    lateinit var authHelper: AuthHelper

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isLocationTrackingActive = false
    private var locationCallback: LocationCallback? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val CHANNEL_NAME = "Location Tracking"
        
        fun startService(context: Context) {
            val intent = Intent(context, LocationRequestService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, LocationRequestService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        Log.i("LocationRequestService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("LocationRequestService", "🚀 Service started - startId: $startId")
        Log.i("LocationRequestService", "Auth status: ${authHelper.sessionIsAuthorized()}")
        Log.i("LocationRequestService", "Location allowed: ${authHelper.getLocationAllowed()}")
        
        startForeground(NOTIFICATION_ID, createNotification())
        Log.i("LocationRequestService", "📱 Foreground service started with notification")
        
        if (authHelper.sessionIsAuthorized() && authHelper.getLocationAllowed()) {
            startLocationTracking()
        } else {
            Log.w("LocationRequestService", "❌ Cannot start location tracking - Auth: ${authHelper.sessionIsAuthorized()}, Location: ${authHelper.getLocationAllowed()}")
            stopSelf()
        }
        
        return START_STICKY // Restart service if killed by system
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopLocationTracking()
        Log.i("LocationRequestService", "Service destroyed")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent notification for background location tracking"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Life Logging")
            .setContentText("Tracking location in background")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun startLocationTracking() {
        if (isLocationTrackingActive) {
            Log.i("LocationRequestService", "Location tracking already active")
            return
        }

        Log.i("LocationRequestService", "🔄 Starting optimized location tracking with direct callback")
        
        // Check location settings and system state first
        checkLocationSettings()
        checkSystemOptimizations()

        // Set up periodic location updates with 30-second intervals
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, TimeUnit.MINUTES.toMillis(10))
            .setMinUpdateIntervalMillis(TimeUnit.MINUTES.toMillis(10))
            .build()

        Log.i("LocationRequestService", "📍 Location request configured: interval=30s, minInterval=15s, priority=HIGH_ACCURACY")

        try {
            // Use only the working direct LocationCallback approach
            startDirectLocationCallback(locationRequest)
            
            isLocationTrackingActive = true
            Log.i("LocationRequestService", "✅ Location tracking started successfully using direct callback")
            Log.i("LocationRequestService", "⏰ Expected: Location updates every 30 seconds")
            
        } catch (e: SecurityException) {
            Log.e("LocationRequestService", "❌ Location permission not granted", e)
            stopSelf()
        } catch (e: Exception) {
            Log.e("LocationRequestService", "❌ Unexpected error starting location tracking", e)
            stopSelf()
        }
    }


    private fun startDirectLocationCallback(locationRequest: LocationRequest) {
        Log.i("LocationRequestService", "🚀 Starting direct LocationCallback tracking...")
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                
                val locations = locationResult.locations
                Log.i("LocationRequestService", "📍 DIRECT CALLBACK: Received ${locations.size} location updates")
                
                locations.forEach { location ->
                    val timeString = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(location.time))
                    Log.i("LocationRequestService", "📍 DIRECT CALLBACK: Location lat=${location.latitude}, lng=${location.longitude}, accuracy=${location.accuracy}m, time=$timeString")
                    
                    // Process location directly (same as LocationChangedReceiver would do)
                    try {
                        val logCoordEntity = CoordEntity(
                            location.latitude, 
                            location.longitude, 
                            location.altitude, 
                            location.accuracy
                        )
                        val logEntryInsert = LogEntryInsert(logCoordEntity)
                        val errorFile = java.io.File(getExternalFilesDir(null), "location-error-log.json")
                        
                        Log.i("LocationRequestService", "🚀 DIRECT CALLBACK: Sending location to DataHandler")
                        dataHandler.createLogItem(logEntryInsert, errorFile)
                        Log.i("LocationRequestService", "✅ DIRECT CALLBACK: Location processed successfully")
                        
                    } catch (e: Exception) {
                        Log.e("LocationRequestService", "❌ DIRECT CALLBACK: Error processing location: ${e.message}", e)
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, mainLooper)
            Log.i("LocationRequestService", "✅ Direct LocationCallback registered")
        } catch (e: SecurityException) {
            Log.e("LocationRequestService", "❌ Direct Callback: Location permission not granted", e)
            throw e
        }
    }

    private fun checkSystemOptimizations() {
        try {
            // Check if the app is whitelisted from battery optimizations
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                val isIgnoringOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName)
                Log.i("LocationRequestService", "🔋 Battery optimization ignored: $isIgnoringOptimizations")
                
                if (!isIgnoringOptimizations) {
                    Log.w("LocationRequestService", "⚠️ WARNING: App is subject to battery optimization - this may prevent location updates")
                    Log.w("LocationRequestService", "💡 SOLUTION: Go to Settings > Battery > Battery Optimization > Select app > Don't optimize")
                }
            }
            
            // Check if device is in doze mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                val isDeviceIdle = powerManager.isDeviceIdleMode
                Log.i("LocationRequestService", "😴 Device in Doze mode: $isDeviceIdle")
                
                if (isDeviceIdle) {
                    Log.w("LocationRequestService", "⚠️ WARNING: Device is in Doze mode - location updates may be delayed")
                }
            }
            
        } catch (e: Exception) {
            Log.w("LocationRequestService", "Could not check system optimizations: ${e.message}")
        }
    }

    private fun checkLocationSettings() {
        try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
            val gpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
            val networkEnabled = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
            
            Log.i("LocationRequestService", "📡 Device location settings - GPS: $gpsEnabled, Network: $networkEnabled")
            
            if (!gpsEnabled && !networkEnabled) {
                Log.e("LocationRequestService", "❌ CRITICAL: No location providers enabled on device!")
            }
        } catch (e: Exception) {
            Log.w("LocationRequestService", "Could not check location settings: ${e.message}")
        }
    }

    private fun stopLocationTracking() {
        if (!isLocationTrackingActive) {
            return
        }

        Log.i("LocationRequestService", "🛑 Stopping location tracking")
        
        // Stop direct LocationCallback tracking
        try {
            locationCallback?.let { callback ->
                fusedLocationClient.removeLocationUpdates(callback)
                locationCallback = null
                Log.i("LocationRequestService", "✅ LocationCallback tracking stopped")
            }
        } catch (e: Exception) {
            Log.e("LocationRequestService", "❌ Error stopping LocationCallback tracking: ${e.message}")
        }
        
        isLocationTrackingActive = false
        Log.i("LocationRequestService", "🛑 Location tracking stopped")
    }
}