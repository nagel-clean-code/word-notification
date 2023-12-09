package com.nagel.wordnotification.presentation.addingwords

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nagel.wordnotification.Constants.DICTIONARY_ID_KEY
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.services.Utils
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.databinding.FragmentAddingWordsBinding
import com.nagel.wordnotification.presentation.MainActivityVM
import com.nagel.wordnotification.presentation.addingwords.actions.EditWordDialog
import com.nagel.wordnotification.presentation.addingwords.actions.MenuSelectingActions
import com.nagel.wordnotification.presentation.addingwords.worddetails.WordDetailsDialog
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import com.nagel.wordnotification.presentation.navigator.navigator
import com.nagel.wordnotification.utils.SharedPrefsUtils
import com.nagel.wordnotification.utils.common.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class AddingWordsFragment : BaseFragment() {

    class Screen : BaseScreen

    private lateinit var binding: FragmentAddingWordsBinding
    private var listWordsAdapter: ListWordsAdapter? = null
    override val viewModel: AddingWordsVM by viewModels()
    private val viewModelActivity: MainActivityVM by activityViewModels()

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
            viewModel.loadedDictionaryFlow.value = false
            showChoosingDictionary()
        }

        binding.modeSettings.setOnClickListener {
            viewModel.loadedDictionaryFlow.value = false
            viewModel.dictionary?.let {
                navigator().showModeSettingsFragment(it.idDictionary)
            }
        }

        loadCurrentDictionary()

        lifecycleScope.launch {
            viewModel.loadedDictionaryFlow.collect() { loaded ->
                binding.progressBar.isVisible = !loaded
                if (loaded) {
                    handleLoadedDictionary()
                }
                binding.nameDictionary.text = viewModel.dictionary?.name
                    ?: requireContext().getString(R.string.dictionary_not_selected)
            }
        }
        lifecycleScope.launch {
            viewModel.showMessage.collect() { msg ->
                msg?.let { showMessage(it) }
                viewModel.showMessage.value = null
            }
        }
    }

    private suspend fun handleLoadedDictionary() {
        if (viewModel.dictionary == null) {
            val accountId = viewModelActivity.myAccountDbEntity.value?.id
            val dictionary = viewModel.getFirstDictionary(accountId)
            if (dictionary == null) {
                val msg = requireContext().getString(R.string.choose_dictionary)
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                showChoosingDictionary()
            } else {
                viewModel.dictionary = dictionary
                initAdapter()
            }
        } else {
            initAdapter()
        }
    }

    private fun showChoosingDictionary() {
        navigator().showChoosingDictionaryFragment(
            idAccount = viewModelActivity.myAccountDbEntity.value?.id ?: -1
        )
    }

    private fun loadCurrentDictionary() {
        val sessionIdDictionary =
            SharedPrefsUtils.getLongPreference(requireContext(), DICTIONARY_ID_KEY, -1)
        if (sessionIdDictionary != -1L) {
            viewModel.loadDictionaryById(sessionIdDictionary)
        } else {
            loadMyDictionary()
        }
    }

    private fun loadMyDictionary() {
        viewModelActivity.myAccountDbEntity.value?.let {
            viewModel.loadDictionaryByName(idAccount = it.id)
        } ?: kotlin.run {
            lifecycleScope.launch {
                viewModelActivity.myAccountDbEntity.collect {
                    it?.let {
                        viewModel.loadDictionaryByName(idAccount = it.id)
                    }
                }
            }
        }
    }

    /**
     * Для оптимизации
     */
    private fun initScroll() {
        binding.scrollView.setOnScrollChangeListener() { v, scrollX, scrollY, oldScrollX, oldScrollY ->
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
            ListWordsAdapter(viewModel.dictionary!!, ::showWordDetails, ::showMenuActionOnWord)
        binding.scrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.editTextWord.hideKeyboard()
            binding.editTextTranslation.hideKeyboard()
        }
        binding.listWordsRecyclerView.adapter = listWordsAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        binding.listWordsRecyclerView.layoutManager = layoutManager
    }

    private fun showWordDetails(word: Word) {
        WordDetailsDialog(word).show(childFragmentManager, WordDetailsDialog.TAG)
    }

    private fun showMenuActionOnWord(word: Word, position: Int) {
        MenuSelectingActions({
            chowEdit(word)
        }) {
            viewModel.deleteWord(word.idWord) {
                viewModel.dictionary?.wordList?.removeIf { it.idWord == word.idWord }
                listWordsAdapter?.notifyItemRemoved(position)
                Utils.deleteNotification(word)
            }
        }.show(childFragmentManager, null)
    }

    private fun chowEdit(word: Word) {
        EditWordDialog(word) {   //TODO поменять на flow
            listWordsAdapter?.notifyDataSetChanged()
            viewModel.repeatNotification(word)
        }.show(parentFragmentManager, EditWordDialog.TAG)
    }

    private fun showMessage(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }

    private fun initButtons() {
        binding.addWordButton.setOnClickListener {
            val textFirst = binding.editTextWord.text.toString().trim()
            val textLast = binding.editTextTranslation.text.toString().trim()
            if (textFirst.isBlank() || textLast.isBlank()) return@setOnClickListener
            val word = Word(
                idDictionary = viewModel.dictionary!!.idDictionary,
                textFirst = textFirst,
                textLast = textLast
            )
            lifecycleScope.launch(Dispatchers.IO) { //TODO перенести в VM
                val idWord = viewModel.dictionaryRepository.addWord(word)
                withContext(Dispatchers.Main) {
                    word.idWord = idWord
                    viewModel.dictionary?.wordList?.add(word)
                    listWordsAdapter?.notifyItemInserted(0)
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