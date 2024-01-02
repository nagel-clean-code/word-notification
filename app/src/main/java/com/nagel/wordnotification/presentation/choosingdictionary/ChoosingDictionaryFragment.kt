package com.nagel.wordnotification.presentation.choosingdictionary

import android.animation.Animator
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.services.Utils
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.databinding.FragmentChoosingDictionaryBinding
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import com.nagel.wordnotification.presentation.navigator.navigator
import com.nagel.wordnotification.presentation.reader.ImportInDb
import com.nagel.wordnotification.utils.common.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class ChoosingDictionaryFragment : BaseFragment() {
    class Screen() : BaseScreen

    private lateinit var binding: FragmentChoosingDictionaryBinding
    override val viewModel: ChoosingDictionaryVM by viewModels()
    private lateinit var adapter: DictionariesListAdapter

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var fileReader: ImportInDb

    private val fileImportIntentLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            closeFABMenu()
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                lifecycleScope.launch(Dispatchers.IO) {
                    fileReader.handleIntent(intent?.data) { msgId ->
                        withContext(Dispatchers.Main) {
                            activity?.showToast(msgId)
                        }
                    }
                }
            }
        }

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
        viewModel.idAccount = sessionRepository.getSession().account!!.id
        val dictionariesAdapter = DictionariesListAdapter(
            dictionaries = viewModel.dictionaries,
            settingsRepository = viewModel.settingsRepository,
            allWord = words,
            requireContext(),
            ::openDictionary,
            ::showMenuActionOnDictionary,
            ::toggleActiveDictionary,
            ::openModeSettings
        )
        adapter = dictionariesAdapter
        binding.dictionariesList.adapter = adapter
        val layoutManager = LinearLayoutManager(requireContext())
        binding.dictionariesList.layoutManager = layoutManager

        viewLifecycleOwner.lifecycleScope.launch() {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dictionaries.collect() {
                    viewModel.listDictionary = it
                    binding.countDictionaries.text = it.size.toString()
                }
            }
        }
    }

    private fun toggleActiveDictionary(dictionary: Dictionary, active: Boolean) {
        Utils.deleteNotification(dictionary.wordList)
        viewModel.toggleActiveDictionary(dictionary.idDictionary, active)
    }

    private fun openModeSettings(idDictionary: Long) {
        navigator()?.showModeSettingsFragment(idDictionary)
    }

    private fun showMenuActionOnDictionary(dictionary: Dictionary, position: Int) {
        MenuForDictionaryDialog(
            dictionary,
            ::showEditDictionaryDialog
        ) {
            viewModel.deleteDictionary(dictionary.idDictionary) {
//                adapter.notifyItemRemoved(position)
                Utils.deleteNotification(dictionary.wordList)
            }
        }.show(parentFragmentManager, null)
    }

    private fun openDictionary(idDictionary: Long) {
        sessionRepository.saveCurrentIdDictionary(idDictionary)
        navigator()?.showAddingWordsFragment()
    }

    private fun initListeners() {
        binding.apply {
            fab.setOnClickListener {
                if (View.GONE == fabBGLayout.visibility) {
                    showFABMenu()
                } else {
                    closeFABMenu()
                }
            }

            fabBGLayout.setOnClickListener { closeFABMenu() }
        }
        binding.addButton.setOnClickListener {
            showAddDictionaryDialog()
            closeFABMenu()
        }
        binding.openLibrary.setOnClickListener {
            navigator()?.showLibraryDictionariesFragment()
            closeFABMenu()
        }
        binding.importButton.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                fileImportIntentLauncher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingWords.collect() {
                    it?.let {
                        binding.progress.isVisible = false
                        initAdapter(it)
                    }
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

    private fun showFABMenu() {
        binding.apply {
            fabLayout1.visibility = View.VISIBLE
            fabLayout2.visibility = View.VISIBLE
            importLayout.visibility = View.VISIBLE
            fabBGLayout.visibility = View.VISIBLE
            fab.animate().rotationBy(180F)
            fabLayout1.animate().translationY(-resources.getDimension(R.dimen.standard_75))
            fabLayout2.animate().translationY(-resources.getDimension(R.dimen.standard_120))
            importLayout.animate().translationY(-resources.getDimension(R.dimen.standard_165))
        }
    }

    private fun closeFABMenu() {
        binding.apply {
            fabBGLayout.visibility = View.GONE
            fab.animate().rotation(0F)
            fabLayout1.animate().translationY(0f)
            fabLayout2.animate().translationY(0f)
            importLayout.animate().translationY(0f)
            importLayout.animate().translationY(0f)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {}
                    override fun onAnimationEnd(animator: Animator) {
                        if (View.GONE == fabBGLayout.visibility) {
                            fabLayout1.visibility = View.GONE
                            fabLayout2.visibility = View.GONE
                            importLayout.visibility = View.GONE
                        }
                    }

                    override fun onAnimationCancel(animator: Animator) {}
                    override fun onAnimationRepeat(animator: Animator) {}
                })
        }
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