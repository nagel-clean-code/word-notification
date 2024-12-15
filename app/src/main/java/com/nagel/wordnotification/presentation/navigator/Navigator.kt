package com.nagel.wordnotification.presentation.navigator

import androidx.fragment.app.Fragment

fun Fragment.navigator(): Navigator? {
    return requireActivity() as? Navigator
}

interface Navigator {
    fun showGoogleDiskFilesFragment()
    fun showAddingWordsFragment()
    fun showRandomizingFragment()
    fun showLibraryDictionariesFragment()
    fun showExportDictionariesFragment()
    fun showModeSettingsFragment(idDictionary: Long)
    fun showChoosingDictionaryFragment()
    fun showProfileFragment()
    fun clearBackStack()
    fun goBack()
}