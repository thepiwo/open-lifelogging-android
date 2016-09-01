package de.thepiwo.lifelogging.android

import android.app.Application
import de.thepiwo.lifelogging.android.dagger.components.ApplicationComponent
import de.thepiwo.lifelogging.android.dagger.components.DaggerApplicationComponent
import de.thepiwo.lifelogging.android.dagger.modules.ApplicationModule

abstract class BaseApplication : Application() {

    protected fun initDaggerComponent(): ApplicationComponent {
        return DaggerApplicationComponent.builder().applicationModule(ApplicationModule(this)).build()
    }

}