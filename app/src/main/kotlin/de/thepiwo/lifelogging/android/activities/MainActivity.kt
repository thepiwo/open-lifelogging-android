package de.thepiwo.lifelogging.android.activities

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.widget.ListView
import com.mcxiaoke.koi.ext.newIntent
import com.mcxiaoke.koi.ext.onClick
import de.thepiwo.lifelogging.android.activities.adapters.LogListAdapter
import de.thepiwo.lifelogging.android.api.LoggingApiService
import de.thepiwo.lifelogging.android.dagger.components.ApplicationComponent
import de.thepiwo.lifelogging.android.util.AuthHelper
import de.thepiwo.lifelogging.android.util.DataHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.*
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

            button("upload samsung health zip") {
                textSize = 16f
                onClick { uploadSamsungHealth() }
            }.lparams(width = matchParent) {
                topMargin = dip(20)
            }

            logList = listView()

        }

        checkPermissions()
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
                    logList.adapter = LogListAdapter(it)
                },
                { error ->
                    toast(error.message ?: "fetch error")
                }
            )

    private fun logout() {
        authHelper.logout()
        navigator.navigateToLoginActivity(this)
        finish()
    }

    private fun uploadSamsungHealth() {
        fun openFile() {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/zip"
            }

            startActivityForResult(intent, MY_INTENT_PICK_FILE)
        }

        openFile()
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
        if (!hasPermission(ACCESS_FINE_LOCATION)) {
            if (!shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                requestPermissions(arrayOf(ACCESS_FINE_LOCATION), MY_PERMISSIONS_ACCESS_FINE_LOCATION)
            }
        }

        if (!hasPermission(ACCESS_BACKGROUND_LOCATION)) {
            if (!shouldShowRequestPermissionRationale(ACCESS_BACKGROUND_LOCATION)) {
                requestPermissions(arrayOf(ACCESS_BACKGROUND_LOCATION), MY_PERMISSIONS_ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    private fun checkStoragePermission() {
        if (!hasPermission(WRITE_EXTERNAL_STORAGE)) {
            if (!shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE)
            }
        }

        if (!hasPermission(READ_EXTERNAL_STORAGE)) {
            if (!shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), MY_PERMISSIONS_READ_EXTERNAL_STORAGE)
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
        const val MY_PERMISSIONS_READ_EXTERNAL_STORAGE: Int = 1339
        const val MY_PERMISSIONS_ACCESS_BACKGROUND_LOCATION: Int = 1341

        const val MY_INTENT_PICK_FILE: Int = 1340
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        when (requestCode) {
            MY_INTENT_PICK_FILE -> {
                resultData?.data?.also { uri ->
                    val file = dataHandler.copyFile(this, uri)

                    loggingApiService.importSamsung(file)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            {
                                toast("imported $it")
                                file.delete()
                                getLogs()
                            },
                            { error ->
                                toast(error.message ?: "fetch error")
                            }
                        )
                }
            }
        }
    }

}
