package de.thepiwo.lifelogging.android.util

import android.content.Context
import android.net.ConnectivityManager
import de.thepiwo.lifelogging.android.dagger.ForApplication
import javax.inject.Inject

@ForApplication
class ConnectivityHelper
@Inject
constructor(context: Context) {
    private val connectivityManager: ConnectivityManager

    init {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun connected(): Boolean {
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
