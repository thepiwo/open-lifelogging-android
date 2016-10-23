package de.thepiwo.lifelogging.android.api.models.logentities

import java.io.Serializable

class WifiEntity : Serializable {

    var id: Long?
    var logEntityId: Long?
    var ssid: String
    var speed: Int
    var status: String

    constructor(id: Long?, logEntityId: Long?, ssid: String, speed: Int, status: String) {
        this.id = id
        this.logEntityId = logEntityId
        this.ssid = ssid
        this.speed = speed
        this.status = status
    }
}