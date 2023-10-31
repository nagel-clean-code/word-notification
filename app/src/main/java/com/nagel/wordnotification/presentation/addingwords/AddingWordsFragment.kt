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
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.databinding.FragmentAddingWordsBinding
import com.nagel.wordnotification.presentation.MainActivityVM
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator
import com.nagel.wordnotification.utils.SharedPrefsUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AddingWordsFragment : BaseFragment() {

    private lateinit var binding: FragmentAddingWordsBinding
    private var listWordsAdapter: ListWordsAdapter? = null
    override val viewModel: AddingWordsVM by viewModels()
    private val viewModelActivity: MainActivityVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddingWordsBinding.inflate(inflater, container, false)
//        if(listWordsAdapter == null) {
        initButtons()
        initListeners()
//        }
        return binding.root
    }

    private fun initListeners() {
//        initScroll()

        binding.selectDictionary.setOnClickListener {
            viewModel.loadedDictionaryFlow.value = false
            navigator().showChoosingDictionaryFragment(
                idAccount = viewModelActivity.myAccountDbEntity.value?.id ?: -1
            )
        }

        binding.modeSettings.setOnClickListener {
            viewModel.loadedDictionaryFlow.value = false
            viewModel.dictionary?.let {
                navigator().showModeSettingsFragment(it.idDictionaries)
            }
        }

        loadCurrentDictionary()

        lifecycleScope.launch {
            viewModel.loadedDictionaryFlow.collect() {
                binding.progressBar.isVisible = !it
                if (it) {
                    initAdapter()
                }
            }
        }
        lifecycleScope.launch {
            viewModel.showMessage.collect() { msg ->
                msg?.let { showMessage(it) }
                viewModel.showMessage.value = null
            }
        }
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
        listWordsAdapter = ListWordsAdapter(viewModel.dictionary!!, ::showMenuActionOnWord)
        binding.listWordsRecyclerView.adapter = listWordsAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        binding.listWordsRecyclerView.layoutManager = layoutManager
        binding.listWordsRecyclerView.addItemDecoration(
            ListWordsAdapter.VerticalSpaceItemDecoration(50)
        )
    }

    private fun showMenuActionOnWord(word: Word, position: Int) {
        MenuSelectingActionsOnWord {
            viewModel.deleteWord(word.idWord) {
                viewModel.dictionary?.wordList?.removeIf { it.idWord == word.idWord }
                listWordsAdapter?.notifyItemRemoved(position)
            }
        }.show(parentFragmentManager, null)
    }

    private fun showMessage(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }

    private fun initButtons() {
        binding.addWordButton.setOnClickListener {
            val textFirst = binding.editTextWord.text.toString().replace("\n", ", ")
            val textLast = binding.editTextTranslation.text.toString().replace("\n", ", ")
            if (textFirst.isEmpty() || textLast.isEmpty()) return@setOnClickListener
            val word = Word(
                viewModel.dictionary!!.idDictionaries,
                textFirst,
                textLast,
                true
            )
            viewModel.dictionaryRepository.addWord(word) { idWord ->
                word.idWord = idWord
                viewModel.dictionary?.wordList?.add(word)
                listWordsAdapter?.notifyItemInserted(0)
                binding.listWordsRecyclerView.scrollToPosition(0)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddingWordsFragment()
    }
}