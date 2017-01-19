package de.thepiwo.lifelogging.android.api.models

import de.thepiwo.lifelogging.android.api.models.logentities.CoordEntity

data class LogEntityReturn(var id: Long?, var userId: Long?, var key: String, var coordEntity: CoordEntity)
