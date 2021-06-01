package de.thepiwo.lifelogging.android.api

import de.thepiwo.lifelogging.android.api.models.*
import de.thepiwo.lifelogging.android.util.AuthHelper
import de.thepiwo.lifelogging.android.util.ConnectivityHelper
import io.reactivex.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody.Part.Companion.createFormData
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Named

class LoggingApiService
@Inject
constructor(@Named("unauthorized") var unauthorizedLoggingApi: LoggingApi,
            @Named("authorized") var authorizedLoggingApi: LoggingApi,
            var authHelper: AuthHelper,
            private var connectivityHelper: ConnectivityHelper) {

    class NoInternetException : Exception("No internet connection available")

    class ApiTimeoutException : Exception("There was a connection timeout, try again later")

    class AuthorizationException : Exception("Login failed")

    class LoginRequiredException : Exception("Login required")

    private fun <Any> failOnErrorResult(observable: Observable<Any>): Observable<Any> {

        if (!connectivityHelper.connected()) {
            return Observable.error<Any>(NoInternetException())
        }

        return observable.onErrorResumeNext { throwable: Throwable ->
            if (throwable is SocketTimeoutException) {
                Observable.error<Any>(ApiTimeoutException())
            }

            if (throwable is HttpException) {
                if (throwable.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Observable.error<Any>(LoginRequiredException())
                }
            }

            Observable.error<Any>(throwable)
        }.doOnNext { t ->
            if (t is Token) {
                authHelper.setToken(t)
            }
        }
    }

    fun login(loginPassword: LoginPassword): Observable<Token> = failOnErrorResult(unauthorizedLoggingApi.login(loginPassword))

    fun createLogItem(logEntryInsert: LogEntryInsert): Observable<LogEntityReturn> = failOnErrorResult(authorizedLoggingApi.createLogItem(logEntryInsert.key, logEntryInsert))

    fun getLogs(limit: Long): Observable<LogList> = failOnErrorResult(authorizedLoggingApi.getLogs(limit))

    fun importSamsung(file: File): Observable<Long> {
        val filePart = createFormData(
            "zip",
            file.name,
            file.asRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        )

        return failOnErrorResult(authorizedLoggingApi.importSamsung(filePart))
    }

}
