package com.nagel.wordnotification.presentation.addingwords

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.services.Utils
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.databinding.FragmentAddingWordsBinding
import com.nagel.wordnotification.presentation.addingwords.actions.EditWordDialog
import com.nagel.wordnotification.presentation.addingwords.actions.MenuSelectingActions
import com.nagel.wordnotification.presentation.addingwords.worddetails.WordDetailsDialog
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import com.nagel.wordnotification.presentation.navigator.navigator
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddingWordsBinding.inflate(inflater, container, false)
        binding.listWordsRecyclerView.addItemDecoration(
            ListWordsAdapter.VerticalSpaceItemDecoration(50)
        )
        initButtons()
        initListeners()
        return binding.root
    }

    private fun initListeners() {
        binding.selectDictionary.setOnClickListener {
            showChoosingDictionary()
        }

        binding.modeSettings.setOnClickListener {
            viewModel.loadedDictionaryFlow.value?.let {
                navigator().showModeSettingsFragment(it.idDictionary)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadedDictionaryFlow.collect() { loaded ->
                binding.progressBar.isVisible = loaded == null
                loaded?.let {
                    initAdapter()
                    binding.nameDictionary.text = loaded.name
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.start()
        navigator().startAlgorithm(2000)
    }

    private fun showChoosingDictionary() {
        navigator().showChoosingDictionaryFragment()
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
            WordDetailsDialog(word, dictionary.idMode).show(
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

    private fun initButtons() {
        binding.addWordButton.setOnClickListener {
            val textFirst = binding.editTextWord.text.toString().trim()
            val textLast = binding.editTextTranslation.text.toString().trim()
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
                    binding.listWordsRecyclerView.scrollToPosition(0)
                    binding.editTextTranslation.setText("")
                    binding.editTextWord.setText("")
                    binding.editTextWord.requestFocus()
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddingWordsFragment()
    }
}