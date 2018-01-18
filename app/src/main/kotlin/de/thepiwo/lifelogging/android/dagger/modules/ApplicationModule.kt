package de.thepiwo.lifelogging.android.dagger.modules

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import de.thepiwo.lifelogging.android.BaseApplication
import de.thepiwo.lifelogging.android.api.LoggingApi
import de.thepiwo.lifelogging.android.api.LoggingApiService
import de.thepiwo.lifelogging.android.dagger.ForApplication
import de.thepiwo.lifelogging.android.util.*
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named


@Module
class ApplicationModule(private val application: BaseApplication) {


    @Provides
    @ForApplication
    fun provideApplicationContext(): Context {
        return application
    }

    @Provides
    @ForApplication
    fun provideNavigator(): Navigator {
        return Navigator()
    }

    @Provides
    @ForApplication
    fun provideSharedPreferences(applicationContext: Context): SharedPreferences {
        return applicationContext.getSharedPreferences(Constants.APP_NAME, Context.MODE_PRIVATE)
    }

    // -------- COMMUNICATION: Cache, Retrofit, OkHttp, API --------
    @Provides
    @ForApplication
    fun provideAuthHelper(gson: Gson, sharedPreferences: SharedPreferences): AuthHelper {
        return AuthHelper(gson, sharedPreferences)
    }

    @Provides
    @ForApplication
    fun provideDataHandler(loggingApiService: LoggingApiService, authHelper: AuthHelper): DataHandler {
        return DataHandler(loggingApiService, authHelper)
    }

    @Provides
    @ForApplication
    fun provideConnectivityHelper(applicationContext: Context): ConnectivityHelper {
        return ConnectivityHelper(applicationContext)
    }

    @Provides
    @ForApplication
    fun provideCache(applicationContext: Context): Cache {
        val cacheSize = Constants.CACHE_SIZE_MB * 1024 * 1024
        val cacheDir = applicationContext.cacheDir
        return Cache(cacheDir, cacheSize)
    }

    @Provides
    @Named("unauthorized")
    @ForApplication
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
    @ForApplication
    fun provideAuthorizedOkHttpClient(cache: Cache, authHelper: AuthHelper, applicationContext: Context): OkHttpClient {
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
    @ForApplication
    fun provideUnauthorizedBalanceApi(@Named("unauthorized") okHttpClient: OkHttpClient, gson: Gson, authHelper: AuthHelper): LoggingApi {
        return Retrofit.Builder().client(okHttpClient).baseUrl(authHelper.getApiUrl()).addConverterFactory(GsonConverterFactory.create(gson)).addCallAdapterFactory(RxJavaCallAdapterFactory.create()).build().create(LoggingApi::class.java)
    }

    @Provides
    @Named("authorized")
    @ForApplication
    fun provideAuthorizedBalanceApi(@Named("authorized") okHttpClient: OkHttpClient, gson: Gson, authHelper: AuthHelper): LoggingApi {
        return Retrofit.Builder().client(okHttpClient).baseUrl(authHelper.getApiUrl()).addConverterFactory(GsonConverterFactory.create(gson)).addCallAdapterFactory(RxJavaCallAdapterFactory.create()).build().create(LoggingApi::class.java)
    }

    @Provides
    @ForApplication
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }
}
