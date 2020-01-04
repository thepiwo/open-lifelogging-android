package de.thepiwo.lifelogging.android.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.afollestad.materialdialogs.MaterialDialog
import com.mcxiaoke.koi.ext.newIntent
import com.mcxiaoke.koi.ext.onClick
import de.thepiwo.lifelogging.android.dagger.components.ApplicationComponent
import de.thepiwo.lifelogging.android.util.AuthHelper
import de.thepiwo.lifelogging.android.util.DataHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.*
import javax.inject.Inject


class LoginActivity : BaseActivity() {

    @Inject
    lateinit var authHelper: AuthHelper

    @Inject
    lateinit var dataHandler: DataHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (authHelper.sessionIsAvailable() && authHelper.sessionIsAuthorized()) {
            navigator.navigateToMainActivity(this)
            finish()
        }

        verticalLayout {
            padding = dip(30)

            textView("endpoint url (end with /)") { textSize = 16f }
            val apiUrl = editText(authHelper.getApiUrl()) {
                textSize = 16f
            }

            textView("username") { textSize = 16f }
            val username = editText(authHelper.getLoginData()?.login) {
                textSize = 16f
            }

            textView("password") { textSize = 16f }
            val password = editText(authHelper.getLoginData()?.password) {
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
            }.lparams(width = matchParent) {
                topMargin = dip(20)
            }
        }

    }

    private fun showApiUrlChangedExit() {
        val dialog = MaterialDialog(this)
                .title(text = "ApiUrl changed")
                .message(text = "The api url differs from the one set, the system has to restart to apply the change.")
                .positiveButton(text = "Restart")
                .negativeButton(text = "Dismiss")
                .noAutoDismiss()
                .cancelable(false)

        dialog.positiveButton {
            navigator.restartApplicationToLogin(this)
        }

        dialog.negativeButton { materialDialog: MaterialDialog ->
            materialDialog.dismiss()
        }

        dialog.show()
    }

    @SuppressLint("CheckResult")
    private fun login(apiUrl: String, username: String, password: String) {
        Log.i("LoginActivity", "login button pressed")

        val oldApiUrl = authHelper.getApiUrl()
        authHelper.setApiUrl(apiUrl)
        authHelper.setUsername(username)
        authHelper.setPassword(password)

        if (apiUrl != oldApiUrl) showApiUrlChangedExit() else {

            dataHandler.login()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                Log.i("LoginActivity", "login successful")
                                navigator.navigateToMainActivity(this)
                                finish()
                            },
                            { error ->
                                toast(error.message ?: "login error")
                            }
                    )
        }
    }


    companion object {
        fun getCallingIntent(context: Context): Intent = context.newIntent<LoginActivity>()
    }

    override fun injectComponent(component: ApplicationComponent) {
        component.inject(this)
    }

}
