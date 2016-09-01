package de.thepiwo.lifelogging.android

import de.thepiwo.lifelogging.android.dagger.components.ApplicationComponent

class Application : BaseApplication() {

    lateinit var component: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        this.component = initDaggerComponent()
    }

}