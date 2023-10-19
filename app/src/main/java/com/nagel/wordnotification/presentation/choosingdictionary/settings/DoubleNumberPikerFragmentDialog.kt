package com.nagel.wordnotification.presentation.choosingdictionary.settings

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.nagel.wordnotification.databinding.SettingTimeIntervalsBinding

class DoubleNumberPikerFragmentDialog(
    private val startTime: String,
    private val callbackTime: (String) -> Unit
) : DialogFragment() {

    private lateinit var binding: SettingTimeIntervalsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingTimeIntervalsBinding.inflate(inflater, container, false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        setStyle(STYLE_NO_FRAME, android.R.style.Theme);

        binding.pikerHours.apply {
            minValue = 0
            maxValue = 24
            value = startTime.substringBefore(':').toInt()
        }
        binding.pikerMinutes.apply {
            minValue = 0
            maxValue = 60
            value = startTime.substringAfter(':').toInt()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }


    private fun initListeners() {
        binding.saveTime.setOnClickListener {
            val hours = binding.pikerHours.value
            val minutes = binding.pikerMinutes.value
            val stringMinutes = if (minutes < 10) "0$minutes" else minutes.toString()

            callbackTime.invoke("$hours:${stringMinutes}")
            dismiss()
        }
    }

}