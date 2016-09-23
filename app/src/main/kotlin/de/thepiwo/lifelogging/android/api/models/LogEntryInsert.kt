package de.thepiwo.lifelogging.android.api.models

import de.thepiwo.lifelogging.android.api.models.logentities.CoordEntity
import de.thepiwo.lifelogging.android.api.models.logentities.WifiEntity
import java.io.Serializable

class LogEntryInsert : Serializable {

    var type: String
    var CoordEntity: CoordEntity?
    var WifiEntity: WifiEntity?

    constructor(coordEntity: CoordEntity) {
        this.type = "CoordEntity"
        this.CoordEntity = coordEntity
        this.WifiEntity = null
    }

    constructor(wifiEntity: WifiEntity) {
        this.type = "WifiEntity"
        this.CoordEntity = null
        this.WifiEntity = wifiEntity
    }
}