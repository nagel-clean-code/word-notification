package com.nagel.wordnotification.presentation.addingwords

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.services.Utils
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.databinding.FragmentAddingWordsBinding
import com.nagel.wordnotification.presentation.addingwords.actions.EditWordDialog
import com.nagel.wordnotification.presentation.addingwords.actions.MenuSelectingActions
import com.nagel.wordnotification.presentation.addingwords.choicelanguage.ChoiceLanguageDialog
import com.nagel.wordnotification.presentation.addingwords.worddetails.WordDetailsDialog
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import com.nagel.wordnotification.presentation.navigator.navigator
import com.nagel.wordnotification.presentation.onboard.OnboardingActivity
import com.nagel.wordnotification.utils.RotationAnimator
import com.nagel.wordnotification.utils.common.hideKeyboard
import com.nagel.wordnotification.utils.common.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddingWordsFragment : BaseFragment() {

    class Screen : BaseScreen

    private lateinit var binding: FragmentAddingWordsBinding
    private var listWordsAdapter: ListWordsAdapter? = null
    override val viewModel: AddingWordsVM by viewModels()
    private lateinit var rotationAnimatorDoubleArrow: RotationAnimator
    private lateinit var rotationAnimatorReloadIcon: RotationAnimator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddingWordsBinding.inflate(inflater, container, false)
        binding.listWordsRecyclerView.addItemDecoration(
            ListWordsAdapter.VerticalSpaceItemDecoration(50)
        )
        binding.listWordsRecyclerView.itemAnimator = null
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        initButtons()
        rotationAnimatorDoubleArrow =
            RotationAnimator(180, viewLifecycleOwner.lifecycleScope, binding.imageView3)
        rotationAnimatorReloadIcon =
            RotationAnimator(0, viewLifecycleOwner.lifecycleScope, binding.swapIcon)
    }

    private fun initListeners() = with(binding) {
        imageView3.setOnClickListener {
            choiceLanguageWord.isVisible = choiceLanguageWord.isVisible.not()
            choiceLanguageTranslation.isVisible = choiceLanguageWord.isVisible.not()
            viewModel.changeCurrentAutoTranslate()
            rotationAnimatorDoubleArrow.rotationRight()
        }
        swapIcon.setOnClickListener {
            viewModel.swapWordsInCurrentDictionary()
            rotationAnimatorReloadIcon.rotationLeft()
        }
        selectDictionary.setOnClickListener {
            showChoosingDictionary()
        }

        modeSettings.setOnClickListener {
            viewModel.loadedDictionaryFlow.value?.let {
                navigator()?.showModeSettingsFragment(it.idDictionary)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadedDictionaryFlow.collect() { loaded ->
                    progressBar.isVisible = loaded == null
                    loaded?.let {
                        initAdapter()
                        nameDictionary.text = loaded.name
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch() {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.showTranslateLastWord.collect() {
                    it?.let { editTextTranslation.setText(it) }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch() {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.showTranslateFirstWord.collect() {
                    it?.let { editTextWord.setText(it) }
                }
            }
        }
        choiceLanguageTranslation.setOnClickListener {
            ChoiceLanguageDialog(viewModel.currentAutoTranslate) { lang, isAutoTranslation ->
                viewModel.setAutoTranslation(isAutoTranslation)
                lang?.let { viewModel.setTranslateLang(lang) }
                viewModel.requestTranslation(
                    editTextWord.text.toString(),
                    TranslationWord.LAST_WORD
                )
            }.show(childFragmentManager, ChoiceLanguageDialog.TAG)
        }
        choiceLanguageWord.setOnClickListener {
            ChoiceLanguageDialog(viewModel.currentAutoTranslate) { lang, isAutoTranslation ->
                viewModel.setAutoTranslation(isAutoTranslation)
                lang?.let { viewModel.setFirstWordTranslateLang(lang) }
                viewModel.requestTranslation(
                    editTextTranslation.text.toString(),
                    TranslationWord.FIRST_WORD
                )
            }.show(childFragmentManager, ChoiceLanguageDialog.TAG)
        }
        editTextWord.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.requestTranslation(s.toString(), TranslationWord.LAST_WORD)
            }
        })
        editTextTranslation.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.requestTranslation(s.toString(), TranslationWord.FIRST_WORD)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.start()
        showOnBoard()
    }

    private fun showOnBoard() {
        if (viewModel.getPermissionShowPreview()) {
            val intent = Intent(requireContext(), OnboardingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showChoosingDictionary() {
        navigator()?.showChoosingDictionaryFragment()
    }

    /**
     * Для оптимизации
     */
    private fun initScroll() {
        binding.scrollView.setOnScrollChangeListener() { _, _, scrollY, _, _ ->
            val view = binding.scrollView.getChildAt(binding.scrollView.childCount - 1) as View
            val diff: Int = view.bottom - (binding.scrollView.height + binding.scrollView.scrollY)
            if (diff == 0) {
                binding.listWordsRecyclerView.isNestedScrollingEnabled = true
            }
            if (scrollY == 0) {
                binding.listWordsRecyclerView.isNestedScrollingEnabled = false
            }
        }
    }

    private fun initAdapter() {
        listWordsAdapter =
            ListWordsAdapter(viewModel.loadWords(), ::showWordDetails, ::showMenuActionOnWord)
        binding.scrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.editTextWord.hideKeyboard()
            binding.editTextTranslation.hideKeyboard()
        }
        binding.listWordsRecyclerView.adapter = listWordsAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        binding.listWordsRecyclerView.layoutManager = layoutManager
    }

    private fun showWordDetails(word: Word) {
        viewModel.loadedDictionaryFlow.value?.let { dictionary ->
            WordDetailsDialog(word, dictionary.idMode, dictionary.include).show(
                childFragmentManager,
                WordDetailsDialog.TAG
            )
        } ?: kotlin.run {
            requireActivity().showToast(R.string.dictionary_could_not_be_loaded)
        }
    }

    private fun showMenuActionOnWord(word: Word) {
        MenuSelectingActions({
            chowEdit(word)
        }) {
            viewModel.deleteWord(word.idWord) {
                viewModel.loadedDictionaryFlow.value?.wordList?.removeIf { it.idWord == word.idWord }
                Utils.deleteNotification(word)
            }
        }.show(childFragmentManager, null)
    }

    private fun chowEdit(word: Word) {
        EditWordDialog(word) {
            viewModel.repeatNotification(word)
        }.show(parentFragmentManager, EditWordDialog.TAG)
    }

    private fun initButtons() = with(binding) {
        addWordButton.setOnClickListener {
            val textFirst = editTextWord.text.toString().trim()
            val textLast = editTextTranslation.text.toString().trim()
            if (textFirst.isBlank() || textLast.isBlank()) return@setOnClickListener
            val word = Word(
                idDictionary = viewModel.loadedDictionaryFlow.value!!.idDictionary,
                textFirst = textFirst,
                textLast = textLast
            )
            lifecycleScope.launch(Dispatchers.IO) { //TODO перенести в VM
                val idWord = viewModel.dictionaryRepository.addWord(word)
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    word.idWord = idWord
                    viewModel.loadedDictionaryFlow.value?.wordList?.add(word)
                    listWordsRecyclerView.scrollToPosition(0)
                    editTextTranslation.setText("")
                    editTextWord.setText("")
                    editTextWord.requestFocus()
                    viewModel.tryCreateNotification(word)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddingWordsFragment()
    }
}