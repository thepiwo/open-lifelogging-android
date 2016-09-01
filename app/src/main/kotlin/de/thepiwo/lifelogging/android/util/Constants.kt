package de.thepiwo.lifelogging.android.util

interface Constants {
    companion object {

        // Strings
        val APP_NAME = "Open Lifelogging"

        val API_DEFAULT_URL = "http://192.168.178.125:9000/v1/"

        // Cache size in MB
        val CACHE_SIZE_MB: Long = 10

        // HTTP Request Timeouts in seconds
        val HTTP_READ_TIMEOUT: Long = 15
        val HTTP_WRITE_TIMEOUT: Long = 60

    }
}
