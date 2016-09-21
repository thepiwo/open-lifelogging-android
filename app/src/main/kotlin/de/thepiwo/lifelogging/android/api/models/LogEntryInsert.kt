package de.thepiwo.lifelogging.android.api.models

import java.io.Serializable

class LogEntryInsert : Serializable {

    var key: String
    var logCoordEntity: LogCoordEntity?
    var logWifiEntity: LogWifiEntity?

    constructor(logCoordEntity: LogCoordEntity) {
        this.key = "location"
        this.logCoordEntity = logCoordEntity
        this.logWifiEntity = null
    }

    constructor(logWifiEntity: LogWifiEntity) {
        this.key = "wifi"
        this.logCoordEntity = null
        this.logWifiEntity = logWifiEntity
    }
}