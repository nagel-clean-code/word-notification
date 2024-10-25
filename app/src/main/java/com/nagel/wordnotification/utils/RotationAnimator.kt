package com.nagel.wordnotification.utils

import android.view.View
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RotationAnimator(
    startPosition: Int,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val view: View
) {

    private var counter = startPosition

    fun rotationRight() {
        val start = if (counter == 0 || counter == 360) 0 else 180
        val end = if (counter == 0 || counter == 360) 180 else 360
        counter = if (counter == 0 || counter == 360) 180 else 0
        lifecycleScope.launch {
            for (i in start..end step 6) {
                view.rotation = i.toFloat()
                delay(17)
            }
        }
    }

    fun rotationLeft() {
        val start = if (counter == 0 || counter == 360) 0 else 180
        val end = if (counter == 0 || counter == 360) 180 else 360
        counter = if (counter == 0 || counter == 360) 180 else 0
        lifecycleScope.launch {
            for (i in end downTo start step 6) {
                view.rotation = i.toFloat()
                delay(17)
            }
        }
    }
}