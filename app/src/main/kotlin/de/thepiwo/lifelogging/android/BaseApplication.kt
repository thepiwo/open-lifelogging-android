package de.thepiwo.lifelogging.android

import androidx.multidex.MultiDexApplication
import de.thepiwo.lifelogging.android.dagger.components.ApplicationComponent
import de.thepiwo.lifelogging.android.dagger.components.DaggerApplicationComponent
import de.thepiwo.lifelogging.android.dagger.modules.ApplicationModule

abstract class BaseApplication : MultiDexApplication() {

    protected fun initDaggerComponent(): ApplicationComponent {
        return DaggerApplicationComponent.builder().applicationModule(ApplicationModule(this)).build()
    }

}