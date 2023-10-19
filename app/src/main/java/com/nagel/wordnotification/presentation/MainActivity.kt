package com.nagel.wordnotification.presentation

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nagel.wordnotification.R
import com.nagel.wordnotification.databinding.ActivityMainBinding
import com.nagel.wordnotification.presentation.addingwords.AddingWordsFragment
import com.nagel.wordnotification.presentation.choosingdictionary.ChoosingDictionaryFragment
import com.nagel.wordnotification.presentation.choosingdictionary.settings.ModeSettingsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), Navigator {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        if (savedInstanceState == null)
//            replaceFragment(AddingWordsFragment())

        binding.bottomNavigationView.setOnItemSelectedListener {
            val bufFragment = when (it.itemId) {
                R.id.add_in_dictionaries -> AddingWordsFragment()
                R.id.dictionaries -> ChoosingDictionaryFragment.newInstance(
                    viewModel.myAccountDbEntity.value?.id ?: -1
                )
//                R.id.randomizing -> AddRecipeFragment()
//                R.id.profile -> FindRecipeFragment()
                else -> AddingWordsFragment()
            }
            replaceFragment(bufFragment)
            getTurnTrue()
        }
        settingKeyboard()
        viewModel.startSession()
//        startServices()
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

    override fun showModeSettingsFragment() {
        launchFragment(ModeSettingsFragment.newInstance())
    }

    override fun showChoosingDictionaryFragment(idAccount: Long) {
        launchFragment(ChoosingDictionaryFragment.newInstance(idAccount))
    }

    private fun getTurnTrue(): Boolean {
        return true
    }

    override fun goBack() {
        if (supportFragmentManager.backStackEntryCount != 1) {
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
}