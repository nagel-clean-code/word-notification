package com.nagel.wordnotification.presentation.profile.evalution

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import com.nagel.wordnotification.R
import com.nagel.wordnotification.utils.AnimationTools

class AnimationStars(
    private val context: Context,
    private val listener: (Int) -> Unit
) {

    private val listStars = mutableListOf<ImageView>()

    fun addStarsView(viewGroup: ViewGroup) {
        listStars.clear()
        repeat(5) {
            val imageView = ImageView(context)
//            if (it > task.currentEvaluation) {
            imageView.setImageResource(R.drawable.ic_star_empty)
//            } else {
//                imageView.setImageResource(R.drawable.ic_star_filled)
//            }
            imageView.setPadding(15, 0, 15, 0)
            imageView.tag = it

            initListenerOnClickStar(imageView)
            viewGroup.addView(imageView)
            listStars.add(imageView)
        }
    }

    private fun initListenerOnClickStar(imageView: ImageView) {
        imageView.setOnClickListener {
            val evaluation = (it as ImageView).tag as Int
            animationFilled(evaluation)
        }
    }

    private fun animationFilled(clickedStarPosition: Int) {
        var timeCounter = 0L
        listStars.forEach {
            if ((it.tag as Int) <= clickedStarPosition) {
                it.postDelayed({ zoomAnimation(it) }, timeCounter)
                timeCounter += 50
            } else {
                it.setImageResource(R.drawable.ic_star_empty)
            }
        }
        listener.invoke(clickedStarPosition)
    }

    private fun zoomAnimation(imageView: ImageView) {
        AnimationTools.viewClip(imageView)
        imageView.setImageResource(R.drawable.ic_star_filled)

        val scale = 1.1f + ((imageView.tag as Int) * 0.2f)
        val animBuf = imageView.animate().scaleX(scale).scaleY(scale).setDuration(50)
        animBuf.setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                imageView.animate().scaleX(1.0f).scaleY(1.0f).duration = 50
                animBuf.setListener(null)
            }
        })
    }

}