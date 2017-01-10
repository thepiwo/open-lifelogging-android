package de.thepiwo.lifelogging.android.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import de.thepiwo.lifelogging.android.activities.LoginActivity
import de.thepiwo.lifelogging.android.activities.MainActivity
import de.thepiwo.lifelogging.android.dagger.ForApplication

@ForApplication
class Navigator {
    fun navigateToLoginActivity(context: Context?) {
        if (context != null) {
            val intentToLaunch = LoginActivity.getCallingIntent(context)
            intentToLaunch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intentToLaunch)
        }
    }

    fun navigateToMainActivity(context: Context?) {
        if (context != null) {
            val intentToLaunch = MainActivity.getCallingIntent(context)
            intentToLaunch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intentToLaunch)
        }
    }

    fun restartApplicationToLogin(context: Context?) {
        if (context != null) {
            val startActivity = Intent(context, LoginActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, startActivity, PendingIntent.FLAG_CANCEL_CURRENT)
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
                    .set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)

            Runtime.getRuntime().exit(0)
        }
    }
}
