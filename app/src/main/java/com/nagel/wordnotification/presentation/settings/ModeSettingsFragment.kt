package com.nagel.wordnotification.presentation.settings

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.helper.widget.Flow
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.algorithms.ForgetfulnessCurveLong
import com.nagel.wordnotification.core.algorithms.ForgetfulnessCurveShort
import com.nagel.wordnotification.core.algorithms.PlateauEffect
import com.nagel.wordnotification.core.services.Utils
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.databinding.FragmentModeSettingsBinding
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.base.ErrorResult
import com.nagel.wordnotification.presentation.base.PendingResult
import com.nagel.wordnotification.presentation.base.SuccessResult
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import com.nagel.wordnotification.presentation.navigator.MainNavigator
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import com.nagel.wordnotification.utils.common.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ModeSettingsFragment : BaseFragment() {

    data class Screen(val idDictionary: Long) : BaseScreen

    private lateinit var binding: FragmentModeSettingsBinding
    override val viewModel: ModeSettingsVM by viewModels()

    @Inject
    lateinit var navigatorV2: NavigatorV2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentModeSettingsBinding.inflate(inflater, container, false)
        val screen = arguments?.getSerializable(MainNavigator.ARG_SCREEN) as Screen
        val idDictionary = screen.idDictionary
        viewModel.idDictionary = idDictionary
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadDictionary(idDictionary)
            viewModel.loadCurrentSettings()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWeekday()
        initListeners()
        checkPermissions()
        initOnBackPressed()
    }

    private fun initOnBackPressed() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isEnabled) {
                        isEnabled = false
                        saveMode(true)
                    }
                }
            }
            )
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(POST_NOTIFICATIONS),
                    101
                )
            }
        }
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
            saveMode(true)
        }
        binding.chainDaysWeek.children.forEachIndexed() { i, view: View ->
            if (view !is Flow) {
                if (i < 5) {
                    view.tag = false
                }
            }
        }
        binding.infoButton.setOnClickListener {
            InformationDialog().show(childFragmentManager, InformationDialog.TAG)
        }
        initListenerLiveResult()
        initData()
    }

    private fun initListenerLiveResult() {
        binding.loadFrame.apply {
            viewModel.liveResult.observe(viewLifecycleOwner) { status ->
                when (status) {
                    is PendingResult -> {
                        root.isVisible = true
                        loadingLayout.isVisible = true
                        errorLayout.isVisible = false
                    }

                    is ErrorResult -> {
                        loadingLayout.isVisible = true
                        errorLayout.isVisible = false
                        if (isAdded) {
                            navigatorV2.whenActivityActive {
                                it.showToast(R.string.error_saving_mode)
                                it.goBack()
                            }
                        }
                    }

                    is SuccessResult -> {
                        navigatorV2.whenActivityActive {
                            it.showToast(R.string.changes_saved)
                            it.goBack()
                        }
                    }
                }
            }
            repeatButton.setOnClickListener {
                saveMode()
            }
        }
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
            val minutes = if (picker.minute < 10) {
                "0${picker.minute}"
            } else {
                picker.minute.toString()
            }
            textView.text = "${picker.hour}:${minutes}"
        }
    }

    private fun initData() {
        viewLifecycleOwner.lifecycleScope.launch() {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
        textView.setTextColor(resources.getColor(R.color.gray_3))
        textView.tag = true
    }

    private fun selectedDayWeek(textView: TextView) {
        textView.setTextColor(resources.getColor(R.color.light_beige))
        textView.setBackgroundResource(R.drawable.selected_day_week)
        textView.tag = false
    }

    private fun saveMode(goBack: Boolean = false) {
        val prevMode = viewModel.loadingMode.value
        val newMode = buildModeSettingsDto()
        Log.d("dd", "prevMode:" + prevMode)
        Log.d("dd", "newMode: " + newMode)
        Log.d("dd", "prevMode:" + prevMode.hashCode())
        Log.d("dd", "newMode: " + newMode.hashCode())

        if (newMode != prevMode) {
            val resetSteps = newMode.selectedMode != prevMode?.selectedMode
            viewModel.saveNewSettings(newMode, resetSteps)
            viewModel.dictionary?.wordList?.let { list -> //TODO удалять только текущую
                Utils.deleteNotification(list)
            }
            if (resetSteps) {
                viewModel.dictionary?.wordList?.let { list ->
                    viewModel.resetStepsSetTimeToCurrentOne(list)
                    viewModel.resetHistory(list)
                }
            } else {
                viewModel.reinstallNotification(newMode.idMode)
            }
        } else if (goBack) {
            navigatorV2.whenActivityActive {
                it.goBack()
            }
        }
    }

    private fun buildModeSettingsDto(): ModeSettingsDto {
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
            if (view is TextView && view.tag == false) {
                listDays.add(view.text.toString())
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