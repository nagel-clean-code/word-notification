package com.nagel.wordnotification.presentation.choosingdictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nagel.wordnotification.core.services.Utils
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.databinding.FragmentChoosingDictionaryBinding
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import com.nagel.wordnotification.presentation.navigator.MainNavigator.Companion.ARG_SCREEN
import com.nagel.wordnotification.presentation.navigator.navigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ChoosingDictionaryFragment : BaseFragment() {
    class Screen() : BaseScreen

    private lateinit var binding: FragmentChoosingDictionaryBinding
    override val viewModel: ChoosingDictionaryVM by viewModels()
    private lateinit var adapter: DictionariesListAdapter

    @Inject
    lateinit var sessionRepository: SessionRepository
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChoosingDictionaryBinding.inflate(layoutInflater, container, false)
        binding.dictionariesList.addItemDecoration(
            DictionariesListAdapter.VerticalSpaceItemDecoration(70)
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initAdapter(words: List<Word>) {
        viewModel.idAccount = sessionRepository.getSession()!!.account!!.id
        val dictionariesAdapter = DictionariesListAdapter(
            dictionaries = viewModel.dictionaries,
            settingsRepository = viewModel.settingsRepository,
            allWord = words,
            requireContext(),
            ::openDictionary,
            ::showMenuActionOnWord,
            ::toggleActiveDictionary,
            ::openModeSettings
        )
        adapter = dictionariesAdapter
        binding.dictionariesList.adapter = adapter
        val layoutManager = LinearLayoutManager(requireContext())
        binding.dictionariesList.layoutManager = layoutManager

        viewLifecycleOwner.lifecycleScope.launch() {
            viewModel.dictionaries.collect() {
                binding.countDictionaries.text = it.size.toString()
            }
        }
    }

    private fun toggleActiveDictionary(dictionary: Dictionary, active: Boolean) {
        Utils.deleteNotification(dictionary.wordList)
        viewModel.toggleActiveDictionary(dictionary.idDictionary, active)
    }

    private fun openModeSettings(idDictionary: Long) {
        navigator().showModeSettingsFragment(idDictionary)
    }

    private fun showMenuActionOnWord(dictionary: Dictionary, position: Int) {
        MenuForDictionaryDialog(
            dictionary,
            ::showEditDictionaryDialog
        ) {
            viewModel.deleteWord(dictionary.idDictionary) {
                adapter.notifyItemRemoved(position)
                Utils.deleteNotification(dictionary.wordList)
            }
        }.show(parentFragmentManager, null)
    }

    private fun openDictionary(idDictionary: Long) {
        sessionRepository.saveCurrentIdDictionary(idDictionary)
        navigator().showAddingWordsFragment()
    }

    private fun initListeners() {
        binding.addButton.setOnClickListener {
            showAddDictionaryDialog()
        }
        lifecycleScope.launch {
            viewModel.showMessage.collect() { msg ->
                msg?.let { showMessage(it) }
                viewModel.showMessage.value = null
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadingWords.collect() {
                it?.let {
                    binding.progress.isVisible = false
                    initAdapter(it)
                }
            }
        }
    }

    private fun showEditDictionaryDialog(dictionary: Dictionary) {
        DictionaryEditorAppDialog(dictionary.name) { name ->
            viewModel.replaceNameDictionary(name = name, idDictionary = dictionary.idDictionary)
            binding.dictionariesList.scrollToPosition(0)
        }.show(childFragmentManager, DictionaryEditorAppDialog.TAG)
    }

    private fun showAddDictionaryDialog() {
        DictionaryEditorAppDialog() { name ->
            viewModel.addDictionary(name = name, viewModel.idAccount)
            binding.dictionariesList.scrollToPosition(0)
        }.show(childFragmentManager, DictionaryEditorAppDialog.TAG)
    }

    private fun showMessage(msgId: Int) {
        val msg = requireContext().getString(msgId)
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val ID_ACCOUNT = "ID_ACCOUNT"
        const val TAG = "CHOOSING_DICTIONARY_FRAGMENT"

        @JvmStatic
        fun newInstance(idAccount: Long) = ChoosingDictionaryFragment().apply {
            arguments = bundleOf(ID_ACCOUNT to idAccount)
        }
    }
}