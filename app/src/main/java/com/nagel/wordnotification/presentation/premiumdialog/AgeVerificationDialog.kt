package com.nagel.wordnotification.presentation.premiumdialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.nagel.wordnotification.databinding.AgeVerificationDialogBinding
import com.nagel.wordnotification.utils.CountyUtils
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR
import java.util.Date

class AgeVerificationDialog(
    private val isAnAdultReturn: (Boolean) -> Unit,
) : DialogFragment() {

    private lateinit var binding: AgeVerificationDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AgeVerificationDialogBinding.inflate(inflater, container, false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setStyle(STYLE_NO_FRAME, android.R.style.Theme)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() = with(binding) {
        cancelButton.setOnClickListener {
            dismiss()
        }
        okButton.setOnClickListener {
            val isAnAdult = checkIsAnAdult()
            dismiss()
            isAnAdultReturn.invoke(isAnAdult)
        }
    }

    private fun checkIsAnAdult(): Boolean {
        with(binding.datePicker) {
            val calendar = Calendar.getInstance()
            calendar.set(YEAR, year)
            calendar.set(MONTH, month)
            calendar.set(DAY_OF_MONTH, dayOfMonth)
            val timestamp = calendar.timeInMillis

            val age = CountyUtils.getAgeOfMajorityOfCountry(requireContext())
            return (Date().time - timestamp) > (AGE_1_MILLISECONDS * age)
        }
    }

    companion object {
        const val TAG = "AgeVerificationDialog"
        private const val AGE_1_MILLISECONDS = 31536000 * 1000L
    }
}