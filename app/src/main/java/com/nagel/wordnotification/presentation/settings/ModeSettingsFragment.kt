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
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.algorithms.ForgetfulnessCurveLong
import com.nagel.wordnotification.core.algorithms.ForgetfulnessCurveShort
import com.nagel.wordnotification.core.algorithms.PlateauEffect
import com.nagel.wordnotification.core.services.Utils
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.databinding.FragmentModeSettingsBinding
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import com.nagel.wordnotification.presentation.navigator.MainNavigator
import com.nagel.wordnotification.presentation.navigator.navigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ModeSettingsFragment : BaseFragment() {

    data class Screen(val idDictionary: Long) : BaseScreen

    private lateinit var binding: FragmentModeSettingsBinding
    override val viewModel: ModeSettingsVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentModeSettingsBinding.inflate(inflater, container, false)
        val screen = arguments?.getSerializable(MainNavigator.ARG_SCREEN) as Screen
        val idDictionary = screen.idDictionary
        viewModel.idDictionary = idDictionary
        viewModel.loadWords(idDictionary)
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
            getTimePiker(binding.time1)
        }
        binding.time2.setOnClickListener {
            getTimePiker(binding.time2)
        }

        initRadioButtons()
        binding.saveButton.setOnClickListener {
            navigator().goBack()
        }
        binding.chainDaysWeek.children.forEachIndexed() { i, view: View ->
            if (view !is Flow) {
                if (i < 5) {
                    view.tag = false
                }
            }
        }
        initData()
    }

    private fun getTimePiker(textView: TextView) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(textView.text.toString().substringBefore(':').toInt())
            .setMinute(textView.text.toString().substringAfter(':').toInt())
            .setTitleText("Select Appointment time")
            .build()
        picker.show(childFragmentManager, "")

        picker.addOnPositiveButtonClickListener {
            textView.text = "${picker.hour}:${picker.minute}"
        }
    }

    private fun initData() {
        lifecycleScope.launch() {
            viewModel.loadingMode.collect() { mode ->
                if (mode == null) {
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
            PlateauEffect -> {
                binding.plateauEffect.isChecked = true
            }

            ForgetfulnessCurveLong -> {
                binding.forgetfulnessCurveLong.isChecked = true
            }

            ForgetfulnessCurveShort -> {
                binding.forgetfulnessCurve.isChecked = true
            }

            else -> {
            }
        }
    }

    private fun initRadioButtons() {
        binding.apply {
            plateauEffect.setOnClickListener {
                viewModel.selectedMode = PlateauEffect
                forgetfulnessCurveLong.isChecked = false
                forgetfulnessCurve.isChecked = false
            }
            forgetfulnessCurveLong.setOnClickListener {
                viewModel.selectedMode = ForgetfulnessCurveLong
                plateauEffect.isChecked = false
                forgetfulnessCurve.isChecked = false
            }
            forgetfulnessCurve.setOnClickListener {
                viewModel.selectedMode = ForgetfulnessCurveShort
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
        val prevMode = viewModel.loadingMode.value
        val newMode = makeModeSettingsDto()
        if (newMode != prevMode) {
            viewModel.saveSettings(newMode)
            showMsg(R.string.changes_saved)
            viewModel.words?.let { words ->
                Utils.deleteNotification(requireActivity().applicationContext, words)
                if (newMode.selectedMode != prevMode?.selectedMode) {
                    resetSteps(words)
                }
            }
        }
    }

    private fun showMsg(idMsg: Int) {
        val msg = requireContext().getString(idMsg)
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    private fun resetSteps(words: List<Word>) {
        words.forEach { word ->
            if (word.learnStep > 0) {
                viewModel.resettingAlgorithm(word)
            }
        }
    }

    private fun makeModeSettingsDto(): ModeSettingsDto {
        val prevMode = viewModel.loadingMode.value
        return ModeSettingsDto(
            idMode = prevMode?.idMode ?: 0,
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