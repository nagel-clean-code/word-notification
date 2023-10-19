package com.nagel.wordnotification.presentation.choosingdictionary.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Flow
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.nagel.wordnotification.R
import com.nagel.wordnotification.databinding.FragmentModeSettingsBinding


class ModeSettingsFragment : Fragment() {

    private lateinit var binding: FragmentModeSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentModeSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWeekday()
        initListeners()
    }

    private fun initListeners() {
        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.repeaterCounter.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
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
        binding.time3.setOnClickListener {
            NumberPikerFragmentDialog(
                isHours = true,
                binding.time3.text.toString()
            ) { resultPikers ->
                binding.time3.text = resultPikers
            }.show(childFragmentManager, "")
        }
        binding.time4.setOnClickListener {
            NumberPikerFragmentDialog(
                isHours = false,
                binding.time4.text.toString()
            ) { resultPikers ->
                binding.time4.text = resultPikers
            }.show(childFragmentManager, "")
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
                textView.tag = true
            } else {
                textView.setBackgroundResource(R.drawable.selected_day_week)
                textView.tag = false
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ModeSettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            ModeSettingsFragment().apply {
                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
                }
            }
    }
}