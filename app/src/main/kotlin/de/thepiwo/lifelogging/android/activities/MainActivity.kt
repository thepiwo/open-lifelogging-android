package de.thepiwo.lifelogging.android.activities

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import com.mcxiaoke.koi.ext.newIntent
import dagger.hilt.android.AndroidEntryPoint
import de.thepiwo.lifelogging.android.api.LoggingApiService
import de.thepiwo.lifelogging.android.api.models.LogList
import de.thepiwo.lifelogging.android.ui.theme.LifeloggingTheme
import de.thepiwo.lifelogging.android.util.AuthHelper
import de.thepiwo.lifelogging.android.util.DataHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    @Inject
    lateinit var authHelper: AuthHelper

    @Inject
    lateinit var dataHandler: DataHandler

    @Inject
    lateinit var loggingApiService: LoggingApiService


    private val logsLiveData = MutableLiveData<LogList>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LifeloggingTheme {
                MainScreen(
                    logs = logsLiveData.observeAsState().value,
                    onLogoutClick = { logout() },
                    onUploadClick = { uploadData() },
                    onMapClick = {
                        val intent = Intent(this@MainActivity, MapActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }

        checkPermissions()
    }

    @Composable
    fun MainScreen(
        logs: LogList?,
        onLogoutClick: () -> Unit,
        onUploadClick: () -> Unit,
        onMapClick: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(30.dp)
        ) {
            Text(
                text = "Logging started",
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Logout",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Button(
                    onClick = onUploadClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Upload",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Button(
                    onClick = onMapClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Map",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            logs?.let { logList ->
                LazyColumn {
                    items(logList.logs) { logItem ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            val itemText = if (logItem.key == "CoordEntity")
                                "${logItem.data.latitude}, ${logItem.data.longitude}"
                            else
                                logItem.key

                            Text(
                                text = "${logItem.createdAtClientString()}: $itemText",
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun onResume() {
        super.onResume()
        getLogs()
    }

    private fun getLogs() =
        loggingApiService.getLogs(100)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    logsLiveData.value = it
                },
                { error ->
                    Toast.makeText(this, error.message ?: "fetch error", Toast.LENGTH_SHORT).show()
                }
            )

    private fun logout() {
        authHelper.logout()
        navigator.navigateToLoginActivity(this)
        finish()
    }

    private fun uploadData() {
        val options = arrayOf("Google Timeline (JSON)", "Samsung Health (ZIP)")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Choose data source to upload")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openFileForGoogle()
                1 -> openFileForSamsung()
            }
        }
        builder.show()
    }


    private fun openFileForGoogle() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        startActivityForResult(intent, MY_INTENT_PICK_GOOGLE_FILE)
    }

    private fun openFileForSamsung() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
        }
        startActivityForResult(intent, MY_INTENT_PICK_SAMSUNG_FILE)
    }


    private fun checkPermissions() {
        checkLocationPermission()
        checkStoragePermission()

        startLocationLogging()
    }

    private fun checkLocationPermission() {
        if (!hasPermission(ACCESS_FINE_LOCATION)) {
            if (!shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                requestPermissions(
                    arrayOf(ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION
                )
            }
        }

        if (!hasPermission(ACCESS_BACKGROUND_LOCATION)) {
            if (!shouldShowRequestPermissionRationale(ACCESS_BACKGROUND_LOCATION)) {
                requestPermissions(
                    arrayOf(ACCESS_BACKGROUND_LOCATION),
                    MY_PERMISSIONS_ACCESS_BACKGROUND_LOCATION
                )
            }
        }
    }

    private fun checkStoragePermission() {
        if (!hasPermission(WRITE_EXTERNAL_STORAGE)) {
            if (!shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                requestPermissions(
                    arrayOf(WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE
                )
            }
        }

        if (!hasPermission(READ_EXTERNAL_STORAGE)) {
            if (!shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                requestPermissions(
                    arrayOf(READ_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun startLocationLogging() {
        val hasFineLocation = hasPermission(ACCESS_FINE_LOCATION)
        val hasBackgroundLocation = hasPermission(ACCESS_BACKGROUND_LOCATION)

        Log.i(
            "MainActivity",
            "Location permissions - Fine: $hasFineLocation, Background: $hasBackgroundLocation"
        )

        if (hasFineLocation && hasBackgroundLocation) {
            authHelper.setLocationAllowed(true)
            val startedLocationService = dataHandler.startLocationServiceObserver(this)
            if (startedLocationService) {
                Toast.makeText(this, "Location logging started", Toast.LENGTH_SHORT).show()
                Log.i("MainActivity", "Location service started successfully")
            } else {
                Toast.makeText(
                    this,
                    "Error starting location logging (Auth: ${authHelper.sessionIsAuthorized()}, Location: ${authHelper.getLocationAllowed()})",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(
                    "MainActivity",
                    "Failed to start location service - Auth: ${authHelper.sessionIsAuthorized()}, Location: ${authHelper.getLocationAllowed()}"
                )
            }
        } else {
            authHelper.setLocationAllowed(false)
            val missingPermissions = mutableListOf<String>()
            if (!hasFineLocation) missingPermissions.add("Fine Location")
            if (!hasBackgroundLocation) missingPermissions.add("Background Location")

            val message = "Missing permissions: ${missingPermissions.joinToString(", ")}"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            Log.w("MainActivity", message)
        }
    }

    private fun hasPermission(perm: String): Boolean {
        return PERMISSION_GRANTED == checkSelfPermission(perm)
    }

    companion object {
        fun getCallingIntent(context: Context): Intent = context.newIntent<MainActivity>()
        const val MY_PERMISSIONS_ACCESS_FINE_LOCATION: Int = 1337
        const val MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: Int = 1338
        const val MY_PERMISSIONS_READ_EXTERNAL_STORAGE: Int = 1339
        const val MY_PERMISSIONS_ACCESS_BACKGROUND_LOCATION: Int = 1341

        const val MY_INTENT_PICK_FILE: Int = 1340
        const val MY_INTENT_PICK_GOOGLE_FILE: Int = 1342
        const val MY_INTENT_PICK_SAMSUNG_FILE: Int = 1343
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_ACCESS_FINE_LOCATION -> {
                Log.i(
                    "MainActivity",
                    "Fine location permission result: ${grantResults.firstOrNull() == PERMISSION_GRANTED}"
                )
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    // Fine location granted, now check for background location
                    checkLocationPermission()
                }
                checkStoragePermission()
                startLocationLogging()
            }

            MY_PERMISSIONS_ACCESS_BACKGROUND_LOCATION -> {
                Log.i(
                    "MainActivity",
                    "Background location permission result: ${grantResults.firstOrNull() == PERMISSION_GRANTED}"
                )
                checkStoragePermission()
                startLocationLogging()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        when (requestCode) {
            MY_INTENT_PICK_GOOGLE_FILE -> {
                resultData?.data?.also { uri ->
                    val file = dataHandler.copyFile(this, uri)

                    loggingApiService.importGoogle(file)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            {
                                Toast.makeText(
                                    this,
                                    "imported $it Google Timeline entries",
                                    Toast.LENGTH_SHORT
                                ).show()
                                file.delete()
                                getLogs()
                            },
                            { error ->
                                Toast.makeText(
                                    this,
                                    error.message ?: "Google import error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                }
            }

            MY_INTENT_PICK_SAMSUNG_FILE -> {
                resultData?.data?.also { uri ->
                    val file = dataHandler.copyFile(this, uri)

                    loggingApiService.importSamsung(file)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            {
                                Toast.makeText(
                                    this,
                                    "imported $it Samsung Health entries",
                                    Toast.LENGTH_SHORT
                                ).show()
                                file.delete()
                                getLogs()
                            },
                            { error ->
                                Toast.makeText(
                                    this,
                                    error.message ?: "Samsung import error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                }
            }
        }
    }

}
