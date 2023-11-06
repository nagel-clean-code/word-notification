package com.nagel.wordnotification.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.helper.widget.Flow
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.data.settings.entities.SelectedMode
import com.nagel.wordnotification.databinding.FragmentModeSettingsBinding
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ModeSettingsFragment : BaseFragment() {

    private lateinit var binding: FragmentModeSettingsBinding
    override val viewModel: ModeSettingsVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentModeSettingsBinding.inflate(inflater, container, false)
        viewModel.idDictionary = requireArguments().getLong(ID_DICTIONARY)
        viewModel.loadCurrentSettings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWeekday()
        initListeners()
    }

    private fun initListeners() {
        binding.time1.setOnClickListener {
            DoubleNumberPikerFragmentDialog(binding.time1.text.toString()) { resultPikers ->
                binding.time1.text = resultPikers
            }.show(childFragmentManager, "")
        }
        binding.time2.setOnClickListener {
            DoubleNumberPikerFragmentDialog(binding.time2.text.toString()) { resultPikers ->
                binding.time2.text = resultPikers
            }.show(childFragmentManager, "")
        }
        initRadioButtons()
        binding.saveButton.setOnClickListener {
            navigator().goBack()
        }
        initData()
    }

    private fun initData() {
        lifecycleScope.launch() {
            viewModel.loadingMode.collect() { mode ->
                if (mode == null){
                    binding.plateauEffect.isChecked = true
                    return@collect
                }
                binding.plateauEffect.isChecked = false
                initSelectedMode(mode)
                if (mode.sampleDays) {
                    binding.sampleDays.isChecked = true
                    initSelectedDays(mode)
                }
                if (mode.timeIntervals) {
                    binding.timeIntervals.isChecked = true
                    binding.time1.text = mode.workingTimeInterval.first
                    binding.time2.text = mode.workingTimeInterval.second
                }
            }
        }
    }

    private fun initSelectedDays(mode: ModeSettingsDto) {
        binding.chainDaysWeek.children.forEach { view ->
            if (view is TextView) {
                if (mode.days.contains(view.text.toString())) {
                    selectedDayWeek(view)
                } else {
                    unselectedDayWeek(view)
                }
            }
        }
    }

    private fun initSelectedMode(mode: ModeSettingsDto) {
        viewModel.selectedMode = mode.selectedMode
        when (mode.selectedMode) {
            SelectedMode.PlateauEffect -> {
                binding.plateauEffect.isChecked = true
            }

            SelectedMode.ForgetfulnessCurveLong -> {
                binding.forgetfulnessCurveLong.isChecked = true
            }

            SelectedMode.ForgetfulnessCurve -> {
                binding.forgetfulnessCurve.isChecked = true
            }

            else -> {
            }
        }
    }

    private fun initRadioButtons() {
        binding.apply {
            plateauEffect.setOnClickListener {
                viewModel.selectedMode = SelectedMode.PlateauEffect
                forgetfulnessCurveLong.isChecked = false
                forgetfulnessCurve.isChecked = false
            }
            forgetfulnessCurveLong.setOnClickListener {
                viewModel.selectedMode = SelectedMode.ForgetfulnessCurveLong
                plateauEffect.isChecked = false
                forgetfulnessCurve.isChecked = false
            }
            forgetfulnessCurve.setOnClickListener {
                viewModel.selectedMode = SelectedMode.ForgetfulnessCurve
                forgetfulnessCurveLong.isChecked = false
                plateauEffect.isChecked = false
            }
        }

    }

    private fun initWeekday() {
        binding.chainDaysWeek.children.forEachIndexed() { ix: Int, view: View ->
            if (view !is Flow) {
                if (binding.chainDaysWeek.children.count() - 1 - ix <= 2) {
                    (view as TextView).tag = true
                }
                initClickListenerItem(view as TextView)
            }
        }
    }

    private fun initClickListenerItem(textView: TextView) {
        textView.setOnClickListener {
            if (textView.tag != true) {
                unselectedDayWeek(textView)
            } else {
                selectedDayWeek(textView)
            }
        }
    }


    private fun unselectedDayWeek(textView: TextView) {
        textView.setBackgroundResource(R.drawable.unselected_day_week)
        textView.setTextColor(resources.getColor(R.color.black))
        textView.tag = true
    }

    private fun selectedDayWeek(textView: TextView) {
        textView.setTextColor(resources.getColor(R.color.light_beige))
        textView.setBackgroundResource(R.drawable.selected_day_week)
        textView.tag = false
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveSettings(
            ModeSettingsDto(
                idDictionary = viewModel.idDictionary,
                selectedMode = viewModel.selectedMode,
                sampleDays = binding.sampleDays.isChecked,
                days = selectedDays(),
                timeIntervals = binding.timeIntervals.isChecked,
                workingTimeInterval = Pair(
                    binding.time1.text.toString(),
                    binding.time2.text.toString()
                ),
            )
        )
        val msg = requireContext().getString(R.string.changes_saved)
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    private fun selectedDays(): List<String> {
        val listDays = arrayListOf<String>()
        binding.chainDaysWeek.children.forEach() { view: View ->
            if (view !is Flow && view.tag == false) {
                listDays.add((view as TextView).text.toString())
            }
        }
        return listDays
    }

    companion object {
        private const val ID_DICTIONARY = "ID_DICTIONARY"

        @JvmStatic
        fun newInstance(idDictionary: Long) =
            ModeSettingsFragment().apply {
                arguments = Bundle().apply {
                    putLong(ID_DICTIONARY, idDictionary)
                }
            }
    }
}