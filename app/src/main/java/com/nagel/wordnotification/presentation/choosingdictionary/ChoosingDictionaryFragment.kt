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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.adv.RewardedAdLoaderImpl
import com.nagel.wordnotification.core.analytecs.AppMetricaAnalytic
import com.nagel.wordnotification.core.services.Utils
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.firbase.RemoteDbRepository
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.databinding.FragmentChoosingDictionaryBinding
import com.nagel.wordnotification.presentation.ConfirmationDialog
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.exportAndImport.FileReader
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import com.nagel.wordnotification.presentation.navigator.navigator
import com.nagel.wordnotification.presentation.premiumdialog.PremiumDialog
import com.nagel.wordnotification.utils.Toggles
import com.nagel.wordnotification.utils.common.sendFile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
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
    lateinit var fileReader: FileReader

    @Inject
    lateinit var realtimeDb: RemoteDbRepository

    @Inject
    lateinit var repository: DictionaryRepository

    @Inject
    lateinit var rewardedAdLoader: RewardedAdLoaderImpl

    private var idAuthorUUID: String? = null
    private var accountId: Long? = null
    private var isAdvToggle = true

    private val fileImportIntentLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            closeFABMenu()
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                lifecycleScope.launch(Dispatchers.Default) {
                    fileReader.handleIntent(intent?.data, ::showPremiumDialog)
                }
            }
        }

    private val continueFlag = AtomicBoolean(false)

    private suspend fun showPremiumDialog(
        text: String,
        advertisementWasViewed: suspend () -> Unit
    ) {
        continueFlag.set(false)
        val currentCoroutineJob = currentCoroutineContext().job
        withContext(Dispatchers.Main) {
            PremiumDialog(
                text = text,
                isChoiceAdvertisement = isAdvToggle,
                showAdv = {
                    showAdv(advertisementWasViewed)
                },
                onCancel = { currentCoroutineJob.cancel() }
            ).show(childFragmentManager, PremiumDialog.TAG)
        }
        while (continueFlag.get().not()) {
            delay(50)
        }
    }

    private fun showAdv(
        advertisementWasViewed: suspend () -> Unit
    ) {
        rewardedAdLoader.show(
            award = {
                AppMetricaAnalytic.reportEvent("remuneration_for_importing_dictionaries")
                lifecycleScope.launch(Dispatchers.Default) {
                    advertisementWasViewed.invoke()
                    continueFlag.set(true)
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        realtimeDb.getFeatureToggles(success = { toggles ->
            isAdvToggle = toggles.content.contains(Toggles.Adv.name)
        })
        binding = FragmentChoosingDictionaryBinding.inflate(layoutInflater, container, false)
        binding.dictionariesList.addItemDecoration(
            DictionariesListAdapter.VerticalSpaceItemDecoration(70)
        )
        val animator = binding.dictionariesList.itemAnimator
        if (animator is DefaultItemAnimator) {
            animator.supportsChangeAnimations = false
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            idAuthorUUID = sessionRepository.getSession().account?.idAuthorUUID
            accountId = sessionRepository.getSession().account?.id
        }
        initListeners()
        initAdapter()
    }

    private fun initAdapter() {
        viewModel.idAccount = sessionRepository.getSession().account!!.id
        val dictionariesAdapter = DictionariesListAdapter(
            dictionaries = viewModel.dictionaries,
            settingsRepository = viewModel.settingsRepository,
            context = requireContext(),
            selectDictionary = ::openDictionary,
            showMenuActionOnWord = ::showMenuActionOnDictionary,
            setActive = ::toggleActiveDictionary,
            openModeSettings = ::openModeSettings
        )
        adapter = dictionariesAdapter
        binding.dictionariesList.adapter = adapter
        val layoutManager = LinearLayoutManager(requireContext())
        binding.dictionariesList.layoutManager = layoutManager

        viewLifecycleOwner.lifecycleScope.launch() {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dictionaries.collect() {
                    viewModel.setDictionaries(it)
                    binding.countDictionaries.text = it.size.toString()
                }
            }
        }
    }

    private fun toggleActiveDictionary(dictionary: Dictionary, active: Boolean) {
        AppMetricaAnalytic.reportEvent("toggle_active_dictionary")
        viewModel.toggleActiveDictionary(active, dictionary)
    }

    private fun openModeSettings(idDictionary: Long) {
        navigator()?.showModeSettingsFragment(idDictionary)
    }

    private fun showMenuActionOnDictionary(dictionary: Dictionary, position: Int) {
        AppMetricaAnalytic.reportEvent("show_menu_action_on_dictionary")
        MenuForDictionaryDialog(
            dictionary = dictionary,
            edit = ::showEditDictionaryDialog,
            copy = ::copyDictionary,
            exportDictionary = ::exportDictionary,
            delete = { showConfirmationDialog(dictionary) }
        ).show(parentFragmentManager, null)
    }

    private fun exportDictionary(dictionary: Dictionary) {
        viewModel.exportDictionary(dictionary, requireActivity()::sendFile)
    }

    private fun showConfirmationDialog(dictionary: Dictionary) {
        val text = getString(R.string.delete_s, dictionary.name)
        ConfirmationDialog(text) {
            viewModel.deleteDictionary(dictionary) {
                Utils.deleteNotification(dictionary.wordList)
            }
        }.show(parentFragmentManager, null)
    }

    private fun copyDictionary(dictionary: Dictionary) {
        if (viewModel.isStarted.get()) {
            viewModel.copyDictionary(dictionary) {
                with(binding) {
                    dictionariesList.layoutManager?.smoothScrollToPosition(
                        dictionariesList,
                        RecyclerView.State(),
                        0
                    )
                }
            }
        } else {
            PremiumDialog(
                text = resources.getString(R.string.copying_dictionaries_is_available),
                isChoiceAdvertisement = false,
            ).show(childFragmentManager, PremiumDialog.TAG)
        }
    }

    private fun openDictionary(idDictionary: Long) {
        sessionRepository.saveCurrentIdDictionary(idDictionary)
        navigator()?.showAddingWordsFragment()
    }

    private fun initListeners() = with(binding) {
        fab.setOnClickListener {
            if (View.GONE == fabBGLayout.visibility) {
                AppMetricaAnalytic.reportEvent("click_plus_dictionaries_screen")
                showFABMenu()
            } else {
                closeFABMenu()
            }
        }

        fabBGLayout.setOnClickListener { closeFABMenu() }
        addButton.setOnClickListener {
            showAddDictionaryDialog()
            closeFABMenu()
        }
        openLibrary.setOnClickListener {
            AppMetricaAnalytic.reportEvent("open_library_click")
            navigator()?.showLibraryDictionariesFragment()
            closeFABMenu()
        }
        importButton.setOnClickListener {
            AppMetricaAnalytic.reportEvent("import_button_click")
            if (realtimeDb.isTesting()) return@setOnClickListener
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
        exportButton.setOnClickListener() {
            AppMetricaAnalytic.reportEvent("export_button_click")
            navigator()?.showExportDictionariesFragment()
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
            exportLayout.visibility = View.VISIBLE
            fabBGLayout.visibility = View.VISIBLE
            fab.animate().rotationBy(180F)
            fabLayout1.animate().translationY(-resources.getDimension(R.dimen.standard_75))
            fabLayout2.animate().translationY(-resources.getDimension(R.dimen.standard_120))
            importLayout.animate().translationY(-resources.getDimension(R.dimen.standard_165))
            exportLayout.animate().translationY(-resources.getDimension(R.dimen.standard_210))
        }
    }

    private fun closeFABMenu() {
        binding.apply {
            fabBGLayout.visibility = View.GONE
            fab.animate().rotation(0F)
            fabLayout1.animate().translationY(0f)
            fabLayout2.animate().translationY(0f)
            importLayout.animate().translationY(0f)
            exportLayout.animate().translationY(0f)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {}
                    override fun onAnimationEnd(animator: Animator) {
                        if (View.GONE == fabBGLayout.visibility) {
                            fabLayout1.visibility = View.GONE
                            fabLayout2.visibility = View.GONE
                            importLayout.visibility = View.GONE
                            exportLayout.visibility = View.GONE
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