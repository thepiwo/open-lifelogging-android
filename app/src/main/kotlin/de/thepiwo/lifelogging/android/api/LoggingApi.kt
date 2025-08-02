package de.thepiwo.lifelogging.android.api

import de.thepiwo.lifelogging.android.api.models.LogEntityReturn
import de.thepiwo.lifelogging.android.api.models.LogEntryInsert
import de.thepiwo.lifelogging.android.api.models.LogList
import de.thepiwo.lifelogging.android.api.models.LoginPassword
import de.thepiwo.lifelogging.android.api.models.Token
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("logs")
    fun getLogsByDateRange(
        @Query("date") fromDate: String,
        @Query("toDate") toDate: String
    ): Observable<LogList>

    @Multipart
    @POST("import/samsung")
    fun importSamsung(@Part filePart: MultipartBody.Part): Observable<Long>

    @Multipart
    @POST("import/google")
    fun importGoogle(@Part filePart: MultipartBody.Part): Observable<Long>
}