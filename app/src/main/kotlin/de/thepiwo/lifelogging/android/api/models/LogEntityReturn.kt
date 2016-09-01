package de.thepiwo.lifelogging.android.api.models

import java.io.Serializable

class LogEntityReturn : Serializable {

    var id: Long?
    var userId: Long?
    var key: String
    var logCoordEntity: LogCoordEntity

    constructor(id: Long?, userId: Long?, key: String, logCoordEntity: LogCoordEntity) {
        this.id = id
        this.userId = userId
        this.key = key
        this.logCoordEntity = logCoordEntity
    }
}
