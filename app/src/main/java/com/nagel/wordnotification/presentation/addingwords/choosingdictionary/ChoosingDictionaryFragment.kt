package com.nagel.wordnotification.presentation.addingwords.choosingdictionary

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nagel.wordnotification.R
import com.nagel.wordnotification.databinding.FragmentChoosingDictionaryBinding
import com.nagel.wordnotification.presentation.MainActivityVM
import com.nagel.wordnotification.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ChoosingDictionaryFragment : BaseFragment() {

    private lateinit var binding: FragmentChoosingDictionaryBinding
    override val viewModel: ChoosingDictionaryVM by viewModels()
    private val viewModelActivity: MainActivityVM by activityViewModels()
    private lateinit var adapter: DictionariesListAdapter
    private var idAccount = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChoosingDictionaryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initListeners()
    }

    private fun initAdapter() {
        idAccount = arguments?.getLong(ID_ACCOUNT) ?: -1
        adapter = DictionariesListAdapter(
            dictionaryRepository = viewModel.dictionaryRepository,
            allWord = viewModelActivity.allWords,
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
        setFragmentResult(TAG, bundleOf(DICTIONARY_ID_KEY to idDictionary))
        requireActivity().supportFragmentManager.popBackStack()
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
        const val DICTIONARY_ID_KEY = "DICTIONARY_ID_KEY"

        @JvmStatic
        fun newInstance(idAccount: Long) = ChoosingDictionaryFragment().apply {
            arguments = bundleOf(ID_ACCOUNT to idAccount)
        }
    }
}