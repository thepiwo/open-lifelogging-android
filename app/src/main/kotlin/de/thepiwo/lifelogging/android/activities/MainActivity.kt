package de.thepiwo.lifelogging.android.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.mcxiaoke.koi.ext.newIntent
import de.thepiwo.lifelogging.android.dagger.components.ApplicationComponent
import de.thepiwo.lifelogging.android.util.AuthHelper
import de.thepiwo.lifelogging.android.util.DataHandler
import javax.inject.Inject
import org.jetbrains.anko.*

class MainActivity : BaseActivity() {

    @Inject
    lateinit var authHelper: AuthHelper

    @Inject
    lateinit var dataHandler: DataHandler

    private val MY_PERMISSIONS_ACCESS_FINE_LOCATIONS: Int = 1337

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_ACCESS_FINE_LOCATIONS
                )
            }
        }

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
        }

        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            authHelper.setLocationAllowed(true)
            val startedLocationService = dataHandler.startLocationService(this)
            if (startedLocationService) toast("Location logging started")
            else toast("Error starting location logging (${authHelper.sessionIsAuthorized()} ${authHelper.getLocationAllowed()})")
        } else {
            toast("Location logging permission not granted")
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_ACCESS_FINE_LOCATIONS -> {
                authHelper.setLocationAllowed(true)
                dataHandler.startLocationService(this)
            }
        }
    }

    private fun logout() {
        authHelper.logout()
        navigator.navigateToLoginActivity(this)
        finish()
    }

    companion object {
        fun getCallingIntent(context: Context): Intent = context.newIntent<MainActivity>()
    }

    override fun injectComponent(component: ApplicationComponent) {
        component.inject(this)
    }

    private fun hasPermission(perm: String): Boolean {
        return PackageManager.PERMISSION_GRANTED === checkSelfPermission(perm)
    }
}
