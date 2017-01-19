package de.thepiwo.lifelogging.android.api.models.logentities

data class WifiEntity(
        var id: Long?,
        var logEntityId: Long?,
        var ssid: String,
        var speed: Int,
        var status: String
)