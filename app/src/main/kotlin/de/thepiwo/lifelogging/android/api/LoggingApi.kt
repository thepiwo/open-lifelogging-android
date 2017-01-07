package de.thepiwo.lifelogging.android.api

import de.thepiwo.lifelogging.android.api.models.LogEntityReturn
import de.thepiwo.lifelogging.android.api.models.LogEntryInsert
import de.thepiwo.lifelogging.android.api.models.LoginPassword
import de.thepiwo.lifelogging.android.api.models.Token
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import rx.Observable

interface LoggingApi {

    @POST("auth/signIn")
    fun login(
            @Body loginPassword: LoginPassword
    ): Observable<Token>

    @POST("logs/key/{key}")
    fun createLogItem(
            @Path("key") key: String,
            @Body data: LogEntryInsert
    ): Observable<LogEntityReturn>
}