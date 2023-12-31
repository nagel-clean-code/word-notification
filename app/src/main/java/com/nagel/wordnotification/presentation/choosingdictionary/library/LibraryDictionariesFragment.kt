package com.nagel.wordnotification.presentation.choosingdictionary.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nagel.wordnotification.data.firbase.RealtimeDbRepository
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.databinding.FragmentLibraryDictionariesBinding
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import com.nagel.wordnotification.presentation.reader.ImportInCash
import com.nagel.wordnotification.utils.common.collectStarted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class LibraryDictionariesFragment : BaseFragment() {
    class Screen() : BaseScreen

    private lateinit var binding: FragmentLibraryDictionariesBinding
    override val viewModel: LibraryDictionariesVM by viewModels()
    private var adapter: ExpListAdapter? = null

    @Inject
    lateinit var navigatorV2: NavigatorV2

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var dataReader: ImportInCash

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLibraryDictionariesBinding.inflate(layoutInflater, container, false)
        binding.expandableListView.setGroupIndicator(null)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private var lastListDictionaries: RealtimeDbRepository.DictionariesLibrary? = null
    private fun initAdapter(library: RealtimeDbRepository.DictionariesLibrary) {
        if (lastListDictionaries?.contents == library.contents) return
        lastListDictionaries = library
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val list = library.getDictionaries(dataReader)
                binding.countDictionaries.text = list.size.toString()
                adapter = ExpListAdapter(list, viewModel::changeChecked)
                binding.expandableListView.setAdapter(adapter)
            }
        }
    }

    private fun initListeners() {
        viewModel.state.collectStarted(viewLifecycleOwner, ::handleState)
        binding.repeatButton.setOnClickListener {
            viewModel.loadDictionaries()
        }
        binding.addToYourselfButton.setOnClickListener {
            viewModel.addToDbDictionaries()
        }
    }

    private fun handleState(state: DictionariesLibraryScreenState) {
        binding.apply {
            progress.isVisible = state.isLoading
            errorLayout.isVisible = state.isError && !state.isLoading
            if (state.dictionariesList != null && !state.isLoading) {
                initAdapter(state.dictionariesList!!)
            }
            addToYourselfButton.isVisible = state.showAddButton
            if (!state.showAddButton) {
                adapter?.clearSelected()
            }
            if (state.closeAction) {
                navigatorV2.whenActivityActive {
                    it.goBack()
                }
            }
        }
    }

    companion object {
        private const val ID_ACCOUNT = "ID_ACCOUNT"
        const val TAG = "CHOOSING_DICTIONARY_FRAGMENT"

        @JvmStatic
        fun newInstance(idAccount: Long) = LibraryDictionariesFragment().apply {
            arguments = bundleOf(ID_ACCOUNT to idAccount)
        }
    }
}