package com.nagel.wordnotification.presentation.choosingdictionary

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nagel.wordnotification.Constants
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.services.Utils
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.databinding.FragmentChoosingDictionaryBinding
import com.nagel.wordnotification.presentation.addingwords.MenuSelectingActions
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator
import com.nagel.wordnotification.utils.SharedPrefsUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ChoosingDictionaryFragment : BaseFragment() {

    private lateinit var binding: FragmentChoosingDictionaryBinding
    override val viewModel: ChoosingDictionaryVM by viewModels()
    private lateinit var adapter: DictionariesListAdapter
    private var idAccount = -1L

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
        idAccount = arguments?.getLong(ID_ACCOUNT) ?: -1
        val dictionariesAdapter = DictionariesListAdapter(
            dictionaryRepository = viewModel.dictionaryRepository,
            settingsRepository = viewModel.settingsRepository,
            allWord = words,
            idAccount = idAccount,
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

        lifecycleScope.launch() {
            dictionariesAdapter.dictionaries.collect() {
                binding.countDictionaries.text = it.size.toString()
            }
        }
    }

    private fun toggleActiveDictionary(dictionary: Dictionary, active: Boolean){
        Utils.deleteNotification(requireActivity().applicationContext, dictionary.wordList)
        viewModel.toggleActiveDictionary(dictionary.idDictionaries,active)
    }

    private fun openModeSettings(idDictionary: Long) {
        navigator().showModeSettingsFragment(idDictionary)
    }

    private fun showMenuActionOnWord(dictionary: Dictionary, position: Int) {
        MenuSelectingActions {
            viewModel.deleteWord(dictionary.idDictionaries) {
                adapter.notifyItemRemoved(position)
                Utils.deleteNotification(requireActivity().applicationContext, dictionary.wordList)
            }
        }.show(parentFragmentManager, null)
    }

    private fun openDictionary(idDictionary: Long) {
        SharedPrefsUtils.setLongPreference(
            requireContext(),
            Constants.DICTIONARY_ID_KEY,
            idDictionary
        )
        navigator().showAddingWordsFragment()
    }

    private fun initListeners() {
        binding.addButton.setOnClickListener {
            showAlertDialog()
        }
        lifecycleScope.launch {
            viewModel.showMessage.collect() { msg ->
                msg?.let { showMessage(it) }
                viewModel.showMessage.value = null
            }
        }
        lifecycleScope.launch {
            viewModel.loadingWords.collect() {
                it?.let {
                    binding.progress.isVisible = false
                    initAdapter(it)
                }
            }
        }
    }

    private fun showAlertDialog() {
        val input = EditText(requireContext())
        input.inputType = EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS
        input.setHint(R.string.input_name_dictionary)
        val buttonClickListener = DialogInterface.OnClickListener { dialog, witch ->
            when (witch) {
                DialogInterface.BUTTON_POSITIVE -> {
                    viewModel.addDictionary(name = input.text.toString(), idAccount)
                    binding.dictionariesList.scrollToPosition(0)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.create_dictionary)
            .setView(input)
            .setPositiveButton(R.string.add, buttonClickListener)
            .setNegativeButton(R.string.cancel, buttonClickListener)
            .show()
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