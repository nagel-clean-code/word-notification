package com.nagel.wordnotification.data.firbase

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.nagel.wordnotification.BuildConfig
import com.nagel.wordnotification.data.firbase.entity.CurrentPrices
import com.nagel.wordnotification.data.firbase.entity.CurrentVersionData
import com.nagel.wordnotification.data.firbase.entity.DictionariesLibrary
import com.nagel.wordnotification.data.firbase.entity.FeatureToggles
import com.nagel.wordnotification.data.firbase.entity.PremiumSettings
import com.nagel.wordnotification.data.premium.PremiumRepository
import com.nagel.wordnotification.presentation.navigator.MainNavigator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDbRepository @Inject constructor(
    private val navigator: MainNavigator,
    private val premiumRepository: PremiumRepository
) {
    private val fireStore: FirebaseFirestore by lazy { Firebase.firestore }
    private var currentPrices: CurrentPrices? = null
    private var featureToggles: FeatureToggles? = null
    private var dictionariesLibrary: DictionariesLibrary? = null
    private var premiumSettings: PremiumSettings? = null

    //    private var testing = Date().time < 1704300235000
    private var testing = false

    init {
        getFeatureToggles()
        requestPremiumSettings()
        requestPremiumInformation()
        requestActualVersionApp()
    }

    fun getFeatureToggles(
        success: (FeatureToggles) -> Unit = {},
        error: () -> Unit = {}
    ) {
        featureToggles?.let {
            success(it)
            return
        }
        fireStore.collection("featureToggle")
            .document("allVersions")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                try {
                    documentSnapshot.toObject<FeatureToggles>()?.let {
                        getTogglesCurrentVersion(it, success)
                    }
                } catch (e: Exception) {
                    error.invoke()
                    Log.w(ContentValues.TAG, "Error getting documents: ", e)
                    return@addOnSuccessListener
                }
            }.addOnFailureListener { exception ->
                error.invoke()
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    private fun getTogglesCurrentVersion(
        mergeToggles: FeatureToggles,
        success: (FeatureToggles) -> Unit,
    ) {
        fireStore.collection("featureToggle")
            .document(BuildConfig.VERSION_NAME)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                try {
                    featureToggles =
                        documentSnapshot.toObject<FeatureToggles>()?.merge(mergeToggles)
                    if (featureToggles == null) {
                        featureToggles = mergeToggles
                    }
                    featureToggles?.let(success)
                } catch (e: Exception) {
                    success(mergeToggles)
                    return@addOnSuccessListener
                }
            }.addOnFailureListener {
                success(mergeToggles)
            }
    }

    fun requestGetDictionaries(
        success: (DictionariesLibrary) -> Unit = {},
        error: () -> Unit = {}
    ) {
        dictionariesLibrary?.let {
            success(it)
            return
        }
        fireStore.collection("dictionaries")
            .document("all")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                try {
                    dictionariesLibrary = documentSnapshot.toObject<DictionariesLibrary>()!!
                    dictionariesLibrary?.let(success)
                } catch (e: Exception) {
                    error.invoke()
                    Log.w(ContentValues.TAG, "Error getting documents: ", e)
                    return@addOnSuccessListener
                }
            }.addOnFailureListener { exception ->
                error.invoke()
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    fun requestPremiumInformation(
        success: (CurrentPrices) -> Unit = {},
        error: () -> Unit = {}
    ) {
        currentPrices?.let {
            success(it)
            return
        }
        fireStore.collection("premium")
            .document("currentPrices")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                try {
                    currentPrices = documentSnapshot.toObject<CurrentPrices>()
                    currentPrices?.let { success(it) }
                } catch (e: Exception) {
                    error()
                    Log.w(ContentValues.TAG, "Error getting documents: ", e)
                    return@addOnSuccessListener
                }
            }.addOnFailureListener { exception ->
                error()
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    fun requestPremiumSettings(
        success: (PremiumSettings) -> Unit = {},
        error: () -> Unit = {}
    ) {
        premiumSettings?.let {
            success(it)
            return
        }
        fireStore.collection("premium")
            .document("settings")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                try {
                    premiumSettings = documentSnapshot.toObject<PremiumSettings>()
                    premiumSettings?.apply {
                        premiumRepository.saveMinFreeWords(minFreeWords)
                        premiumRepository.saveAddNumberFreeWords(addNumberFreeWords)
                        premiumRepository.saveAddNumberFreeRandomizer(addNumberFreeRandomizer)
                        premiumRepository.saveMinFreeRandomizer(minFreeRandomizer)
                        premiumRepository.saveAllIsStarted(allPremium.seconds)
                    }
                    premiumSettings?.let { success(it) }
                } catch (e: Exception) {
                    error()
                    Log.w(ContentValues.TAG, "Error getting documents: ", e)
                    return@addOnSuccessListener
                }
            }.addOnFailureListener { exception ->
                error()
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    private fun requestActualVersionApp() {
        fireStore.collection("current_viersion_app")
            .document("data")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                try {
                    documentSnapshot.toObject<CurrentVersionData>()?.let {
                        checkCurrentVersionApp(it)
                    }
                } catch (e: Exception) {
                    Log.w(ContentValues.TAG, "Error getting documents: ", e)
                    return@addOnSuccessListener
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    private fun checkCurrentVersionApp(currentVersionData: CurrentVersionData) {
        val option = currentVersionData.optionalUpdates.contains(BuildConfig.VERSION_CODE)
        val mandatoryUpdates =
            currentVersionData.mandatoryUpdates.contains(BuildConfig.VERSION_CODE)
        if (option || mandatoryUpdates) {
            navigator.whenActivityActive { mainActivity ->
                mainActivity.showUpdateAppDialog(mandatoryUpdates, currentVersionData.link)
            }
        }
    }

    fun isTesting() = testing

}