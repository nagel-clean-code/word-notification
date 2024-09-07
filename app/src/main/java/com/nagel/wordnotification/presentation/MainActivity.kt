package com.nagel.wordnotification.presentation

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.nagel.wordnotification.R
import com.nagel.wordnotification.app.App
import com.nagel.wordnotification.core.analytecs.Analytic
import com.nagel.wordnotification.core.services.AlgorithmAdjustmentWork
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), Navigator {

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
    }

    private fun initFirebase() {
        commonFirebaseAnalytics = Firebase.analytics
        auth = com.google.firebase.ktx.Firebase.auth
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Analytic.logEvent(
                        FirebaseAnalytics.Event.LOGIN,
                        FirebaseAnalytics.Param.METHOD,
                        "successful"
                    )
//                    val user = auth.currentUser
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
        startAlgorithm()
    }

    override fun onResume() {
        super.onResume()
        navigator.whenActivityActive.mainActivity = this
//        rateApp()
    }

    private fun rateApp() {
        if (viewModel.isItPossibleShowRateApp()) {
            EvaluationAppDialog().show(supportFragmentManager, EvaluationAppDialog.TAG)
        }
    }

    override fun startAlgorithm(postDelay: Long?) {
        postDelay?.let {
            binding.root.postDelayed({
                startAlgorithm()
            }, it)
            return
        }
        val workManager = WorkManager.getInstance(App.get())
        val info = workManager.getWorkInfosByTag("AlgorithmWork")
        val logs = mapOf("Work" to "start", "info.get()" to info.get())
        Analytic.logEvent("CoroutineWorker", logs, false)
        val worker = PeriodicWorkRequestBuilder<AlgorithmAdjustmentWork>(
            WORK_REPEAT_INTERVAL,
            TimeUnit.MINUTES
        ).addTag(TAG_WORK).build()
        workManager.enqueueUniquePeriodicWork(
            TAG_WORK,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            worker
        )
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

    override fun blackoutBottomNavigationView(show: Boolean) {
        binding.blackout.visibility = if (show) View.VISIBLE else View.INVISIBLE
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
        const val TAG_WORK = "AlgorithmWork"
        const val WORK_REPEAT_INTERVAL = 15L
    }
}