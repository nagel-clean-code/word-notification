package com.nagel.wordnotification.presentation

import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.nagel.wordnotification.Constants.BOOT_COMPLETED
import com.nagel.wordnotification.Constants.HTC_QUICKBOOT_POWERON
import com.nagel.wordnotification.Constants.QUICKBOOT_POWERON
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.algorithms.NotificationAlgorithm
import com.nagel.wordnotification.core.analytecs.Analytic
import com.nagel.wordnotification.core.services.NotificationRestorerReceiver
import com.nagel.wordnotification.data.firbase.RealtimeDbRepository
import com.nagel.wordnotification.databinding.ActivityMainBinding
import com.nagel.wordnotification.presentation.addingwords.AddingWordsFragment
import com.nagel.wordnotification.presentation.choosingdictionary.ChoosingDictionaryFragment
import com.nagel.wordnotification.presentation.choosingdictionary.library.LibraryDictionariesFragment
import com.nagel.wordnotification.presentation.navigator.MainNavigator
import com.nagel.wordnotification.presentation.navigator.Navigator
import com.nagel.wordnotification.presentation.profile.ProfileFragment
import com.nagel.wordnotification.presentation.profile.evalution.EvaluationAppDialog
import com.nagel.wordnotification.presentation.randomizer.RandomizingFragment
import com.nagel.wordnotification.presentation.reader.ImportInDb
import com.nagel.wordnotification.presentation.settings.ModeSettingsFragment
import com.nagel.wordnotification.utils.common.MessageUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), Navigator {

    private val broadcastReceiver: NotificationRestorerReceiver = NotificationRestorerReceiver()

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var navigator: MainNavigator

    @Inject
    lateinit var realtimeDb: RealtimeDbRepository

    @Inject
    lateinit var fileReader: ImportInDb

    private val viewModel: MainActivityVM by viewModels()
    private lateinit var commonFirebaseAnalytics: FirebaseAnalytics
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_WordNotification)
        super.onCreate(savedInstanceState)
        navigatorInstance = navigator
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initFirebase()
        binding.bottomNavigationView.setOnItemSelectedListener {
            val screen = when (it.itemId) {
                R.id.add_in_dictionaries -> AddingWordsFragment.Screen()

                R.id.dictionaries -> ChoosingDictionaryFragment.Screen()

                R.id.randomizing -> RandomizingFragment.Screen()
                R.id.profile -> ProfileFragment.Screen()
                else -> AddingWordsFragment.Screen()
            }
            clearBackStack()
            navigator.launchFragment(this, screen, false)
            true
        }
        lifecycleScope.launch(Dispatchers.IO) {
            fileReader.handleIntent(intent.data) { msgId ->
                withContext(Dispatchers.Main) {
                    MessageUtils.showToast(msgId, this@MainActivity)
                }
            }
        }
        initReceiver()
        viewModel.startNotification()
    }

    private fun initReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BOOT_COMPLETED)
        intentFilter.addAction(QUICKBOOT_POWERON)
        intentFilter.addAction(HTC_QUICKBOOT_POWERON)
        ContextCompat.registerReceiver(
            this,
            broadcastReceiver,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun initFirebase() {
        commonFirebaseAnalytics = Firebase.analytics
        auth = com.google.firebase.ktx.Firebase.auth
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    task.result.user?.zzb()?.metadata?.creationTimestamp?.let {
                        val date = NotificationAlgorithm.dateFormat.format(it)
                        Log.d("DATA::: Дата регистрации:", date + " $it")
                    }
                    Analytic.logEvent(
                        FirebaseAnalytics.Event.LOGIN,
                        FirebaseAnalytics.Param.METHOD,
                        "successful"
                    )
                } else {
                    Analytic.logEvent(
                        FirebaseAnalytics.Event.LOGIN,
                        FirebaseAnalytics.Param.METHOD,
                        "fails"
                    )
                }
            }
    }

    override fun onPause() {
        super.onPause()
        navigator.whenActivityActive.mainActivity = null
    }

    override fun onResume() {
        super.onResume()
        navigator.whenActivityActive.mainActivity = this
//        rateApp()
    }

    //TODO Вернуть когда выложу на плеймаркет
    private fun rateApp() {
        if (viewModel.isItPossibleShowRateApp()) {
            EvaluationAppDialog().show(supportFragmentManager, EvaluationAppDialog.TAG)
        }
    }

    override fun showAddingWordsFragment() {
        binding.bottomNavigationView.selectedItemId = R.id.add_in_dictionaries
    }

    override fun showRandomizingFragment() {
        navigator.launchFragment(this, RandomizingFragment.Screen())
    }

    override fun showLibraryDictionariesFragment() {
        navigator.launchFragment(this, LibraryDictionariesFragment.Screen())
    }

    override fun showModeSettingsFragment(idDictionary: Long) {
        navigator.launchFragment(this, ModeSettingsFragment.Screen(idDictionary))
    }

    override fun showChoosingDictionaryFragment() {
        navigator.launchFragment(this, ChoosingDictionaryFragment.Screen())
    }

    override fun showProfileFragment() {
        navigator.launchFragment(this, ProfileFragment.Screen())
    }

    override fun clearBackStack() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun goBack() {
        if (supportFragmentManager.backStackEntryCount != 0) {
            onBackPressed()
        }
    }

    fun showUpdateAppDialog(mandatory: Boolean, link: String) {
        val prevDialog = supportFragmentManager.findFragmentByTag(PopupUpdateAppDialog.TAG)
        (prevDialog as? DialogFragment)?.dismiss()

        PopupUpdateAppDialog(mandatory, link)
            .show(supportFragmentManager, PopupUpdateAppDialog.TAG)
    }

    companion object {
        var navigatorInstance: MainNavigator? = null
    }
}