package com.nagel.wordnotification.presentation.navigator

import androidx.fragment.app.Fragment

fun Fragment.navigator(): Navigator {
    return requireActivity() as Navigator
}

interface Navigator {
    fun showAddingWordsFragment()
    fun showRandomizingFragment()
    fun showModeSettingsFragment(idDictionary: Long)
    fun showChoosingDictionaryFragment(idAccount: Long)
    fun showProfileFragment()
    fun clearBackStack()
    fun blackoutBottomNavigationView(show: Boolean)
    fun goBack()
}