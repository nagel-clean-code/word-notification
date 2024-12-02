package com.nagel.wordnotification.presentation

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.huawei.hms.push.HmsMessaging
import com.nagel.wordnotification.Constants.BOOT_COMPLETED
import com.nagel.wordnotification.Constants.HTC_QUICKBOOT_POWERON
import com.nagel.wordnotification.Constants.QUICKBOOT_POWERON
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.algorithms.NotificationAlgorithm
import com.nagel.wordnotification.core.analytecs.AppMetricaAnalyticPlatform
import com.nagel.wordnotification.core.services.NotificationRestorerReceiver
import com.nagel.wordnotification.data.firbase.RemoteDbRepository
import com.nagel.wordnotification.databinding.ActivityMainBinding
import com.nagel.wordnotification.presentation.addingwords.AddingWordsFragment
import com.nagel.wordnotification.presentation.choosingdictionary.ChoosingDictionaryFragment
import com.nagel.wordnotification.presentation.choosingdictionary.library.LibraryDictionariesFragment
import com.nagel.wordnotification.presentation.exportdictionaries.ExportFragment
import com.nagel.wordnotification.presentation.navigator.MainNavigator
import com.nagel.wordnotification.presentation.navigator.Navigator
import com.nagel.wordnotification.presentation.profile.ProfileFragment
import com.nagel.wordnotification.presentation.profile.evalution.EvaluationAppDialog
import com.nagel.wordnotification.presentation.randomizer.RandomizingFragment
import com.nagel.wordnotification.presentation.reader.ImportInDb
import com.nagel.wordnotification.presentation.settings.ModeSettingsFragment
import com.nagel.wordnotification.utils.common.MessageUtils
import dagger.hilt.android.AndroidEntryPoint
import io.appmetrica.analytics.AppMetrica
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
    lateinit var realtimeDb: RemoteDbRepository

    @Inject
    lateinit var fileReader: ImportInDb

    @Inject
    lateinit var appMetrica: AppMetricaAnalyticPlatform

    private val viewModel: MainActivityVM by viewModels()
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_WordNotification)
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            AppMetrica.reportAppOpen(this)
        }
        navigatorInstance = navigator
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (areGoogleServicesAvailable()) {
            initFirebase()
        } else {
            setAutoInitHmsPushEnabled()
        }

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        AppMetrica.reportAppOpen(intent)
    }

    private fun areGoogleServicesAvailable(): Boolean {
        val availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        val isGms = availability == com.google.android.gms.common.ConnectionResult.SUCCESS
        appMetrica.changeIsGmsBuild(isGms)
        return isGms
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
        auth = Firebase.auth
        auth?.signInAnonymously()?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                task.result.user?.zzb()?.metadata?.creationTimestamp?.let {
                    val date = NotificationAlgorithm.dateFormat.format(it)
                    Log.d("DATA::: Дата регистрации:", date + " $it")
                }
            } else {
                Log.d("Ошибка", "входа в fb")
            }
        }
    }

    private fun setAutoInitHmsPushEnabled() {
        HmsMessaging.getInstance(this).isAutoInitEnabled = true
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

    override fun showExportDictionariesFragment() {
        navigator.launchFragment(this, ExportFragment.Screen())
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