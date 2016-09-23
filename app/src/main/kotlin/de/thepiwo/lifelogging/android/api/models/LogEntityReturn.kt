package de.thepiwo.lifelogging.android.api.models

import de.thepiwo.lifelogging.android.api.models.logentities.CoordEntity
import java.io.Serializable

class LogEntityReturn : Serializable {

    var id: Long?
    var userId: Long?
    var key: String
    var coordEntity: CoordEntity

    constructor(id: Long?, userId: Long?, key: String, coordEntity: CoordEntity) {
        this.id = id
        this.userId = userId
        this.key = key
        this.coordEntity = coordEntity
    }
}
