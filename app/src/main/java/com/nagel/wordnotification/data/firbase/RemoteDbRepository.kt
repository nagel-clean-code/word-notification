package com.nagel.wordnotification.data.firbase

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.nagel.wordnotification.BuildConfig
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.presentation.navigator.MainNavigator
import com.nagel.wordnotification.presentation.reader.ImportInCash
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDbRepository @Inject constructor(
    private val navigator: MainNavigator
) {
    private val fireStore: FirebaseFirestore by lazy { Firebase.firestore }

    //    private var testing = Date().time < 1704300235000
    private var testing = false
    private var _state = MutableStateFlow(DictionariesLibraryState())
    var state = _state.asStateFlow()

    init {
        requestGetDictionaries()
        requestActualVersionApp()
    }

    fun requestGetDictionaries() {
        _state.value = _state.value.copy(isLoading = true)
        fireStore.collection("dictionaries")
            .document("all")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                try {
                    val result = documentSnapshot.toObject<DictionariesLibrary>()!!
                    _state.value = _state.value.copy(
                        isLoading = false,
                        dictionariesList = result
                    )
                } catch (e: Exception) {
                    Log.w(ContentValues.TAG, "Error getting documents: ", e)
                    _state.value = _state.value.copy(isError = true)
                    return@addOnSuccessListener
                }
            }.addOnFailureListener { exception ->
                _state.value = _state.value.copy(isError = true)
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

    internal data class CurrentVersionData(
        val link: String,
        val mandatory: Boolean,
        val mandatoryUpdates: List<Int>,
        val noUpdateNeeded: List<Int>,
        val optionalUpdates: List<Int>
    ) {
        constructor() : this("", false, emptyList(), emptyList(), emptyList())
    }

    data class DictionariesLibrary(
        val contents: String
    ) {
        constructor() : this("")

        suspend fun getDictionaries(dataReader: ImportInCash): List<Dictionary> {
            dataReader.readStringAndCreateDictionaries(contents)
            return dataReader.dictionaryRepository.getDictionaries()
        }
    }

    data class DictionariesLibraryState(
        var isLoading: Boolean = true,
        var dictionariesList: DictionariesLibrary? = null,
        val isError: Boolean = false
    )
}