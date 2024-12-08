package com.nagel.wordnotification.core.adv

import android.app.Activity
import android.content.Context
import android.util.Log
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appmetrica.analytics.AppMetrica
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

//TODO могут возникать утечки памяти https://ads.yandex.com/helpcenter/ru/dev/android/rewarded
@Singleton
class RewardedAdLoaderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var rewardedAd: RewardedAd? = null
    private var isAward = false
    private var isAdFailedToLoad = false
    private var counterLoad = 0

    private val rewardedAdLoader = RewardedAdLoader(context).apply {
        setAdLoadListener(object : RewardedAdLoadListener {
            override fun onAdLoaded(rewarded: RewardedAd) {
                isAdFailedToLoad = false
                Log.d(TAG, "Реклама загрузилась")
                rewardedAd = rewarded
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                isAdFailedToLoad = true
                Log.e(TAG, "Ошибка загрузки рекламы: ${error.description} (errorObject: $error)")
                AppMetrica.reportEvent("ad_loading_error")
            }
        })
    }

    init {
        loadRewardedAd()
    }

    suspend fun showAdv(activity: Activity, award: () -> Unit, loaded: (Boolean) -> Unit) {
        Log.d(TAG, "Нажата крнопка показа рекламы")
        counterLoad = 0
        while (rewardedAd == null && !isAdFailedToLoad && counterLoad < 1000) {
            ++counterLoad
            delay(20)
        }
        loaded.invoke(isAdFailedToLoad || counterLoad >= 1000)
        rewardedAd?.apply {
            setAdEventListener(object : RewardedAdEventListener {
                override fun onAdShown() {
                    Log.d(TAG, "ПОКАЗ РЕКЛАМЫ")
                    // Вызывается при показе рекламы.
                }

                override fun onAdFailedToShow(adError: AdError) {
                    //Вызывался, когда реклама с вознаграждением не показывалась
                    // Очистить ресурсы после сбоя показа рекламы
                    rewardedAd?.setAdEventListener(null)
                    rewardedAd = null
                    AppMetrica.reportEvent("advertisement_did_not_appear")

                    // Теперь вы можете предварительно загрузить следующее рекламное объявление с вознаграждением.
                    loadRewardedAd()
                }

                override fun onAdDismissed() {
                    // Вызывается при закрытии объявления.
                    // Очистка ресурсов после закрытия объявления
                    Log.d(TAG, "ЗАКРЫТИЕ ОБЪЯВЛЕНИЯ")
                    rewardedAd?.setAdEventListener(null)
                    rewardedAd = null

                    AppMetrica.reportEvent("closing_an_ad")
                    if (isAward) {
                        isAward = false
                        award.invoke()
                    }
                    //Теперь вы можете предварительно загрузить следующее рекламное объявление с вознаграждением.
                    loadRewardedAd()
                }

                override fun onAdClicked() {
                    //Вызывается, когда регистрируется клик по объявлению.
                    AppMetrica.reportEvent("click_on_the_ad")
                    Log.d(TAG, "КЛИК ПО ОБЪЯВЛЕНИЮ")
                }

                override fun onAdImpression(impressionData: ImpressionData?) {
                    //Вызывается при регистрации показа рекламы.
                    Log.d(TAG, "ПОКАЗ РЕКЛАМЫ")
                }

                override fun onRewarded(reward: Reward) {
                    Log.d(TAG, "ВОЗНАГРАЖДЕНИЕ")
                    isAward = true
                    // Вызывается, когда пользователь может быть вознагражден.
                }
            })
            show(activity)
        }
    }

    private fun loadRewardedAd() {
        val adRequestConfiguration = AdRequestConfiguration.Builder("R-M-13190463-1").build()
        rewardedAdLoader.loadAd(adRequestConfiguration)
    }

    companion object {
        const val TAG = "RewardedAdLoaderImpl:::"
    }
}