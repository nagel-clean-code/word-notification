package com.nagel.wordnotification.utils

import android.content.Context
import android.telephony.TelephonyManager

interface CountyTools {
    fun checkPermissionAnalytics(context: Context): Boolean
    fun getAgeOfMajorityOfCountry(context: Context): Int
}

object CountyUtils : CountyTools {
    /**
     * Возвращает доступность под политику сбора данных для ЕС
     */
    override fun checkPermissionAnalytics(context: Context): Boolean {
        val country = getCountry(context)
        return country?.let { !deepContains(countryEs, country) } ?: false
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

    override fun getAgeOfMajorityOfCountry(context: Context): Int {
        getCountry(context)?.let { country ->
            if (deepContains(countryComingOfAgeWith19, country)) return 19
            if (deepContains(countryComingOfAgeWith20, country)) return 20
            if (deepContains(countryComingOfAgeWith21, country)) return 21
        }
        return 18
    }

    private fun deepContains(list: List<String>, key: String): Boolean {
        list.forEach { item ->
            if (item.contains(key, ignoreCase = true) || key.contains(item, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private val countryComingOfAgeWith19 =
        listOf("dz", "co", "ca-nb", "ca-bc", "ca-ns", "ca", "ca-yt")

    private val countryComingOfAgeWith20 =
        listOf("bf", "kr", "nz", "py", "tw", "th", "tn", "jp")

    private val countryComingOfAgeWith21 =
        listOf(
            "ar", "bo", "bw", "ga", "gm", "gh", "hn", "eg",
            "id", "cm", "ci", "kw", "lr", "ly", "mg", "ml",
            "ne", "ni", "ae", "rw", "sn", "sg", "tg", "td"
        )

    /**
     * Страны взяты с сайта https://translated.turbopages.org/proxy_u/en-ru.ru.87e4c81a-67544e8c-13f28abe-74722d776562/https/worldpopulationreview.com/country-rankings/gdpr-countries
     */
    private val countryEs = listOf(
        "de", "vg", "fr", "it", "es", "pl", "ro", "nl", "be", "cz",
        "se", "pt", "gr", "hu", "at", "bg", "dk", "fi", "sk", "ie",
        "hr", "lt", "si", "lv", "ee", "cy", "lu", "mt", "ng", "br",
        "jp", "tr", "za", "ke", "kr", "ug", "ar", "ca", "il", "ch",
        "nz", "uy", "qa", "bh", "mu"
    )
}