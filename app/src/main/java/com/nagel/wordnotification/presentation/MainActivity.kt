package com.nagel.wordnotification.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.analytecs.Analytic
import com.nagel.wordnotification.core.services.AlgorithmAdjustmentWork
import com.nagel.wordnotification.data.firbase.RealtimeDbRepository
import com.nagel.wordnotification.databinding.ActivityMainBinding
import com.nagel.wordnotification.presentation.addingwords.AddingWordsFragment
import com.nagel.wordnotification.presentation.choosingdictionary.ChoosingDictionaryFragment
import com.nagel.wordnotification.presentation.navigator.MainNavigator
import com.nagel.wordnotification.presentation.navigator.Navigator
import com.nagel.wordnotification.presentation.profile.ProfileFragment
import com.nagel.wordnotification.presentation.randomizer.RandomizingFragment
import com.nagel.wordnotification.presentation.settings.ModeSettingsFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), Navigator {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var navigator: MainNavigator

    @Inject
    lateinit var realtimeDb: RealtimeDbRepository

    private val viewModel: MainActivityVM by viewModels()
    private lateinit var commonFirebaseAnalytics: FirebaseAnalytics
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initFirebase()
        binding.bottomNavigationView.setOnItemSelectedListener {
            val screen = when (it.itemId) {
                R.id.add_in_dictionaries -> AddingWordsFragment.Screen()

                R.id.dictionaries -> ChoosingDictionaryFragment.Screen(
                    viewModel.myAccountDbEntity.value?.id ?: -1
                )

                R.id.randomizing -> RandomizingFragment.Screen()
                R.id.profile -> ProfileFragment.Screen()
                else -> AddingWordsFragment.Screen()
            }
            navigator.launchFragment(this, screen, false)

            getTurnTrue()
        }
        settingKeyboard()
        viewModel.startSession()
        checkPermissions()
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
    }

    private fun startAlgorithm() {
        val workManager = WorkManager.getInstance(this)
        val info = workManager.getWorkInfosByTag("AlgorithmWork")
        if (info.get().isEmpty() || info.isCancelled) {
            //TODO отменять все созданные алерты если они были (в бд поменять флаг lesson)
            val logs = mapOf(
                "Work" to "start",
                "info.get()" to info.get()
            )
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
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    private fun launchFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun showAddingWordsFragment() {
        binding.bottomNavigationView.selectedItemId = R.id.add_in_dictionaries
    }

    override fun showRandomizingFragment() {
        navigator.launchFragment(this, RandomizingFragment.Screen())
    }

    override fun showModeSettingsFragment(idDictionary: Long) {
        navigator.launchFragment(this, ModeSettingsFragment.Screen(idDictionary))
    }

    override fun showChoosingDictionaryFragment(idAccount: Long) {
        navigator.launchFragment(this, ChoosingDictionaryFragment.Screen(idAccount))
    }

    override fun showProfileFragment() {
        navigator.launchFragment(this, ProfileFragment.Screen())
    }

    private fun getTurnTrue(): Boolean {
        return true
    }

    override fun goBack() {
        if (supportFragmentManager.backStackEntryCount != 0) {
            onBackPressed()
        }
    }

    /** Скрытие поднятого меню над клавиатурой при вводе текста */
    private fun settingKeyboard() {
        binding.bottomNavigationView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            binding.root.getWindowVisibleDisplayFrame(r)
            if (binding.root.rootView.height - (r.bottom - r.top) > 500) {
                binding.bottomNavigationView.visibility = View.GONE
            } else {
                binding.bottomNavigationView.visibility = View.VISIBLE
            }
        }
    }

    fun showUpdateAppDialog(mandatory: Boolean, link: String) {
        val prevDialog = supportFragmentManager.findFragmentByTag(PopupUpdateAppDialog.TAG)
        (prevDialog as? DialogFragment)?.dismiss()

        PopupUpdateAppDialog(mandatory, link)
            .show(supportFragmentManager, PopupUpdateAppDialog.TAG)
    }

    companion object {
        const val TAG_WORK = "AlgorithmWork"
        const val WORK_REPEAT_INTERVAL = 15L
    }
}