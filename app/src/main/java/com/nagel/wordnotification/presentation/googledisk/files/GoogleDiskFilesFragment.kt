package com.nagel.wordnotification.presentation.googledisk.files

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.nagel.wordnotification.databinding.FragmentGoogleDiskFilesBinding
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class GoogleDiskFilesFragment : BaseFragment() {
    class Screen : BaseScreen

    private lateinit var binding: FragmentGoogleDiskFilesBinding
    override val viewModel: GoogleDiskVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGoogleDiskFilesBinding.inflate(inflater, container, false)
        initListeners()
        return binding.root
    }

    private fun initListeners() {

    }

    companion object {
        @JvmStatic
        fun newInstance() = GoogleDiskFilesFragment()
    }
}