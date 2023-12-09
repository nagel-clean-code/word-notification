package com.nagel.wordnotification.presentation.profile.evalution

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.session.entities.SessionDataEntity
import com.nagel.wordnotification.databinding.EvaluationAppBinding
import com.nagel.wordnotification.utils.common.NavigationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class EvaluationAppDialog() : DialogFragment() {

    private lateinit var binding: EvaluationAppBinding

    @Inject
    lateinit var sessionRepository: SessionRepository
    lateinit var session: SessionDataEntity
    private var currentStart = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EvaluationAppBinding.inflate(inflater, container, false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setStyle(STYLE_NO_FRAME, android.R.style.Theme)
        initListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = sessionRepository.getSession()!!
        session.stepRatedApp = session.stepRatedApp!! + 1
    }

    private fun initListeners() {
        binding.sendButton.setOnClickListener {
            if (currentStart == 4) {
                session.ratedApp = true
                NavigationUtils.openGooglePlayStore(requireContext())
            } else {
                val text = binding.text.text.toString().trim()
                if (text.isNotBlank()) {
                    NavigationUtils.sendEmail(MY_EMAIL, "Отзыв", text, requireContext())
                }
            }
            dismiss()
        }
        AnimationStars(requireContext()) {
            currentStart = it
        }.addStarsView(binding.linearStars)
    }

    override fun onPause() {
        super.onPause()
        runBlocking {
            sessionRepository.saveSession(session)
        }
    }

    companion object {
        const val TAG = "EvaluationAppDialog"
        private const val MY_EMAIL = "nagel99word@gmail.com"
    }
}