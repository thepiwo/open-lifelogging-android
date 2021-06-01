package de.thepiwo.lifelogging.android.api

import de.thepiwo.lifelogging.android.api.models.*
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.http.*

interface LoggingApi {

    @POST("auth/signIn")
    fun login(@Body loginPassword: LoginPassword): Observable<Token>

    @POST("logs/key/{key}")
    fun createLogItem(
        @Path("key") key: String,
        @Body data: LogEntryInsert
    ): Observable<LogEntityReturn>

    @GET("logs/latest")
    fun getLogs(@Query("limit") limit: Long): Observable<LogList>

    @Multipart
    @POST("import/samsung")
    fun importSamsung(@Part filePart: MultipartBody.Part): Observable<Long>
}