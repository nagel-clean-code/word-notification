package com.nagel.wordnotification.presentation.randomizer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.nagel.wordnotification.R
import com.nagel.wordnotification.databinding.FragmentRandomizerBinding
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class RandomizingFragment : BaseFragment() {
    class Screen : BaseScreen

    private lateinit var binding: FragmentRandomizerBinding
    override val viewModel: RandomizingVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRandomizerBinding.inflate(inflater, container, false)
        initListeners()
        return binding.root
    }

    private fun onClickAnswer() {
        if (binding.translation.text == EMPTY_WORD) {
            binding.translation.text = viewModel.currentWord.value?.textLast ?: ""
            binding.root.postDelayed({
                viewModel.nextWord()
            }, DURATION_SHOW_ANSWER)
        } else {
            viewModel.nextWord()
        }
    }

    private fun initListeners() {
        with(binding.notRememberButton) {
            setOnClickListener {
                setBackgroundResource(R.drawable.background_selected_not_remember)
                postDelayed({
                    setBackgroundResource(R.drawable.background_open_book_view)
                }, DURATION_PRESSING_BUTTON)
                viewModel.notRemember()
                onClickAnswer()
            }
        }

        with(binding.rememberButton) {
            setOnClickListener {
                setBackgroundResource(R.drawable.background_selected_remember)
                postDelayed({
                    setBackgroundResource(R.drawable.background_open_book_view)
                }, DURATION_PRESSING_BUTTON)
                viewModel.remember()
                onClickAnswer()
            }
        }

        with(binding.bookButton) {
            setOnClickListener {
                setBackgroundResource(R.drawable.background_selected_book_view)
                openBookButtonClick()
                postDelayed({
                    setBackgroundResource(R.drawable.background_open_book_view)
                }, DURATION_PRESSING_BUTTON)
            }
        }
        lifecycleScope.launch {
            viewModel.currentDictionary.collect() {
                if (it?.isBlank() == true) {
                    val msg = requireContext().getString(R.string.dictionary_not_selected)
                    binding.head.text = msg
                } else {
                    binding.head.text = it
                }
            }
        }
        lifecycleScope.launch {
            viewModel.currentWord.collect() {
                binding.word.text = it?.textFirst
                binding.translation.text = EMPTY_WORD
                showDataCounter()
            }
        }
        lifecycleScope.launch {
            viewModel.loadingDictionaries.collect() {
                it?.let {
                    initDictionaries()
                    binding.progressBar.isVisible = false
                }
            }
        }
        lifecycleScope.launch {
            viewModel.showResult.collect() {
                it?.let {
                    ResultRandomizingFragmentDialog(
                        it.first,
                        it.second
                    ).show(parentFragmentManager, null)
                    binding.translation.text = EMPTY_WORD
                    showDataCounter()
                }
            }
        }
    }

    private fun showDataCounter() {
        binding.notRememberTextView.text = "${viewModel.countNotRemember}"
        binding.counterWords.text =
            "${viewModel.countNotRemember + viewModel.countRemember}/${viewModel.listWord.size}"
        binding.rememberTextView.text = "${viewModel.countRemember}"
    }

    private fun openBookButtonClick() {
        with(binding.translation) {
            text = if (text.toString() == EMPTY_WORD) {
                viewModel.currentWord.value?.textLast ?: ""
            } else {
                EMPTY_WORD
            }
        }
    }

    private fun initDictionaries() {
        viewModel.selectedDictionarySet.forEach {
            val view = createTextView(it)
            binding.chainDictionaries.addView(view)
            binding.flow.addView(view)
        }
    }

    private fun createTextView(text: String): TextView {
        val lParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val textView = TextView(context)
        textView.id = View.generateViewId()
        textView.setTextColor(resources.getColor(R.color.light_beige))
        textView.setBackgroundResource(R.drawable.selected_day_week)
        textView.textSize = 16f
        textView.setPadding(30, 5, 30, 5)
        textView.typeface = ResourcesCompat.getFont(requireContext(), R.font.montserrat_bold)
        textView.layoutParams = lParams
        textView.text = text
        initClickListenerItem(textView)
        return textView
    }

    private fun initClickListenerItem(textView: TextView) {
        textView.setOnClickListener {
            if (textView.tag != true) {
                unselectedDictionary(textView)
            } else {
                selectedDictionary(textView)
            }
        }
    }

    private fun unselectedDictionary(textView: TextView) {
        viewModel.unselectedDictionary(textView.text.toString())
        textView.setBackgroundResource(R.drawable.unselected_day_week)
        textView.setTextColor(resources.getColor(R.color.black))
        textView.tag = true
    }

    private fun selectedDictionary(textView: TextView) {
        viewModel.selectedDictionary(textView.text.toString())
        textView.setTextColor(resources.getColor(R.color.light_beige))
        textView.setBackgroundResource(R.drawable.selected_day_week)
        textView.tag = false
    }

    companion object {
        @JvmStatic
        fun newInstance() = RandomizingFragment()

        private const val DURATION_PRESSING_BUTTON = 200L
        private const val DURATION_SHOW_ANSWER = 800L
        const val EMPTY_WORD = "?"
    }
}