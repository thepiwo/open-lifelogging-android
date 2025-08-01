package de.thepiwo.lifelogging.android.dagger.modules

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.thepiwo.lifelogging.android.api.LoggingApi
import de.thepiwo.lifelogging.android.api.LoggingApiService
import de.thepiwo.lifelogging.android.util.AuthHelper
import de.thepiwo.lifelogging.android.util.ConnectivityHelper
import de.thepiwo.lifelogging.android.util.Constants
import de.thepiwo.lifelogging.android.util.DataHandler
import de.thepiwo.lifelogging.android.util.LocalDateTimeConverter
import de.thepiwo.lifelogging.android.util.Navigator
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Named


@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    fun provideNavigator(): Navigator {
        return Navigator()
    }

    @Provides
    fun provideSharedPreferences(@ApplicationContext applicationContext: Context): SharedPreferences {
        return applicationContext.getSharedPreferences(Constants.APP_NAME, Context.MODE_PRIVATE)
    }

    // -------- COMMUNICATION: Cache, Retrofit, OkHttp, API --------
    @Provides
    fun provideAuthHelper(gson: Gson, sharedPreferences: SharedPreferences): AuthHelper {
        return AuthHelper(gson, sharedPreferences)
    }

    @Provides
    fun provideDataHandler(loggingApiService: LoggingApiService, authHelper: AuthHelper): DataHandler {
        return DataHandler(loggingApiService, authHelper)
    }

    @Provides
    fun provideConnectivityHelper(@ApplicationContext applicationContext: Context): ConnectivityHelper {
        return ConnectivityHelper(applicationContext)
    }

    @Provides
    fun provideCache(@ApplicationContext applicationContext: Context): Cache {
        val cacheSize = Constants.CACHE_SIZE_MB * 1024 * 1024
        val cacheDir = applicationContext.cacheDir
        return Cache(cacheDir, cacheSize)
    }

    @Provides
    @Named("unauthorized")
    fun provideUnauthorizedOkHttpClient(cache: Cache): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder().addInterceptor(interceptor).cache(cache)
                .addNetworkInterceptor { chain ->
                    val request = chain.request().newBuilder().addHeader("X-Client", "Android-App").build()
                    chain.proceed(request)
                }
                .readTimeout(Constants.HTTP_READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.HTTP_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build()
    }

    @Provides
    @Named("authorized")
    fun provideAuthorizedOkHttpClient(cache: Cache, authHelper: AuthHelper, @ApplicationContext applicationContext: Context): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .cache(cache)
                .addNetworkInterceptor { chain ->
                    val request = chain.request().newBuilder().addHeader("Token",
                            if (authHelper.getToken() != null)
                                authHelper.getToken()!!.token
                            else
                                "").addHeader("X-Client", "Android-App")
                            .build()
                    chain.proceed(request)
                }
                .addNetworkInterceptor { chain ->
                    val originalResponse = chain.proceed(chain.request())
                    if (ConnectivityHelper(applicationContext).connected()) {
                        val maxAge = 60 // read from cache for 1 minute
                        originalResponse.newBuilder()
                                .header("Cache-Control", "public, max-age=" + maxAge)
                                .build()
                    } else {
                        val maxStale = 60 * 60 * 24 * 28 // tolerate 4-weeks stale
                        originalResponse.newBuilder()
                                .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                                .build()
                    }
                }
                .readTimeout(Constants.HTTP_READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.HTTP_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build()
    }

    @Provides
    @Named("unauthorized")
    fun provideUnauthorizedBalanceApi(@Named("unauthorized") okHttpClient: OkHttpClient, gson: Gson, authHelper: AuthHelper): LoggingApi {
        return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(authHelper.getApiUrl())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(LoggingApi::class.java)
    }

    @Provides
    @Named("authorized")
    fun provideAuthorizedBalanceApi(@Named("authorized") okHttpClient: OkHttpClient, gson: Gson, authHelper: AuthHelper): LoggingApi {
        return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(authHelper.getApiUrl())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(LoggingApi::class.java)
    }

    @Provides
    fun provideLoggingApiService(
        @Named("unauthorized") unauthorizedLoggingApi: LoggingApi,
        @Named("authorized") authorizedLoggingApi: LoggingApi,
        authHelper: AuthHelper,
        connectivityHelper: ConnectivityHelper
    ): LoggingApiService {
        return LoggingApiService(unauthorizedLoggingApi, authorizedLoggingApi, authHelper, connectivityHelper)
    }

    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
                .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeConverter())
                .create()
    }
}
