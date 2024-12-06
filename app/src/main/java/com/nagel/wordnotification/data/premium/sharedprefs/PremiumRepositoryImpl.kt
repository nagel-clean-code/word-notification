package com.nagel.wordnotification.data.premium.sharedprefs

import android.content.Context
import com.nagel.wordnotification.Constants.COUNT_FREE_USE_RANDOMIZER
import com.nagel.wordnotification.Constants.NUMBER_OF_FREE_WORDS
import com.nagel.wordnotification.Constants.NUMBER_OF_FREE_WORDS_PER_ADVERTISEMENT
import com.nagel.wordnotification.data.premium.PremiumRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumRepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context
) : PremiumRepository {

    private val sharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_PREMIUM, Context.MODE_PRIVATE)

    override fun getAddNumberFreeRandomizer(): Int {
        return sharedPreferences.getInt(ADD_NUMBER_FREE_RANDOMIZER, COUNT_FREE_USE_RANDOMIZER)
    }

    override fun saveAddNumberFreeRandomizer(number: Int) {
        sharedPreferences.edit().putInt(ADD_NUMBER_FREE_RANDOMIZER, number).apply()
    }

    override fun getAddNumberFreeWords(): Int {
        return sharedPreferences.getInt(
            ADD_NUMBER_FREE_WORDS,
            NUMBER_OF_FREE_WORDS_PER_ADVERTISEMENT
        )
    }

    override fun saveAddNumberFreeWords(number: Int) {
        sharedPreferences.edit().putInt(ADD_NUMBER_FREE_WORDS, number).apply()
    }

    override fun getMinFreeRandomizer(): Int {
        return sharedPreferences.getInt(MIN_FREE_RANDOMIZER, COUNT_FREE_USE_RANDOMIZER)
    }

    override fun saveMinFreeRandomizer(number: Int) {
        sharedPreferences.edit().putInt(MIN_FREE_RANDOMIZER, number).apply()
    }

    override fun getMinFreeWords(): Int {
        return sharedPreferences.getInt(MIN_FREE_WORDS, NUMBER_OF_FREE_WORDS)
    }

    override fun saveMinFreeWords(number: Int) {
        sharedPreferences.edit().putInt(MIN_FREE_WORDS, number).apply()
    }

    override fun saveIsStarted(isStarted: Boolean) {
        sharedPreferences.edit().putBoolean(IS_STARTED, isStarted).apply()
    }

    override fun getIsStarted(): Boolean {
        return sharedPreferences.getBoolean(IS_STARTED, false)
    }

    override fun getCurrentLimitWord(): Int {
        return sharedPreferences.getInt(LIMIT_WORDS, NUMBER_OF_FREE_WORDS)
    }

    override fun saveCurrentLimitWords(limit: Int) {
        sharedPreferences.edit().putInt(LIMIT_WORDS, limit).apply()
    }

    override fun getCurrentLimitRandomizer(): Int {
        val lastDate = sharedPreferences.getString(LAST_DATE_USE_RANDOMIZER, "")
        return if (simpleCurrentDateFormat.format(Date().time) != lastDate) {
            getMinFreeRandomizer()
        } else {
            sharedPreferences.getInt(LIMIT_RANDOMIZER, getMinFreeRandomizer())
        }
    }

    override fun saveCurrentLimitRandomizer(limit: Int) {
        val date = simpleCurrentDateFormat.format(Date().time)
        sharedPreferences.edit().putString(LAST_DATE_USE_RANDOMIZER, date).apply()
        sharedPreferences.edit().putInt(LIMIT_RANDOMIZER, limit).apply()
    }

    companion object {
        private const val SHARED_PREFS_PREMIUM = "SHARED_PREFS_PREMIUM"
        private const val ADD_NUMBER_FREE_RANDOMIZER = "ADD_NUMBER_FREE_RANDOMIZER"
        private const val ADD_NUMBER_FREE_WORDS = "ADD_NUMBER_FREE_WORDS"
        private const val MIN_FREE_RANDOMIZER = "MIN_FREE_RANDOMIZER"
        private const val MIN_FREE_WORDS = "MIN_FREE_WORDS"
        private const val IS_STARTED = "IS_STARTED" //это премиум для шифровки
        private const val LIMIT_WORDS = "LIMIT_WORDS"
        private const val LIMIT_RANDOMIZER = "LIMIT_RANDOMIZER"
        private const val LAST_DATE_USE_RANDOMIZER = "LAST_DATE_USE_RANDOMIZER"

        private val simpleCurrentDateFormat = SimpleDateFormat("yyyy-MM-dd")
    }
}