package com.nagel.wordnotification.presentation.settings

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.USE_EXACT_ALARM
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.helper.widget.Flow
import androidx.core.view.children
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
import com.nagel.wordnotification.core.analytecs.AppMetricaAnalytic
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.databinding.FragmentModeSettingsBinding
import com.nagel.wordnotification.presentation.ConfirmationDialog
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import com.nagel.wordnotification.presentation.navigator.MainNavigator
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import com.nagel.wordnotification.presentation.premiumdialog.PremiumDialog
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

    private val permissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            val isNotification = granted[POST_NOTIFICATIONS] == true
            val isExactAlarm = granted[USE_EXACT_ALARM] == true
            AppMetricaAnalytic.changeStatusNotification(isNotification && isExactAlarm)

            if (isNotification.not() || isExactAlarm.not()) {
                if (shouldShowRequestPermissionRationale(POST_NOTIFICATIONS) ||
                    shouldShowRequestPermissionRationale(USE_EXACT_ALARM)
                ) { // Если пользователь запретил не на всегда
                    //Объясняем пользователю зачем нам нужно это разрешение
                    RequestPermissionDialog(
                        text = resources.getString(R.string.permissions_required_for_the_algorithm_to_work),
                        exitButtonClick = ::postDelayBack,
                        provideButtonClick = ::requestPermissions,
                        onDestroy = ::postDelayBack
                    ).show(childFragmentManager, RequestPermissionDialog.TAG)
                } else {
                    // Перенаправим пользователя в настройки чтобы он руками изменил разрешение
                    askUserForOpeningAppSettings()
                }
            }
        }

    private fun postDelayBack() {
        binding.root.postDelayed({
            try {
                navigatorV2.goBack()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 100)
    }

    private fun askUserForOpeningAppSettings() {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireContext().packageName, null)
        )
        if (requireActivity().packageManager.resolveActivity(
                appSettingsIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            ) == null
        ) {
            RequestPermissionDialog(
                text = resources.getString(R.string.permissions_required_for_the_algorithm_to_work_manually),
                exitButtonClick = ::postDelayBack,
                onDestroy = ::postDelayBack
            ).show(childFragmentManager, RequestPermissionDialog.TAG)
        } else {
            RequestPermissionDialog(
                text = resources.getString(R.string.permissions_required_for_the_algorithm_to_work_manually),
                exitButtonClick = ::postDelayBack,
                onDestroy = ::postDelayBack,
                provideButtonClick = {
                    startActivity(appSettingsIntent)
                },
            ).show(childFragmentManager, RequestPermissionDialog.TAG)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentModeSettingsBinding.inflate(inflater, container, false)
        val screen = arguments?.getSerializable(MainNavigator.ARG_SCREEN) as Screen
        viewModel.preload(screen.idDictionary)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWeekday()
        initListeners()
        initOnBackPressed()
    }

    override fun onStart() {
        super.onStart()
        requestPermissions()
    }

    private fun initOnBackPressed() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isEnabled) {
                        AppMetricaAnalytic.reportEvent("start_algorithm_on_back_press")
                        isEnabled = false
                        saveMode()
                    }
                }
            }
            )
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionResult.launch(arrayOf(POST_NOTIFICATIONS, USE_EXACT_ALARM))
        }
    }

    private fun initListeners() = with(binding) {
        resettingButton.setOnClickListener {
            AppMetricaAnalytic.reportEvent("resetting_button_click")
            ConfirmationDialog(requireContext().getString(R.string.reset_text_algorithm)) {
                AppMetricaAnalytic.reportEvent("resetting_algorithm")
                viewModel.resettingAlgorithm()
            }.show(parentFragmentManager, null)
        }

        time1.setOnClickListener {
            AppMetricaAnalytic.reportEvent("choice_time1_click")
            if (viewModel.isStarted.get()) {
                getTimePiker(time1)
            } else {
                PremiumDialog(
                    text = resources.getString(R.string.it_is_available_to_configure_time_intervals),
                    isChoiceAdvertisement = false,
                ).show(childFragmentManager, PremiumDialog.TAG)
            }
        }
        time2.setOnClickListener {
            AppMetricaAnalytic.reportEvent("choice_time2_click")
            if (viewModel.isStarted.get()) {
                getTimePiker(time2)
            } else {
                PremiumDialog(
                    text = resources.getString(R.string.it_is_available_to_configure_time_intervals),
                    isChoiceAdvertisement = false,
                ).show(childFragmentManager, PremiumDialog.TAG)
            }
        }

        timeIntervals.setOnCheckedChangeListener { _, isChecked ->
            AppMetricaAnalytic.reportEvent(
                "time_interval_checkbox",
                mapOf("isChecked" to isChecked)
            )
        }

        sampleDays.setOnCheckedChangeListener { _, isChecked ->
            AppMetricaAnalytic.reportEvent("sample_days_checkbox", mapOf("isChecked" to isChecked))
        }

        initRadioButtons()
        saveButton.setOnClickListener {
            AppMetricaAnalytic.reportEvent("start_algorithm_button")
            navigatorV2.whenActivityActive {
                it.goBack()
            }
        }
        chainDaysWeek.children.forEachIndexed() { i, view: View ->
            if (view !is Flow) {
                if (i < 5) {
                    view.tag = false
                }
            }
        }
        infoButton.setOnClickListener {
            AppMetricaAnalytic.reportEvent("info_button_click")
            InformationDialog().show(childFragmentManager, InformationDialog.TAG)
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

    private fun getCountSelectedDays(): Int {
        var count = 0
        binding.chainDaysWeek.children.forEach { view: View ->
            if (view !is Flow) {
                if ((view as TextView).tag == false) {
                    ++count
                }
            }
        }
        return count
    }

    private fun initClickListenerItem(textView: TextView) {
        textView.setOnClickListener {
            if (textView.tag != true) {
                if (getCountSelectedDays() > 1) {
                    unselectedDayWeek(textView)
                }
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

    private fun saveMode() {
        val prevMode = viewModel.loadingMode.value
        val newMode = buildModeSettingsDto()
        Log.d("dd", "prevMode:" + prevMode)
        Log.d("dd", "newMode: " + newMode)
        Log.d("dd", "prevMode:" + prevMode.hashCode())
        Log.d("dd", "newMode: " + newMode.hashCode())

        if (newMode != prevMode) {
            val resetSteps = newMode.selectedMode != prevMode?.selectedMode
            val statusNotification = viewModel.getStatusNotificationDictionary()
            viewModel.saveNewSettings(newMode) {
                if (resetSteps) {
                    AppMetricaAnalytic.reportEvent(
                        "selected_mode",
                        mapOf("mode_name" to newMode.selectedMode?.getName(requireContext()))
                    )
                    viewModel.dictionary?.wordList?.let { list ->
                        viewModel.resetStepsSetTimeToCurrentOne(list)
                        viewModel.resetHistory(list) //TODO не обязательно - выпилить и учитывать это в показе шагов
                    }
                }
                viewModel.tryReinstallNotification(newMode, prevMode) {
                    navigatorV2.whenActivityActive {
                        it.showToast(R.string.changes_saved)
                        if (!statusNotification) {
                            it.showToast(R.string.notifications_enabled)
                        }
                        it.goBack()
                    }
                }
            }
        } else {
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