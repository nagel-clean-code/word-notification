package com.nagel.wordnotification.presentation.settings

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.nagel.wordnotification.databinding.NumberPikerIntervalsBinding

class NumberPikerFragmentDialog(
    private val isHours: Boolean,
    private val startTime: String,
    private val callbackTime: (String) -> Unit
) : DialogFragment() {

    private lateinit var binding: NumberPikerIntervalsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NumberPikerIntervalsBinding.inflate(inflater, container, false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        setStyle(STYLE_NO_FRAME, android.R.style.Theme);

        binding.piker.apply {
            minValue = 0
            maxValue = if (isHours) 24 else 60
            value = startTime.toInt()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }


    private fun initListeners() {
        binding.saveTime.setOnClickListener {
            val time = binding.piker.value
            val stringHours = if (time < 10 && !isHours) "0$time" else time.toString()

            callbackTime.invoke(stringHours)
            dismiss()
        }
    }

}