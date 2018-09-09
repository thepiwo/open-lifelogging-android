package de.thepiwo.lifelogging.android.activities

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v4.app.ActivityCompat.shouldShowRequestPermissionRationale
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.widget.ListView
import com.mcxiaoke.koi.ext.newIntent
import com.mcxiaoke.koi.ext.onClick
import de.thepiwo.lifelogging.android.activities.adapters.LogListAdapter
import de.thepiwo.lifelogging.android.api.LoggingApiService
import de.thepiwo.lifelogging.android.dagger.components.ApplicationComponent
import de.thepiwo.lifelogging.android.util.AuthHelper
import de.thepiwo.lifelogging.android.util.DataHandler
import org.jetbrains.anko.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var authHelper: AuthHelper

    @Inject
    lateinit var dataHandler: DataHandler

    @Inject
    lateinit var loggingApiService: LoggingApiService

    lateinit var logList: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {
            padding = dip(30)

            textView("Logging started") {
                textSize = 20f
            }

            button("logout") {
                textSize = 16f
                onClick { logout() }
            }.lparams(width = matchParent) {
                topMargin = dip(20)
            }

            logList = listView()

        }

        checkPermissions()
    }

    override fun onResume() {
        super.onResume()

        loggingApiService.getLogs(100)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            logList.adapter = LogListAdapter(it)
                        },
                        { error ->
                            toast(error.message ?: "fetch error")
                        }
                )
    }

    private fun logout() {
        authHelper.logout()
        navigator.navigateToLoginActivity(this)
        finish()
    }

    override fun injectComponent(component: ApplicationComponent) {
        component.inject(this)
    }


    private fun checkPermissions() {
        checkLocationPermission()
        checkStoragePermission()

        startLocationLogging()
    }

    private fun checkLocationPermission() {
        if (checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), MY_PERMISSIONS_ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun checkStoragePermission() {
        if (checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
                requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun startLocationLogging() {
        if (hasPermission(ACCESS_FINE_LOCATION)) {
            authHelper.setLocationAllowed(true)
            val startedLocationService = dataHandler.startLocationServiceObserver(this)
            if (startedLocationService) toast("Location logging started")
            else toast("Error starting location logging (${authHelper.sessionIsAuthorized()} ${authHelper.getLocationAllowed()})")
        } else {
            toast("Location logging permission not granted")
        }
    }

    private fun hasPermission(perm: String): Boolean {
        return PERMISSION_GRANTED == checkSelfPermission(perm)
    }

    companion object {
        fun getCallingIntent(context: Context): Intent = context.newIntent<MainActivity>()
        const val MY_PERMISSIONS_ACCESS_FINE_LOCATION: Int = 1337
        const val MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: Int = 1338
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_ACCESS_FINE_LOCATION -> {
                checkStoragePermission()
                startLocationLogging()
            }
        }
    }
}
