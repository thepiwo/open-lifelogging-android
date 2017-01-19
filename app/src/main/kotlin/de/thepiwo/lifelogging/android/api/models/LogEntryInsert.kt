package de.thepiwo.lifelogging.android.api.models

import de.thepiwo.lifelogging.android.api.models.logentities.CoordEntity
import de.thepiwo.lifelogging.android.api.models.logentities.WifiEntity
import java.io.Serializable

data class LogEntryInsert(
        var key: String,
        var data: Any,
        var createdAtClient: Long
) {
    constructor(coordEntity: CoordEntity) : this(
            key = "CoordEntity",
            data = coordEntity,
            createdAtClient = System.currentTimeMillis()
    )

    constructor(wifiEntity: WifiEntity) : this(
            key = "WifiEntity",
            data = wifiEntity,
            createdAtClient = System.currentTimeMillis()
    )
}