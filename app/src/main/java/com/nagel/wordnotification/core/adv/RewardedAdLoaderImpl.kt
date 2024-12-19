package com.nagel.wordnotification.core.adv

import android.content.Context
import android.util.Log
import com.my.target.ads.Reward
import com.my.target.ads.RewardedAd
import com.my.target.common.models.IAdLoadingError
import com.nagel.wordnotification.core.analytecs.AppMetricaAnalytic
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton


//TODO могут возникать утечки памяти https://ads.yandex.com/helpcenter/ru/dev/android/rewarded
@Singleton
class RewardedAdLoaderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var rewardedAd: RewardedAd? = null
    private var isAdFailedToLoad = false
    private var counterLoad = 0
    private var award: () -> Unit = {}
    private var counterError = 0
    private var isLoaded = false

    fun init() {
        // Включение режима отладки
//        val myTargetConfig = MyTargetConfig.Builder()
//            .withTestDevices("931dd442-b37a-498a-9305-8318696bd43b")
//            .build()
//        MyTargetManager.setSdkConfig(myTargetConfig)
//        MyTargetManager.setDebugMode(true);
        Log.d(TAG, "fun init()")

        rewardedAd = RewardedAd(YOUR_SLOT_ID_2, context).apply {
            listener = object : RewardedAd.RewardedAdListener {
                override fun onLoad(p0: RewardedAd) {
                    isAdFailedToLoad = false
                    Log.d(TAG, "onLoad")
                    counterError = 0
                    isLoaded = true
                }

                override fun onNoAd(p0: IAdLoadingError, p1: RewardedAd) {
                    if (p0 == null) {
                        AppMetricaAnalytic.reportEvent("advertisement_did_not_appear")
                    }
                    isAdFailedToLoad = true
                    isLoaded = false
                    Log.e(TAG, "Ошибка загрузки рекламы: ${p0?.message} (code: ${p0?.code})")
                    AppMetricaAnalytic.reportEvent("ad_loading_error")
                    if (counterError++ < MAX_COUNTER_ERROR) {
                        init()
                    }
                }

                override fun onClick(p0: RewardedAd) {
                    //Вызывается, когда регистрируется клик по объявлению.
                    AppMetricaAnalytic.reportEvent("click_on_the_ad")
                    Log.d(TAG, "КЛИК ПО ОБЪЯВЛЕНИЮ")
                }

                override fun onDismiss(p0: RewardedAd) {
                    // Вызывается при закрытии объявления.
                    // Очистка ресурсов после закрытия объявления
                    Log.d(TAG, "ЗАКРЫТИЕ ОБЪЯВЛЕНИЯ")
                    AppMetricaAnalytic.reportEvent("closing_an_ad")
                    award.invoke()
                    init()
                }

                override fun onReward(p0: Reward, p1: RewardedAd) {
                    Log.d(TAG, "ВОЗНАГРАЖДЕНИЕ")
                }

                override fun onDisplay(p0: RewardedAd) {
                    Log.d(TAG, "ПОКАЗ РЕКЛАМЫ")
                }
            }
        }
        isLoaded = false
        rewardedAd?.load()
    }

    suspend fun showAdv(loaded: (Boolean) -> Unit) {
        if (isAdFailedToLoad) {
            counterError = 0
            init()
        }
        Log.d(TAG, "Нажата крнопка показа рекламы")
        counterLoad = 0
        while (isLoaded.not() && counterError < MAX_COUNTER_ERROR && counterLoad < 1000) {
            ++counterLoad
            delay(20)
        }
        loaded.invoke(isAdFailedToLoad || counterLoad >= 1000)
    }

    fun show(award: () -> Unit) {
        this.award = award
        rewardedAd?.show()
    }

    companion object {
        const val TAG = "RewardedAdLoaderImpl:::"
        private const val MAX_COUNTER_ERROR = 2
        const val YOUR_SLOT_ID_2 = 1744052
    }
}