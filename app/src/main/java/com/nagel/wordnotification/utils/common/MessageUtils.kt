package com.nagel.wordnotification.utils.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.nagel.wordnotification.R
import com.nagel.wordnotification.presentation.MainActivity
import com.google.android.material.R as RMaterial

object MessageUtils {
    fun showToast(resId: Int, context: Context?) {
        if (context != null) Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }

    fun showToast(text: String?, context: Context?) {
        if (context != null) Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun showCustomToast(id: Int, context: Context?) {
        showCustomToast(AndroidResourceUtils.getString(id, context), context)
    }

    fun showCustomToast(text: String?, context: Context?) {
        if (context == null) return
        val inflater = LayoutInflater.from(context)
        @SuppressLint("InflateParams") val layout = inflater.inflate(R.layout.custom_toast, null)
        val text1 = layout.findViewById<TextView>(R.id.tvCustomToastText)
        text1.text = text
        val toast = Toast(context)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }

    fun showTopMessageGrey(textId: Int, activity: Activity?) {
        showTopMessage(
            textId = textId,
            activity = activity,
            bgColorResId = R.color.colorCustomSnackBarGrey
        )
    }

    fun showTopMessageRed(textId: Int, activity: Activity?) {
        showTopMessage(
            textId = textId,
            activity = activity,
            bgColorResId = R.color.colorCustomSnackBarRed
        )
    }

    private var isTopMessage = false
    private fun showTopMessage(
        textId: Int,
        activity: Activity?,
        bgColorResId: Int,
        textColorResId: Int = android.R.color.white
    ) {
        if (activity == null) return
        if (isTopMessage) {
            return
        }
        try {
            val inflater = LayoutInflater.from(activity)
            val viewGroup = activity.findViewById<ViewGroup>(android.R.id.content)
            val layout = inflater.inflate(
                R.layout.custom_snackbar, viewGroup, false
            ) as LinearLayout
            val rootView: ViewGroup = if (activity is MainActivity) {
                viewGroup.getChildAt(0).parent as ViewGroup
            } else {
                viewGroup.getChildAt(0).rootView as ViewGroup
            }
            layout.setPadding(0, StatusBarUtils.getStatusBarHeight(activity.baseContext), 0, 0)
            val tvSnackBarText = layout.findViewById<TextView>(R.id.tvSnackBarText)
            tvSnackBarText.setText(textId)
            layout.setBackgroundColor(ContextCompat.getColor(activity, bgColorResId))
            val animation = loadAnimation(activity, R.anim.anim_custom_snackbar)
            val contentColor = ContextCompat.getColor(activity, textColorResId)
            tvSnackBarText.setTextColor(contentColor)

            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
//                    StatusBarUtils.clearLightStatusBar(activity)
                    isTopMessage = true
                }

                override fun onAnimationEnd(animation: Animation) {
                    rootView.removeView(layout)
//                    StatusBarUtils.setLightStatusBar(activity)
                    isTopMessage = false
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            layout.startAnimation(animation)
            rootView.addView(layout)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    fun showSnack(textId: Int, activity: Activity?) {
        activity ?: return
        val snackbar = Snackbar.make(
            activity.findViewById(android.R.id.content),
            textId,
            Snackbar.LENGTH_SHORT
        )
        val view = snackbar.view
        val context = view.context
        val params = view.layoutParams as FrameLayout.LayoutParams
        val statusBarHeight = context.resources.getDimensionPixelSize(
            context.resources.getIdentifier(
                RESOURCE_NAME,
                DIMEN_DEF_TYPE,
                ANDROID_DEF_PACKAGE
            )
        )
        val toolbarHeight = 100 //activity.findViewById<Toolbar>(R.id.toolbar)?.height ?: 0

        view.layoutParams = params.apply {
            gravity = Gravity.TOP
            topMargin = statusBarHeight + toolbarHeight + NOTICE_MARGIN_TOP
        }

        with(snackbar) {
            setBackgroundTint(
                ContextCompat.getColor(context, R.color.search_medicine_warning_background)
            )
            setTextColor(ContextCompat.getColor(context, R.color.colorGrey900))
            view.setOnClickListener {
                dismiss()
            }
            val tv = this.view.findViewById<TextView>(RMaterial.id.snackbar_text)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tv.textAlignment = View.TEXT_ALIGNMENT_CENTER
            } else {
                tv.gravity = Gravity.CENTER_HORIZONTAL
            }
            show()
        }
    }

    private const val NOTICE_MARGIN_TOP = 8
    private const val RESOURCE_NAME = "status_bar_height"
    private const val DIMEN_DEF_TYPE = "dimen"
    private const val ANDROID_DEF_PACKAGE = "android"
}