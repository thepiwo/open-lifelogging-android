package de.thepiwo.lifelogging.android.api.models

import java.io.Serializable

class Token : Serializable {

    var id: Long
    var userId: Long
    var token: String

    constructor(id: Long, userId: Long, token: String) {
        this.id = id
        this.userId = userId
        this.token = token
    }
}
