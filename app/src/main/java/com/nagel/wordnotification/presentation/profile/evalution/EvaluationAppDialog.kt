package com.nagel.wordnotification.presentation.profile.evalution

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.session.entities.SessionDataEntity
import com.nagel.wordnotification.databinding.EvaluationAppBinding
import com.nagel.wordnotification.utils.common.NavigationUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EvaluationAppDialog() : DialogFragment() {

    private lateinit var binding: EvaluationAppBinding
    private lateinit var manager: ReviewManager

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
        manager = ReviewManagerFactory.create(requireContext())
        session = sessionRepository.getSession()
        session.stepRatedApp = session.stepRatedApp!! + 1
    }

    private fun initListeners() {
        binding.sendButton.setOnClickListener {
            if (currentStart == 4) {
                session.ratedApp = true
                openGoogleStore()
            } else {
                val text = binding.text.text.toString().trim()
                if (text.isNotBlank()) {
                    NavigationUtils.sendEmail(MY_EMAIL, "Отзыв", text, requireContext())
                }
                dismiss()
            }
        }
        AnimationStars(requireContext()) {
            currentStart = it
        }.addStarsView(binding.linearStars)
    }

    private fun openGoogleStore() {
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                flow.addOnCompleteListener { _ ->
                    dismiss()
                }
            } else {
                NavigationUtils.openGooglePlayStore(requireContext())
                @ReviewErrorCode val reviewErrorCode = (task.exception as? ReviewException)?.errorCode
                Log.d("CoroutineWorker", "reviewErrorCode:$reviewErrorCode")
                dismiss()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sessionRepository.saveSession(session)
    }

    companion object {
        const val TAG = "EvaluationAppDialog"
        private const val MY_EMAIL = "nagel99word@gmail.com"
    }
}