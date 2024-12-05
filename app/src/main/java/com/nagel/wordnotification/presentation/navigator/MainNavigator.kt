package com.nagel.wordnotification.presentation.navigator

import android.app.Application
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nagel.wordnotification.R
import com.nagel.wordnotification.app.App
import com.nagel.wordnotification.presentation.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainNavigator @Inject constructor() : AndroidViewModel(App.get()), NavigatorV2 {

    override val whenActivityActive = MainActivityActions()
    private val _result = MutableLiveData<Any>()
    val result: LiveData<Any> = _result

    override fun launch(screen: BaseScreen) = whenActivityActive {
        launchFragment(it, screen)
    }

    override fun goBack(result: Any?) = whenActivityActive {
        result?.let { _result.value = it }
        it.onBackPressed()
    }

    override fun toast(messageId: Int) {
        whenActivityActive.mainActivity?.runOnUiThread() {
            Toast.makeText(getApplication(), messageId, Toast.LENGTH_LONG).show()
        }
    }

    override fun getString(messageRes: Int): String {
        return getApplication<Application>().getString(messageRes)
    }

    override fun onCleared() {
        super.onCleared()
        whenActivityActive.clear()
    }

    fun launchFragment(activity: MainActivity, screen: BaseScreen, addToBackStack: Boolean = true) {
        val fragment = screen.javaClass.enclosingClass.newInstance() as Fragment
        fragment.arguments = bundleOf(ARG_SCREEN to screen)
        val transaction = activity.supportFragmentManager.beginTransaction()
        if (addToBackStack) transaction.addToBackStack(null)
        transaction
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    companion object {
        const val ARG_SCREEN = "SCREEN"
    }
}