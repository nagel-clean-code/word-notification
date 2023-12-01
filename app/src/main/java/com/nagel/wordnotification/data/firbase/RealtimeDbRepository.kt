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
import com.nagel.wordnotification.presentation.navigator.MainNavigator
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

    init{
        realtimeDatabase
            .child("current_version")
            .addValueEventListener(observerForCurrentVersion)
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

    internal data class CurrentVersionData(
        val link: String,
        val mandatory: Boolean
    ) {
        constructor() : this("", false)
    }
}