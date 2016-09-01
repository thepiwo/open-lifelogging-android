package de.thepiwo.lifelogging.android.api.models

import java.io.Serializable

class LogCoordEntity : Serializable {

    var id: Long?
    var logEntityId: Long?
    var latitude: Double
    var longitude: Double
    var altitude: Double
    var accuracy: Float

    constructor(id: Long?, logEntityId: Long?, latitude: Double, longitude: Double, altitude: Double, accuracy: Float) {
        this.id = id
        this.logEntityId = logEntityId
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        this.accuracy = accuracy
    }
}
