package com.nagel.wordnotification.presentation.addingwords

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.databinding.FragmentAddingWordsBinding
import com.nagel.wordnotification.presentation.MainActivityVM
import com.nagel.wordnotification.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class AddingWordsFragment : BaseFragment() {

    @Inject
    lateinit var dictionaryRepository: DictionaryRepository

    private lateinit var binding: FragmentAddingWordsBinding
    private lateinit var listWordsAdapter: ListWordsAdapter
    override val viewModel: AddingWordsViewModel by viewModels()
    private val viewModelActivity: MainActivityVM by activityViewModels()
    private val recyclerView by lazy { binding.listWordsRecyclerView }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAddingWordsBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
//        initScroll()
        lifecycleScope.launch {
            viewModelActivity.myAccountDbEntity.collect {
                it?.let {
                    viewModel.loadDictionary(idAccount = it.id)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.loadedDictionaryFlow.collect() {
                if (it) {
                    binding.progressBar.isVisible = false
                    initAdapter()
                    initButtons()
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
                recyclerView.isNestedScrollingEnabled = true
            }
            if(scrollY == 0){
                recyclerView.isNestedScrollingEnabled = false
            }
        }
    }

    private fun initAdapter() {
        listWordsAdapter = ListWordsAdapter(dictionaryRepository)
        recyclerView.adapter = listWordsAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(
            ListWordsAdapter.VerticalSpaceItemDecoration(50)
        )
    }

    private fun initButtons() {
        binding.addWordButton.setOnClickListener {
            val textFirst = binding.editTextWord.text.toString().replace("\n", ", ")
            val textLast = binding.editTextTranslation.text.toString().replace("\n", ", ")
            if (textFirst.isEmpty() || textLast.isEmpty()) return@setOnClickListener
            listWordsAdapter.addWord(textFirst, textLast)
            recyclerView.scrollToPosition(0)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddingWordsFragment()
    }
}