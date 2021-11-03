package com.hyapp.sytrrraewb.mainfolder

import android.app.Application
import android.content.ContextWrapper
import com.onesignal.OneSignal
import com.pixplicity.easyprefs.library.Prefs
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val APPS_KEY = "JM5VAJYte9GNJL4VmgkE2R"
const val ONE_S = "72810a4e-2074-44d6-bc5c-068c306dafd4"
const val GIST_LINK = "https://gist.githubusercontent.com/alpaco/e224cbeb814f202da66571cf4a7cadce/raw/Happy%2520strawberry/"

class Happy : Application() {

    var api: ApiInterface? = null

    override fun onCreate() {
        super.onCreate()
        startPrefs()
        startOneSignal()
        startRetrofit()
    }

    private fun startPrefs(){
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()
    }

    private fun startOneSignal(){
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONE_S)
    }

    private fun startRetrofit(){
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        api = Retrofit.Builder()
            .baseUrl(GIST_LINK)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ApiInterface::class.java)
    }


}