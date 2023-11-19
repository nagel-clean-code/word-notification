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
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.services.AlgorithmAdjustmentWork
import com.nagel.wordnotification.databinding.ActivityMainBinding
import com.nagel.wordnotification.presentation.addingwords.AddingWordsFragment
import com.nagel.wordnotification.presentation.choosingdictionary.ChoosingDictionaryFragment
import com.nagel.wordnotification.presentation.profile.ProfileFragment
import com.nagel.wordnotification.presentation.randomizer.RandomizingFragment
import com.nagel.wordnotification.presentation.settings.ModeSettingsFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), Navigator {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.setOnItemSelectedListener {
            val bufFragment = when (it.itemId) {
                R.id.add_in_dictionaries -> AddingWordsFragment()
                R.id.dictionaries -> ChoosingDictionaryFragment.newInstance(
                    viewModel.myAccountDbEntity.value?.id ?: -1
                )

                R.id.randomizing -> RandomizingFragment()
                R.id.profile -> ProfileFragment()
                else -> AddingWordsFragment()
            }
            replaceFragment(bufFragment)
            getTurnTrue()
        }
        settingKeyboard()
        viewModel.startSession()
        checkPermissions()
    }

    override fun onPause() {
        super.onPause()
        startAlgorithm()
    }

    private fun startAlgorithm() {
        val workManager = WorkManager.getInstance(this)
        val info = workManager.getWorkInfosByTag("AlgorithmWork")
        if (info.get().isEmpty() || info.isCancelled) {
            //Добавить аналитику аналитику
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

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
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
        launchFragment(RandomizingFragment.newInstance())
    }

    override fun showModeSettingsFragment(idDictionary: Long) {
        launchFragment(ModeSettingsFragment.newInstance(idDictionary))
    }

    override fun showChoosingDictionaryFragment(idAccount: Long) {
        launchFragment(ChoosingDictionaryFragment.newInstance(idAccount))
    }

    override fun showProfileFragment() {
        launchFragment(ProfileFragment.newInstance())
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

    companion object {
        const val TAG_WORK = "AlgorithmWork"
        const val WORK_REPEAT_INTERVAL = 15L
    }
}