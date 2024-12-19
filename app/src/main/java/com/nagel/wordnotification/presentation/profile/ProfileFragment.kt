package com.nagel.wordnotification.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.analytecs.AppMetricaAnalytic
import com.nagel.wordnotification.data.firbase.entity.CurrentPrices
import com.nagel.wordnotification.databinding.FragmentProfileBinding
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import com.nagel.wordnotification.utils.GlobalFunction.openUrl
import com.nagel.wordnotification.utils.common.collectStarted
import com.nagel.wordnotification.utils.common.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {
    class Screen : BaseScreen

    private lateinit var binding: FragmentProfileBinding
    override val viewModel: ProfileVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        initListeners()
        return binding.root
    }

    private fun premiumIsDisabled() = with(binding) {
        errorText.text = resources.getString(R.string.purchases_not_available)
        showError()
    }

    private fun showPremium() {
        loadingInformationPremium()
        viewModel.requestPremiumInformation()
    }


    private fun showError() = with(binding) {
        getPremiumButton.isVisible = false
        progressBar.isVisible = false
        linearLayout3.isVisible = false
        saleTextImg.isInvisible = true
        errorText.isVisible = true
    }

    private fun chowPremiumInformation(currentPrices: CurrentPrices) = with(binding) {
        errorText.isVisible = false
        getPremiumButton.isVisible = true
        progressBar.isVisible = false

        saleButton1.price.text = resources.getString(R.string.rub_x, currentPrices.button1Price)
        saleButton2.price.text = resources.getString(R.string.rub_x, currentPrices.button2Price)
        saleButton3.price.text = resources.getString(R.string.rub_x, currentPrices.button3Price)

        saleButton1.title.text = currentPrices.button1Title
        saleButton2.title.text = currentPrices.button2Title
        saleButton3.title.text = currentPrices.button3Title

        saleButton1.root.setOnClickListener {
            AppMetricaAnalytic.reportEvent("sale_button_1_click")
            openLink(currentPrices.linkButton1)
        }
        saleButton2.root.setOnClickListener {
            AppMetricaAnalytic.reportEvent("sale_button_2_click")
            openLink(currentPrices.linkButton2)
        }
        saleButton3.root.setOnClickListener {
            AppMetricaAnalytic.reportEvent("sale_button_3_click")
            openLink(currentPrices.linkButton3)
        }
        getPremiumButton.setOnClickListener {
            AppMetricaAnalytic.reportEvent("get_premium_button_click")
            openLink(currentPrices.linkGetPremiumButton)
        }

        linearLayout3.isVisible = true
        saleTextImg.isVisible = true
    }

    private fun openLink(link: String) {
        if (link.isNotBlank()) {
            requireContext().openUrl(link)
        } else {
            requireActivity().showToast(R.string.purchases_not_available)
        }
    }

    private fun loadingInformationPremium() = with(binding) {
        errorText.isVisible = false
        getPremiumButton.isInvisible = true
        progressBar.isVisible = true
        linearLayout3.isVisible = false
        saleTextImg.isInvisible = true
    }

    private fun initListeners() = with(binding) {
        viewModel.state.collectStarted(viewLifecycleOwner, ::renderState)

        vkButton.setOnClickListener {
            requireContext().openUrl("https://vk.com/club223679470")  //Желательно ссылки брать с Firbase
        }
        okButton.setOnClickListener {
            requireContext().openUrl("https://ok.ru/group/70000004748309")
        }
        telegramButton.setOnClickListener {
            requireContext().openUrl("https://t.me/notifier2023")
        }
    }

    private fun renderState(state: ProfileUiState) = with(binding) {
        state.showErrorEvent?.getContentIfNotHandled()?.let {
            showError()
        }
        state.showPremiumEvent?.getContentIfNotHandled()?.let {
            showPremium()
        }
        state.premiumIsDisabledEvent?.getContentIfNotHandled()?.let {
            premiumIsDisabled()
        }
        state.chowPremiumInformationEvent?.getContentIfNotHandled()?.let { currentPrices ->
            chowPremiumInformation(currentPrices)
        }
        state.youHavePremium?.getContentIfNotHandled()?.let { date ->
            showYouHavePremium(date)
        }
    }

    private fun showYouHavePremium(date: String) = with(binding) {
        linearLayout3.isInvisible = true
        progressBar.isVisible = false
        saleTextImg.setImageResource(R.drawable.you_have_premium)
        getPremiumButton.text = resources.getString(R.string.to_extend)
        timeEndPremium.text = date
        timeEndPremium.isVisible = true
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}