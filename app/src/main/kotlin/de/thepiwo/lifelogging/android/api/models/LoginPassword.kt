package de.thepiwo.lifelogging.android.api.models

import java.io.Serializable

class LoginPassword : Serializable {

    var login: String
    var password: String

    constructor(login: String, password: String) {
        this.login = login
        this.password = password
    }
}