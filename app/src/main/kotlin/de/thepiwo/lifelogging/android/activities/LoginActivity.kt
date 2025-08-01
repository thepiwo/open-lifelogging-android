package de.thepiwo.lifelogging.android.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afollestad.materialdialogs.MaterialDialog
import com.mcxiaoke.koi.ext.newIntent
import dagger.hilt.android.AndroidEntryPoint
import de.thepiwo.lifelogging.android.ui.theme.LifeloggingTheme
import de.thepiwo.lifelogging.android.util.AuthHelper
import de.thepiwo.lifelogging.android.util.DataHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@AndroidEntryPoint
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

        setContent {
            LifeloggingTheme {
                LoginScreen(
                    initialApiUrl = authHelper.getApiUrl(),
                    initialUsername = authHelper.getLoginData()?.login ?: "",
                    initialPassword = authHelper.getLoginData()?.password ?: "",
                    onLoginClick = { apiUrl, username, password ->
                        login(apiUrl, username, password)
                    }
                )
            }
        }
    }

    @Composable
    fun LoginScreen(
        initialApiUrl: String,
        initialUsername: String,
        initialPassword: String,
        onLoginClick: (String, String, String) -> Unit
    ) {
        var apiUrl by remember { mutableStateOf(initialApiUrl) }
        var username by remember { mutableStateOf(initialUsername) }
        var password by remember { mutableStateOf(initialPassword) }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp)
        ) {
            Text(
                text = "endpoint url (end with /)",
                fontSize = 16.sp
            )
            TextField(
                value = apiUrl,
                onValueChange = { apiUrl = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "username",
                fontSize = 16.sp
            )
            TextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "password",
                fontSize = 16.sp
            )
            TextField(
                value = password,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = { onLoginClick(apiUrl, username, password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("login", fontSize = 16.sp)
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
                                Toast.makeText(this, error.message ?: "login error", Toast.LENGTH_SHORT).show()
                            }
                    )
        }
    }


    companion object {
        fun getCallingIntent(context: Context): Intent = context.newIntent<LoginActivity>()
    }


}
