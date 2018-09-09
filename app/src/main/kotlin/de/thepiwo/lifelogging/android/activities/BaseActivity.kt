package de.thepiwo.lifelogging.android.activities

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import de.thepiwo.lifelogging.android.Application
import de.thepiwo.lifelogging.android.dagger.components.ApplicationComponent
import de.thepiwo.lifelogging.android.util.Navigator
import javax.inject.Inject

open class BaseActivity : AppCompatActivity() {

    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // disable landscape mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        this.applicationComponent.inject(this)
        this.injectComponent(this.applicationComponent)
    }

    private val applicationComponent: ApplicationComponent
        get() = (application as Application).component


    open fun injectComponent(component: ApplicationComponent) {}
}
