package de.thepiwo.lifelogging.android.api.models

import de.thepiwo.lifelogging.android.api.models.logentities.CoordEntity
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class LogEntityReturn(
        var id: Long?,
        var userId: Long?,
        var key: String,
        var hash: String,
        var createdAtClient: LocalDateTime,
        var createdAt: LocalDateTime,
        var data: CoordEntity
) {
    fun createdAtClientString(): String =
            createdAtClient
                    .atZone(ZoneOffset.UTC)
                    .withZoneSameInstant(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_TIME)

}
