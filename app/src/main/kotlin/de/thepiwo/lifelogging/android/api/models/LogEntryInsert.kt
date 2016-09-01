package de.thepiwo.lifelogging.android.api.models

import java.io.Serializable

class LogEntryInsert : Serializable {

    var logCoordEntity: LogCoordEntity

    constructor(logCoordEntity: LogCoordEntity) {
        this.logCoordEntity = logCoordEntity
    }
}