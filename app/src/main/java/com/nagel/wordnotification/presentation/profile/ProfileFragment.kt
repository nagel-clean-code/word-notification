package com.nagel.wordnotification.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.firbase.RemoteDbRepository
import com.nagel.wordnotification.data.firbase.entity.CurrentPrices
import com.nagel.wordnotification.databinding.FragmentProfileBinding
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import com.nagel.wordnotification.utils.GlobalFunction.openUrl
import com.nagel.wordnotification.utils.Toggles
import com.nagel.wordnotification.utils.common.showToast
import dagger.hilt.android.AndroidEntryPoint
import io.appmetrica.analytics.AppMetrica
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {
    class Screen : BaseScreen

    @Inject
    lateinit var remoteDbRepository: RemoteDbRepository
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        remoteDbRepository.getFeatureToggles(
            success = {
                if (it.content.contains(Toggles.Purchases.name)) {
                    showPremium()
                } else {
                    premiumIsDisabled()
                }
            },
            error = ::showError
        )
    }

    private fun premiumIsDisabled() = with(binding) {
        errorText.text = resources.getString(R.string.purchases_not_available)
        showError()
    }

    private fun showPremium() {
        loadingInformationPremium()
        remoteDbRepository.requestPremiumInformation(
            success = ::chowPremiumInformation,
            error = ::showError
        )
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
            AppMetrica.reportEvent("sale_button_1_click")
            openLink(currentPrices.linkButton1)
        }
        saleButton2.root.setOnClickListener {
            AppMetrica.reportEvent("sale_button_2_click")
            openLink(currentPrices.linkButton2)
        }
        saleButton3.root.setOnClickListener {
            AppMetrica.reportEvent("sale_button_3_click")
            openLink(currentPrices.linkButton3)
        }
        getPremiumButton.setOnClickListener {
            AppMetrica.reportEvent("get_premium_button_click")
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

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}