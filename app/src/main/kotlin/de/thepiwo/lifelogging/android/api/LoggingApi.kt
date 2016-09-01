package de.thepiwo.lifelogging.android.api

import de.thepiwo.lifelogging.android.api.models.LoginPassword
import de.thepiwo.lifelogging.android.api.models.Token
import retrofit2.http.Body
import retrofit2.http.POST
import rx.Observable

interface LoggingApi {

    @POST("auth/signIn")
    fun getLogin(
            @Body loginPassword: LoginPassword
    ): Observable<Token>
}