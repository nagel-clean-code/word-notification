package com.nagel.wordnotification.data.firbase

import android.content.ContentValues
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
class RealtimeDbRepository @Inject constructor(
    private val navigator: MainNavigator
) {
    private val fireStore: FirebaseFirestore by lazy { Firebase.firestore }
    private val realtimeDatabase: DatabaseReference by lazy {
        FirebaseDatabase
            .getInstance("https://notifire-7d04d-default-rtdb.europe-west1.firebasedatabase.app/")
            .reference
    }
    private var testing = false
    private var _state = MutableStateFlow(DictionariesLibraryState())
    var state = _state.asStateFlow()

    private val observerForCurrentVersion = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            dataSnapshot.getValue(String::class.java)?.let {
                Log.d(ContentValues.TAG, "Текущея версия приложения: $it")
                if (it != BuildConfig.VERSION_NAME) {
                    showUpdateAppDialog()
                }
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w(
                ContentValues.TAG,
                "Отмена чтения текущей версии приложения",
                databaseError.toException()
            )
        }
    }

    init {
        requestGetDictionaries()
        realtimeDatabase
            .child("current_version")
            .addValueEventListener(observerForCurrentVersion)
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

    private fun showUpdateAppDialog() {
        fireStore.collection("current_viersion_app")
            .document("data")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                try {
                    val result = documentSnapshot.toObject<CurrentVersionData>()!!
                    navigator.whenActivityActive { mainActivity ->
                        mainActivity.showUpdateAppDialog(result.mandatory, result.link)
                    }
                } catch (e: Exception) {
                    Log.w(ContentValues.TAG, "Error getting documents: ", e)
                    return@addOnSuccessListener
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    fun isTesting() = testing

    internal data class CurrentVersionData(
        val link: String,
        val mandatory: Boolean
    ) {
        constructor() : this("", false)
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