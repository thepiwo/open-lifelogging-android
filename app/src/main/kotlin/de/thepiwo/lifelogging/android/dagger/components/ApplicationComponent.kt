package de.thepiwo.lifelogging.android.dagger.components

import dagger.Component
import de.thepiwo.lifelogging.android.LocationRequestService
import de.thepiwo.lifelogging.android.activities.BaseActivity
import de.thepiwo.lifelogging.android.activities.LoginActivity
import de.thepiwo.lifelogging.android.activities.MainActivity
import de.thepiwo.lifelogging.android.dagger.ForApplication
import de.thepiwo.lifelogging.android.dagger.modules.ApplicationModule
import de.thepiwo.lifelogging.android.util.BootCompletedReceiver
import de.thepiwo.lifelogging.android.util.LocationChangedReceiver
import de.thepiwo.lifelogging.android.util.WakeLockReceiver

@ForApplication
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    fun inject(baseActivity: BaseActivity)

    fun inject(loginActivity: LoginActivity)

    fun inject(mainActivity: MainActivity)

    fun inject(bootCompletedReceiver: BootCompletedReceiver)

    fun inject(locationRequestService: LocationRequestService)

    fun inject(wakeLockReceiver: WakeLockReceiver)

    fun inject(locationChangedReceiver: LocationChangedReceiver)

}
