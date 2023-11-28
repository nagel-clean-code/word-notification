package com.nagel.wordnotification.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.nagel.wordnotification.R
import com.nagel.wordnotification.databinding.FragmentProfileBinding
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProfileFragment : BaseFragment() {
    class Screen: BaseScreen

    private lateinit var binding: FragmentProfileBinding
    override val viewModel: ProfileVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        initListeners()
        return binding.root
    }

    private fun initListeners() {
        lifecycleScope.launch {
            viewModel.showData.collect() {
                if (it != null) {
                    val percent = if (viewModel.numbersWords != 0) {
                        viewModel.learnedWords * 100 / viewModel.numbersWords
                    } else {
                        0
                    }
                    val text = requireContext().getString(
                        R.string.text_statistic,
                        viewModel.learnedWords,
                        viewModel.numbersWords,
                        percent,
                        viewModel.learnedDictionaries,
                        viewModel.countDictionaries
                    )
                    binding.textView1.text = text
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}