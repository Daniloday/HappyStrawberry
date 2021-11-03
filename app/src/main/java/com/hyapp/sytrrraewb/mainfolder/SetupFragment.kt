package com.hyapp.sytrrraewb.mainfolder

import android.animation.ObjectAnimator
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appsflyer.AFLogger
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.applinks.AppLinkData
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.hyapp.sytrrraewb.MainActivity
import com.hyapp.sytrrraewb.R
import com.hyapp.sytrrraewb.databinding.FragmentSetupBinding


import com.onesignal.OneSignal
import com.pixplicity.easyprefs.library.Prefs
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*

const val STATE = "state"


class SetupFragment : Fragment() {

    private val binding : FragmentSetupBinding by viewBinding()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("start setup")
        setupProgressBar()
        when(val action = Prefs.getString(STATE,"null")){
            "null" -> newUser()
            "play" -> transferToPlay()
            else -> transferToWeb(action)
        }
    }

    private fun newUser(){
        println("new user")
        val act = (activity as MainActivity)

        val timeZone: String = TimeZone.getDefault().id
        val adb = Settings.Secure.getInt(activity?.contentResolver,
            "development_settings_enabled", 0)

        val conv = @Obfuscate object :
            AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionData: MutableMap<String, Any>) {
                val adId = AdvertisingIdClient.getAdvertisingIdInfo(act).id
                var campaign : String? = null
                if (conversionData.containsKey("campaign"))
                    campaign = conversionData["campaign"].toString()
                val appsId = AppsFlyerLib.getInstance().getAppsFlyerUID(act)
                var mediaSource : String? = null
                if (conversionData.containsKey("media_source"))
                    mediaSource = conversionData["media_source"].toString()

                lifecycleScope.launchWhenStarted {
                    val api = (act.application as Happy).api
                    val state : ResponseStateEntity = withContext(Dispatchers.IO){
                        try {
                            api?.requestState()
                        }catch (e: Exception) {
                            ResponseStateEntity()
                            withContext(Dispatchers.Main) {
                                transferToPlay()
                            }
                        }
                    } as ResponseStateEntity
                    fbInitialization(idFb = state.id, act = act)
                    if(state.access){
                        var url = "${state.url}?ad_id=${adId}&deviceID=${appsId}&settings=$adb&timeZone=$timeZone"
                        if (
                            mediaSource != null
                            && mediaSource.isNotBlank()
                            && mediaSource != "None"
                        )
                            url += "&m_source=${mediaSource}"
                        if (campaign != null){
                            url += "&sub_id_1=${campaign}"
                            optimizeOneSignal(campaign = campaign)
                            transferToWeb(url = url)
                        }
                        else
                            deepLink(url = url, act = act)
                    }
                    else {
                        transferToPlay()
                    }
                }

            }
            override fun onConversionDataFail(errorMessage: String) {
                println("fail conv1")
                transferToPlay()
            }
            override fun onAppOpenAttribution(attributionData: MutableMap<String, String>) {
                println("fail conv2")
                transferToPlay()
            }
            override fun onAttributionFailure(errorMessage: String) {
                println("fail conv3")
                transferToPlay()
            }
        }

        AppsFlyerLib.getInstance().setLogLevel(AFLogger.LogLevel.VERBOSE)
        AppsFlyerLib.getInstance().init(APPS_KEY, conv, act)
        AppsFlyerLib.getInstance().start(act)
    }

    private fun deepLink(url: String, act: MainActivity){
        AppLinkData.fetchDeferredAppLinkData(
            act
        ) {
            val source = it ?: AppLinkData.createFromActivity(act)
            if (source == null){
                transferToWeb(url)
            }
            else{
                val campaignFull = try {
                    source.targetUri.toString()
                } catch (e: Exception) {
                    "null"
                }
                if (campaignFull == "null") transferToWeb(url)
                else{
                    val campaign = campaignFull.substring(8,campaignFull.length)
                    optimizeOneSignal(campaign = campaign)
                    val fullUrl = "$url&sub_id_1=${campaign}"
                    transferToWeb(url = fullUrl)
                }

            }
        }
    }

    private fun fbInitialization(idFb: String, act : MainActivity) {
        try {
            FacebookSdk.setApplicationId(idFb)
            FacebookSdk.setAdvertiserIDCollectionEnabled(true)
            FacebookSdk.sdkInitialize(act)
            FacebookSdk.fullyInitialize()
            AppEventsLogger.activateApp(act)
        }
        catch (e : Exception){
            println(e)
        }

    }

    private fun transferToPlay(){
        println("Final game")
        Prefs.putString(STATE,"play")
        findNavController().navigate(SetupFragmentDirections.actionSetupFragmentToPlayFragment())
    }

    private fun transferToWeb(url : String){
        println("Final url:")
        println(url)
        Prefs.putString(STATE,url)
        findNavController().navigate(
            SetupFragmentDirections.actionSetupFragmentToWebFragment(url)
        )
    }

    private fun optimizeOneSignal(campaign : String){
        val pushToken = campaign
            .substringAfter("push=", "")
            .substringBefore("&")
        if (pushToken.isNotBlank()) {
            OneSignal.sendTag("sub_app", pushToken)
        }
    }

    private fun setupProgressBar(){
        lifecycleScope.launchWhenResumed {
            val bar = ObjectAnimator.ofInt(binding.progress, "progress", 0, 100)
            bar.duration = 8000L
            bar.start()

        }
    }


}