package com.nagel.wordnotification.presentation.choosingdictionary

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nagel.wordnotification.Constants
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.databinding.FragmentChoosingDictionaryBinding
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initAdapter(words: List<Word>) {
        idAccount = arguments?.getLong(ID_ACCOUNT) ?: -1
        adapter = DictionariesListAdapter(
            dictionaryRepository = viewModel.dictionaryRepository,
            allWord = words,
            idAccount = idAccount,
            requireContext(),
            ::openDictionary
        )
        binding.dictionariesList.adapter = adapter
        val layoutManager = LinearLayoutManager(requireContext())
        binding.dictionariesList.layoutManager = layoutManager
        binding.dictionariesList.addItemDecoration(
            DictionariesListAdapter.VerticalSpaceItemDecoration(50)
        )
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

    private fun showMessage(msg: String) {
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