package com.nagel.wordnotification.utils

import android.content.Context
import android.telephony.TelephonyManager


object CheckCountyEs {

    /**
     * Возвращает доступность под политику сбора данных для ЕС
     */
    fun checkPermissionAnalytics(context: Context): Boolean {
        val country = getCountry(context)
        return country?.let { countryEs.contains(country).not() } ?: false
    }

    private fun getCountry(context: Context): String? {
        val phoneManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        phoneManager?.let {
            phoneManager.simCountryIso?.let { country ->
                return country
            }
            phoneManager.networkCountryIso?.let { country ->
                return country
            }
        }
        return null
    }

    /**
     * Страны взяты с сайта https://translated.turbopages.org/proxy_u/en-ru.ru.87e4c81a-67544e8c-13f28abe-74722d776562/https/worldpopulationreview.com/country-rankings/gdpr-countries
     */
    private val countryEs = listOf(
        "de",
        "vg",
        "fr",
        "it",
        "es",
        "pl",
        "ro",
        "nl",
        "be",
        "cz",
        "se",
        "pt",
        "gr",
        "hu",
        "at",
        "bg",
        "dk",
        "fi",
        "sk",
        "ie",
        "hr",
        "lt",
        "si",
        "lv",
        "ee",
        "cy",
        "lu",
        "mt",
        "ng",
        "br",
        "jp",
        "tr",
        "za",
        "ke",
        "kr",
        "ug",
        "ar",
        "ca",
        "il",
        "ch",
        "nz",
        "uy",
        "qa",
        "bh",
        "mu",
    )
}