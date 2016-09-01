package de.thepiwo.lifelogging.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.mcxiaoke.koi.ext.newIntent
import de.thepiwo.lifelogging.android.dagger.components.ApplicationComponent
import de.thepiwo.lifelogging.android.util.AuthHelper
import de.thepiwo.lifelogging.android.util.Constants
import de.thepiwo.lifelogging.android.util.DataHandler
import org.jetbrains.anko.*
import javax.inject.Inject

class LoginActivity : BaseActivity() {

    @Inject
    lateinit var authHelper: AuthHelper

    @Inject
    lateinit var dataHandler: DataHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (authHelper.sessionIsAvailable()) {
            navigator.navigateToMainActivity(this)
            finish()
        }

        verticalLayout {
            padding = dip(30)
            val apiUrl = editText(Constants.API_DEFAULT_URL) {
                hint = "api url"
                textSize = 16f
            }
            val username = editText {
                hint = "username"
                textSize = 16f
            }
            val password = editText {
                hint = "password"
                textSize = 16f
            }
            button("login") {
                textSize = 16f
                onClick {
                    login(
                            apiUrl.text.toString(),
                            username.text.toString(),
                            password.text.toString()
                    )
                }
            }
        }

    }

    private fun login(apiUrl: String, username: String, password: String) {
        Log.i("LoginActivity", "login button pressed")
        authHelper.setApiUrl(apiUrl)
        authHelper.setUsername(username)
        authHelper.setPassword(password)
        navigator.navigateToMainActivity(this)
        finish()
    }

    companion object {
        fun getCallingIntent(context: Context): Intent = context.newIntent<LoginActivity>()
    }

    override fun injectComponent(component: ApplicationComponent) {
        component.inject(this)
    }

}
