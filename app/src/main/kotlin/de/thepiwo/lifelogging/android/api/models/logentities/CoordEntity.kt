package de.thepiwo.lifelogging.android.api.models.logentities

data class CoordEntity(
        var id: Long?,
        var logEntityId: Long?,
        var latitude: Double,
        var longitude: Double,
        var altitude: Double,
        var accuracy: Float
)