package com.nagel.wordnotification.presentation.onboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.nagel.wordnotification.R
import com.nagel.wordnotification.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initPager()
        initButtonSkip()
        initButtonContinue()
    }

    private fun initPager() {
        val adapter = OnBoardAdapter()
        binding.pager.adapter = adapter
        binding.pager.offscreenPageLimit = 1
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                processSkipButton(position)
                updateButtonContinueText(position)
            }
        })
        TabLayoutMediator(binding.tabLayout, binding.pager, true) { _, _ -> }.attach()
    }

    private fun initButtonSkip() {
        binding.tvBtnSkip.setOnClickListener {
            finish()
        }
    }

    private fun processSkipButton(position: Int) {
        val isLastFragment = OnBoardAdapter.images.size == position + 1
        binding.tvBtnSkip.isClickable = !isLastFragment
        binding.tvBtnSkip.isFocusable = !isLastFragment
        binding.tvBtnSkip.isVisible = !isLastFragment
    }

    private fun initButtonContinue() {
        binding.continueButton.setOnClickListener {
            val isLastFragment = OnBoardAdapter.images.size == binding.pager.currentItem + 1
            if (isLastFragment) {
                finish()
                return@setOnClickListener
            }
            binding.pager.currentItem++
        }
    }

    private fun updateButtonContinueText(position: Int) {
        val textResId = when (position == OnBoardAdapter.images.size - 1) {
            true -> R.string.begin
            else -> R.string.onboard_button_continue
        }
        binding.continueButton.setText(textResId)
    }
}