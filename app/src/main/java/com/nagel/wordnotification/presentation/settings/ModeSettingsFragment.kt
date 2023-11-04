package com.nagel.wordnotification.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Flow
import androidx.core.view.children
import androidx.fragment.app.viewModels
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.data.settings.entities.SelectedMode
import com.nagel.wordnotification.databinding.FragmentModeSettingsBinding
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator
import dagger.hilt.android.AndroidEntryPoint


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
                textView.setBackgroundResource(R.drawable.unselected_day_week)
                textView.setTextColor(resources.getColor(R.color.black))
                textView.tag = true
            } else {
                textView.setTextColor(resources.getColor(R.color.light_beige))
                textView.setBackgroundResource(R.drawable.selected_day_week)
                textView.tag = false
            }
        }
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
    }

    private fun selectedDays(): List<String> {
        val listDays = arrayListOf<String>()
        binding.chainDaysWeek.children.forEach() { view: View ->
            if (view !is Flow) {
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