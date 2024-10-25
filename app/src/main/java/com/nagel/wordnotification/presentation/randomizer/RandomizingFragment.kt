package com.nagel.wordnotification.presentation.randomizer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nagel.wordnotification.R
import com.nagel.wordnotification.databinding.FragmentRandomizerBinding
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import com.nagel.wordnotification.utils.RotationAnimator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class RandomizingFragment : BaseFragment() {
    class Screen : BaseScreen

    private lateinit var binding: FragmentRandomizerBinding
    private lateinit var rotationAnimatorReloadIcon: RotationAnimator
    override val viewModel: RandomizingVM by viewModels()
    private var runable: Runnable? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRandomizerBinding.inflate(inflater, container, false)
        initListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rotationAnimatorReloadIcon =
            RotationAnimator(0, viewLifecycleOwner.lifecycleScope, binding.swapIcon)
    }

    private fun addRunnable(duration: Long, run: () -> Unit) {
        if (runable != null) return
        runable = Runnable {
            run.invoke()
            runable = null
        }
        binding.root.postDelayed(runable, duration)
    }

    private fun onClickAnswer() = with(binding) {
        if (translation.text == EMPTY_WORD) {
            translation.text = if (translation.tag as? Boolean != true) {
                viewModel.currentWord.value?.textLast ?: ""
            } else {
                viewModel.currentWord.value?.textFirst ?: ""
            }
            root.postDelayed({
                viewModel.nextWord()
            }, DURATION_SHOW_ANSWER)
        } else {
            viewModel.nextWord()
        }
    }

    private fun initListeners() = with(binding) {
        swapIcon.setOnClickListener {
            if (translation.tag as? Boolean != true) {
                translation.tag = true
                word.text = viewModel.currentWord.value?.textLast ?: ""
            } else {
                translation.tag = false
                word.text = viewModel.currentWord.value?.textFirst ?: ""
            }
            translation.text = EMPTY_WORD
            rotationAnimatorReloadIcon.rotationLeft()
        }
        head.setOnClickListener {
            if (head.tag as? Boolean == true) {
                head.tag = false
                updateHead(viewModel.currentDictionary.value)
            } else {
                updateHead("❄❄❄❄❄❄❄❄")
                head.tag = true
            }
        }
        with(notRememberButton) {
            setOnClickListener {
                if (runable == null) {
                    setBackgroundResource(R.drawable.background_selected_not_remember)
                    viewModel.notRemember()
                    onClickAnswer()
                    addRunnable(DURATION_PRESSING_BUTTON) {
                        setBackgroundResource(R.drawable.background_open_book_view)
                    }
                }
            }
        }

        with(rememberButton) {
            setOnClickListener {
                if (runable == null) {
                    setBackgroundResource(R.drawable.background_selected_remember)
                    viewModel.remember()
                    onClickAnswer()
                    addRunnable(DURATION_PRESSING_BUTTON) {
                        setBackgroundResource(R.drawable.background_open_book_view)
                    }
                }
            }
        }

        with(bookButton) {
            setOnClickListener {
                setBackgroundResource(R.drawable.background_selected_book_view)
                openBookButtonClick()
                addRunnable(DURATION_PRESSING_BUTTON) {
                    setBackgroundResource(R.drawable.background_open_book_view)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentDictionary.collect() {
                    updateHead(it)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentWord.collect() {
                    word.text = if (translation.tag as? Boolean != true) {
                        it?.textFirst
                    } else {
                        it?.textLast
                    }
                    translation.text = EMPTY_WORD
                    showDataCounter()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadingDictionaries.collect() {
                it?.let {
                    initDictionaries()
                    progressBar.isVisible = false
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.showResult.collect() {
                    it?.let {
                        ResultRandomizingFragmentDialog(
                            it.first,
                            it.second
                        ).show(parentFragmentManager, null)
                        translation.text = EMPTY_WORD
                        showDataCounter()
                    }
                }
            }
        }
    }

    private fun updateHead(dictionaryName: String?) = with(binding) {
        if (head.tag as? Boolean == true) return@with
        if (dictionaryName?.isBlank() == true) {
            val msg = requireContext().getString(R.string.dictionary_not_selected)
            head.text = msg
        } else {
            head.text = dictionaryName
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
                if (tag as? Boolean != true) {
                    viewModel.currentWord.value?.textLast ?: ""
                } else {
                    viewModel.currentWord.value?.textFirst ?: ""
                }
            } else {
                EMPTY_WORD
            }
        }
    }

    private fun initDictionaries() = with(binding) {
        viewModel.selectedDictionarySet.forEach {
            val view = createTextView(it)
            chainDictionaries.addView(view)
            flow.addView(view)
        }
    }

    private fun createTextView(text: String): TextView {
        val lParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val textView = TextView(requireContext())
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
        textView.setTextColor(resources.getColor(R.color.gray_3))
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