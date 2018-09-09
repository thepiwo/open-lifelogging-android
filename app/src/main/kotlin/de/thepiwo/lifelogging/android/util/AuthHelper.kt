package de.thepiwo.lifelogging.android.util

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import de.thepiwo.lifelogging.android.api.models.LoginPassword
import de.thepiwo.lifelogging.android.api.models.Token
import de.thepiwo.lifelogging.android.dagger.ForApplication
import javax.inject.Inject

@ForApplication
class AuthHelper
@Inject
constructor(val gson: Gson, private val sharedPreferences: SharedPreferences) {

    private var editor: SharedPreferences.Editor? = null

    private var locationAllowed: Boolean = false
    private var apiUrl: String = Constants.API_DEFAULT_URL
    private var password: String? = null
    private var username: String? = null
    private var token: Token? = null

    @SuppressLint("CommitPrefEdits")
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

    fun getToken(): Token? {
        if (this.token != null) {
            return token
        }

        val tokenJson = sharedPreferences.getString(TOKEN, null)
        if (tokenJson != null) {
            token = gson.fromJson<Token>(tokenJson)
        }

        return token
    }

    fun setToken(token: Token?) {
        if (token != null) {
            this.token = token
            getEditor().putString(TOKEN, gson.toJson(token))
            editor!!.commit()
        }
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

    fun sessionIsAuthorized(): Boolean {
        return getToken() != null
    }

    companion object {
        const val LOCATION_ALLOWED = "auth_helper_location_allowed"
        const val PASSWORD = "auth_helper_password"
        const val USERNAME = "auth_helper_username"
        const val API_URL = "auth_helper_api_url"
        const val TOKEN = "auth_helper_token"
    }
}
