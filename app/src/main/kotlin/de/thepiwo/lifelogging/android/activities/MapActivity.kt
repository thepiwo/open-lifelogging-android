package de.thepiwo.lifelogging.android.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import dagger.hilt.android.AndroidEntryPoint
import de.thepiwo.lifelogging.android.api.LoggingApiService
import de.thepiwo.lifelogging.android.api.models.LogEntityReturn
import de.thepiwo.lifelogging.android.ui.theme.LifeloggingTheme
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@AndroidEntryPoint
class MapActivity : BaseActivity() {

    @Inject
    lateinit var loggingApiService: LoggingApiService

    @SuppressLint("CheckResult")
    private fun loadTodaysLocations(
        onSuccess: (List<LogEntityReturn>) -> Unit,
        onError: (String) -> Unit
    ) {
        // Get today's date in the format expected by the API (YYYY-MM-DD)
        val today = java.time.LocalDate.now().toString()

        loggingApiService.getLogsByDateRange(today, today)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { logList ->
                    // Filter for location entries only
                    val locationLogs = logList.logs.filter { it.key == "CoordEntity" }
                    onSuccess(locationLogs)
                },
                { error ->
                    onError("Error loading today's locations: ${error.message}")
                }
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LifeloggingTheme {
                MapScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }

    @Composable
    fun MapScreen(
        onBackClick: () -> Unit
    ) {
        var locations by remember { mutableStateOf<List<LogEntityReturn>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Load today's location data when the composable is first created
        LaunchedEffect(Unit) {
            loadTodaysLocations(
                onSuccess = { loadedLocations ->
                    locations = loadedLocations
                    isLoading = false
                },
                onError = { error ->
                    errorMessage = error
                    isLoading = false
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // Back button
            Button(
                onClick = onBackClick,
                modifier = Modifier.padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Back",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Content based on loading state
            when {
                isLoading -> {
                    // Loading state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Loading today's locations...",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }

                errorMessage != null -> {
                    // Error state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage!!,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                locations.isNotEmpty() -> {
                    // Success state with locations
                    val allPolylinePoints = locations.map { location ->
                        LatLng(location.data.latitude, location.data.longitude)
                    }

                    // Filter out consecutive duplicate points to prevent unwanted connections
                    val polylinePoints = allPolylinePoints.filterIndexed { index, point ->
                        index == 0 || point != allPolylinePoints[index - 1]
                    }

                    val cameraPositionState = rememberCameraPositionState()

                    // Auto-zoom to show all locations
                    LaunchedEffect(polylinePoints) {
                        if (polylinePoints.isNotEmpty()) {
                            val boundsBuilder = LatLngBounds.Builder()
                            polylinePoints.forEach { point ->
                                boundsBuilder.include(point)
                            }
                            val bounds = boundsBuilder.build()

                            // Add padding around the bounds (100 pixels)
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngBounds(
                                    bounds,
                                    100
                                ),
                                durationMs = 1000
                            )
                        }
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        // Create polyline connecting all location points (no markers)
                        if (polylinePoints.size > 1) {
                            Polyline(
                                points = polylinePoints,
                                color = Color.Blue,
                                width = 5f
                            )
                        }
                    }
                }

                else -> {
                    // No locations found
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No location data found for today",
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}