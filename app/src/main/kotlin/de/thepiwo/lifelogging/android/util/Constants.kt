package de.thepiwo.lifelogging.android.util

interface Constants {
    companion object {

        // Strings
        const val APP_NAME = "Open Lifelogging"

        const val API_DEFAULT_URL = "http://192.168.0.102:9001/v1/"

        // Cache size in MB
        const val CACHE_SIZE_MB: Long = 10

        // HTTP Request Timeouts in seconds
        const val HTTP_READ_TIMEOUT: Long = 15
        const val HTTP_WRITE_TIMEOUT: Long = 60

    }
}
