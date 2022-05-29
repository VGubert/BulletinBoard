package com.fiftyonepercent.bulletinboard.frag

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.fiftyonepercent.bulletinboard.R
import com.fiftyonepercent.bulletinboard.databinding.ListImageFragBinding
import com.fiftyonepercent.bulletinboard.utils.BillingManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

open class BaseAdsFrag: Fragment(), InterAdsClose {
    lateinit var adView: AdView
    var interAd: InterstitialAd? = null
    private var pref: SharedPreferences? = null
    private var isPremiumUser = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) //запускаем рекламу
        pref = activity?.getSharedPreferences(BillingManager.MAIN_PREF, AppCompatActivity.MODE_PRIVATE)
        isPremiumUser = pref?.getBoolean(BillingManager.REMOVE_ADS_PREF, false)!!
        if(!isPremiumUser){ //если не пермиум пользователь
            initAds()
            loadInterAd()
        }  else {
            adView.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadInterAd()
    }

    override fun onResume() { //при возобновлении
        super.onResume()
        adView.resume() //возобновляет рекламу
    }

    override fun onPause() { //при остановке
        super.onPause()
        adView.pause() //останавливает рекламу
    }

    override fun onDestroy() { //при разрушении
        super.onDestroy()
        adView.pause() //разрушает рекламу
    }

    private fun initAds(){ //реклама
        MobileAds.initialize(activity as Activity) //иницилизация
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest) //загружаем рекламу
    }

    private fun loadInterAd(){ //реклама на весь экран
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context as Activity, getString(R.string.ad_inter_id), adRequest, object :InterstitialAdLoadCallback(){
            override fun onAdLoaded(ad: InterstitialAd) { //загрузка рекламы
                 interAd = ad //присваиваем переменной рекламу
            }
        })
    }

    fun showInterAd() { //показываем рекламу на весь экран
        if(interAd != null) {
            interAd?.fullScreenContentCallback = object : FullScreenContentCallback(){ //FullScreenContentCallback() буедт следить с рекламой которая показалась
                override fun onAdDismissedFullScreenContent() {
                    onClose() //закрытие рекламы
                }
                override fun onAdFailedToShowFullScreenContent(p0: AdError) { //если вышла ошибка во время показа рекламы
                    onClose() //тоже закрываем
                }
            }
            interAd?.show(activity as Activity) //показываем рекламу
        } else {
            onClose()
        }

    }

    override fun onClose() { } //закрытие рекламы
}