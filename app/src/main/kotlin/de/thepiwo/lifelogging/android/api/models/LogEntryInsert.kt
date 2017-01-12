package de.thepiwo.lifelogging.android.api.models

import de.thepiwo.lifelogging.android.api.models.logentities.CoordEntity
import de.thepiwo.lifelogging.android.api.models.logentities.WifiEntity
import java.io.Serializable

class LogEntryInsert : Serializable {

    var key: String
    var data: Any
    var createdAtClient: Long

    constructor(coordEntity: CoordEntity) {
        this.key = "CoordEntity"
        this.data = coordEntity
        this.createdAtClient = System.currentTimeMillis()
    }

    constructor(wifiEntity: WifiEntity) {
        this.key = "WifiEntity"
        this.data = wifiEntity
        this.createdAtClient = System.currentTimeMillis()
    }
}