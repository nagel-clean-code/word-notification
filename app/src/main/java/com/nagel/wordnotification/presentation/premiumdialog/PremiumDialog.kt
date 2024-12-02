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
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.firbase.RemoteDbRepository
import com.nagel.wordnotification.data.firbase.entity.CurrentPrices
import com.nagel.wordnotification.databinding.PremiumAskDialogBinding
import com.nagel.wordnotification.utils.GlobalFunction.openUrl
import com.nagel.wordnotification.utils.common.showToast
import dagger.hilt.android.AndroidEntryPoint
import io.appmetrica.analytics.AppMetrica
import javax.inject.Inject

@AndroidEntryPoint
class PremiumDialog(
    private val text: String,
    private val isChoiceAdvertisement: Boolean,
    private val advertisementWasViewed: () -> Unit = {}
) : DialogFragment() {

    private lateinit var binding: PremiumAskDialogBinding

    @Inject
    lateinit var remoteDbRepository: RemoteDbRepository

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
            AppMetrica.reportEvent("watch_ads_button_click")
            advertisementWasViewed.invoke()
            dismiss()
        }
        getPremiumButton.setOnClickListener {
            AppMetrica.reportEvent("get_premium_button_click")
            remoteDbRepository.requestPremiumInformation(
                success = ::openLinkPremium,
                error = ::showErrorPremium
            )
        }
    }

    private fun openLinkPremium(data: CurrentPrices) {
        data.linkGetPremiumButton.let {
            if (it.isNotBlank()) {
                requireContext().openUrl(it)
            } else {
                showErrorPremium()
            }
        }
    }

    private fun showErrorPremium() {
        requireActivity().showToast(R.string.purchases_not_available)
    }

    companion object {
        const val TAG = "WordDetailsDialog"
    }
}