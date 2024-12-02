package com.nagel.wordnotification.presentation.premiumdialog

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.nagel.wordnotification.databinding.PremiumAskDialogBinding
import com.nagel.wordnotification.utils.GlobalFunction.openUrl

class PremiumDialog(
    private val text: String,
    private val isChoiceAdvertisement: Boolean,
    private val advertisementWasViewed: () -> Unit = {}
) : DialogFragment() {

    private lateinit var binding: PremiumAskDialogBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PremiumAskDialogBinding.inflate(inflater, container, false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setStyle(STYLE_NO_FRAME, android.R.style.Theme)
        binding.text.text = text
        binding.watchAdsButton.isVisible = isChoiceAdvertisement
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
        watchAdsButton.setOnClickListener {
            //TODO реализовать просмотр рекламы
            advertisementWasViewed.invoke()
            dismiss()
        }
        getPremiumButton.setOnClickListener {
            requireContext().openUrl("https://t.me/notifier2023") //TODO добавить ссылку на получение PREMIUM
        }
    }

    companion object {
        const val TAG = "WordDetailsDialog"
    }
}