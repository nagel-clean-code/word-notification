package com.nagel.wordnotification.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nagel.wordnotification.R
import com.nagel.wordnotification.databinding.FragmentProfileBinding
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import com.nagel.wordnotification.utils.GlobalFunction.openUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProfileFragment : BaseFragment() {
    class Screen : BaseScreen

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
        binding.apply {
            vkButton.setOnClickListener {
                requireContext().openUrl("https://vk.com/club223679470")  //Желательно ссылки брать с Firbase
            }
            okButton.setOnClickListener {
                requireContext().openUrl("https://ok.ru/group/70000004748309")
            }
            telegramButton.setOnClickListener {
                requireContext().openUrl("https://t.me/notifier2023")
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}