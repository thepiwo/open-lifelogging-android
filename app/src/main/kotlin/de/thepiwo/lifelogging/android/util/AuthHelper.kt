package de.thepiwo.lifelogging.android.util

import android.content.SharedPreferences
import com.mcxiaoke.koi.Const
import de.thepiwo.lifelogging.android.api.models.LoginPassword
import de.thepiwo.lifelogging.android.api.models.Token
import de.thepiwo.lifelogging.android.dagger.ForApplication
import javax.inject.Inject

@ForApplication
class AuthHelper
@Inject
constructor(private val sharedPreferences: SharedPreferences) {

    private var editor: SharedPreferences.Editor? = null

    private var locationAllowed: Boolean = false
    private var apiUrl: String = Constants.API_DEFAULT_URL
    private var password: String? = null
    private var username: String? = null
    private var token: Token? = null

    private fun getEditor(): SharedPreferences.Editor {
        if (editor == null) {
            editor = sharedPreferences.edit()
        }

        return editor!!
    }


    fun setLocationAllowed(autoLogin: Boolean) {
        this.locationAllowed = autoLogin
        getEditor().putBoolean(LOCATION_ALLOWED, autoLogin)
        editor!!.commit()
    }

    fun getLocationAllowed(): Boolean {
        this.locationAllowed = sharedPreferences.getBoolean(LOCATION_ALLOWED, false)
        return locationAllowed
    }

    fun setApiUrl(apiUrl: String) {
        this.apiUrl = apiUrl
        getEditor().putString(API_URL, apiUrl)
        editor!!.commit()
    }

    fun getApiUrl(): String {
        this.apiUrl = sharedPreferences.getString(API_URL, Constants.API_DEFAULT_URL)
        return apiUrl
    }

    fun getLoginData(): LoginPassword? {
        if (this.password != null && this.username != null) {
            return LoginPassword(this.username as String, this.password as String)
        }

        val password = sharedPreferences.getString(PASSWORD, null)
        val username = sharedPreferences.getString(USERNAME, null)


        if (password != null && username != null) {
            return LoginPassword(username, password)
        }

        return null
    }

    fun setPassword(password: String?) {
        if (password != null) {
            this.password = password
            getEditor().putString(PASSWORD, password)
            editor!!.commit()
        }
    }

    fun setUsername(card: String?) {
        if (card != null) {
            this.username = card
            getEditor().putString(USERNAME, card)
            editor!!.commit()
        }
    }

    fun setToken(token: Token?) {
        if (token != null) {
            this.token = token
        }
    }

    fun getToken(): Token? {
        return this.token
    }

    fun emptySharedPrefs() {
        getEditor().clear()
        editor!!.commit()
    }

    fun logout() {
        this.password = null
        this.username = null
        getEditor().remove(PASSWORD)
        getEditor().remove(USERNAME)
        emptySharedPrefs()
    }

    fun sessionIsAvailable(): Boolean {
        return getLoginData() != null
    }

    companion object {
        private val LOCATION_ALLOWED = "auth_helper_location_allowed"
        private val PASSWORD = "auth_helper_password"
        private val USERNAME = "auth_helper_username"
        private val API_URL = "auth_helper_api_url"
    }
}
