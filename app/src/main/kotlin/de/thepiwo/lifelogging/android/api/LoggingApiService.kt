package de.thepiwo.lifelogging.android.api

import de.thepiwo.lifelogging.android.api.models.*
import de.thepiwo.lifelogging.android.util.AuthHelper
import de.thepiwo.lifelogging.android.util.ConnectivityHelper
import retrofit2.adapter.rxjava.HttpException
import rx.Observable
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Named

class LoggingApiService
@Inject
constructor(@Named("unauthorized") unauthorizedLoggingApi: LoggingApi,
            @Named("authorized") authorizedLoggingApi: LoggingApi,
            authHelper: AuthHelper,
            connectivityHelper: ConnectivityHelper) {

    class NoInternetException : Exception("No internet connection available")

    class ApiTimeoutException : Exception("There was a connection timeout, try again later")

    class DataNotChangedException : Exception("Data has not changed")

    class LoginRequiredException : Exception("Login required")

    lateinit var unauthorizedLoggingApi: LoggingApi
    lateinit var authorizedLoggingApi: LoggingApi
    lateinit var authHelper: AuthHelper
    lateinit var connectivityHelper: ConnectivityHelper


    init {
        this.unauthorizedLoggingApi = unauthorizedLoggingApi
        this.authorizedLoggingApi = authorizedLoggingApi
        this.authHelper = authHelper
        this.connectivityHelper = connectivityHelper
    }

    private fun <Any> failOnErrorResult(observable: Observable<Any>): Observable<Any> {

        if (!connectivityHelper.connected()) {
            return Observable.error<Any>(NoInternetException())
        }

        return observable.onErrorResumeNext { throwable ->
            if (throwable is SocketTimeoutException) {
                Observable.error<Any>(ApiTimeoutException())
            }

            if (throwable is HttpException) {
                if (throwable.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Observable.error<Any>(LoginRequiredException())
                }
            }

            Observable.error<Any>(throwable)
        }.doOnNext({ t ->
            if (t is Token) {
                authHelper.setToken(t)
            }
        })
    }

    fun login(loginPassword: LoginPassword): Observable<Token> = failOnErrorResult(unauthorizedLoggingApi.getLogin(loginPassword))

}
