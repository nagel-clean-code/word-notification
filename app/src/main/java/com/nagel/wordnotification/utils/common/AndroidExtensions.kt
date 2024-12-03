package com.nagel.wordnotification.utils.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.URLSpan
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.nagel.wordnotification.BuildConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.roundToInt


fun Activity.sendFile(file: File) {
    try {
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + ".provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
            startActivity(intent)
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

fun Int.dp() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    Resources.getSystem().displayMetrics
).roundToInt()

fun Int.toColorStateList(context: Context): ColorStateList {
    val color = ContextCompat.getColor(context, this)
    return ColorStateList.valueOf(color)
}

fun Int.toPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}

fun String.removePunctuations(): String {
    return StringUtils.removePunctuations(this)
}

fun Context.getDimension(resId: Int): Float {
    return AndroidResourceUtils.getDimension(resId, this)
}

fun Float.dp() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    Resources.getSystem().displayMetrics
)

fun <T> List<T>?.toArrayList(): ArrayList<T>? {
    return if (this != null) java.util.ArrayList(this) else null
}

inline fun <T> StateFlow<T>.collectStarted(
    lifecycleOwner: LifecycleOwner,
    crossinline block: (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            this@collectStarted.collect {
                block(it)
            }
        }
    }
}

fun Context.getDrawable(@DrawableRes id: Int?): Drawable? {
    return id?.let { AppCompatResources.getDrawable(this, it) }
}

fun Context?.getStringNotNull(
    @StringRes id: Int,
    vararg args: Any
): String {
    if (this == null) return String()

    return when (args.isEmpty()) {
        true -> getString(id)
        else -> getString(id, *args)
    }
}

fun TextView.removeLinksUnderline() {
    val spannable = SpannableString(text)
    for (urlSpan in spannable.getSpans(0, spannable.length, URLSpan::class.java)) {
        val newUrlSpan = object : URLSpan(urlSpan.url) {
            override fun updateDrawState(tp: TextPaint) {
                super.updateDrawState(tp)
                tp.isUnderlineText = false
            }
        }
        val spanStart = spannable.getSpanStart(urlSpan)
        val spanEnd = spannable.getSpanEnd(urlSpan)
        val flags = 0
        spannable.setSpan(newUrlSpan, spanStart, spanEnd, flags)
    }
    text = spannable
}

fun Fragment.doOnKeyboardHidden(action: () -> Unit) {
    val maxAttempts = 10
    var index = 0
    view?.viewTreeObserver?.addOnGlobalLayoutListener(object :
        ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (index++ >= maxAttempts || view?.isKeyboardVisible() != true) {
                action()
                view?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
            }
        }
    })
}

fun Activity.showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

fun Activity.showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Fragment.showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, resId, duration).show()
}

fun Fragment.showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, text, duration).show()
}

fun Activity.copyToClipboard(textToCopy: String) {
    val clipboardManager =
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    val clipData = ClipData.newPlainText(ClipDescription.MIMETYPE_TEXT_PLAIN, textToCopy)

    clipboardManager.setPrimaryClip(clipData)
}

fun Fragment.copyToClipboard(textToCopy: String) {
    activity?.copyToClipboard(textToCopy)
}

@OptIn(ExperimentalContracts::class)
fun Double?.isNullOrZero(): Boolean {
    contract {
        returns(false) implies (this@isNullOrZero != null)
    }
    return this == null || this == 0.0
}

@OptIn(ExperimentalContracts::class)
fun Double?.orZero(): Double {
    contract {
        returns(false) implies (this@orZero != null)
    }

    return when (this == null || this == 0.0) {
        true -> 0.0
        else -> this
    }
}

data class ViewTouchResult(
    val view: View?,
    val motionEvent: MotionEvent?,
    val isActionDown: Boolean,
    val isActionUp: Boolean,
    val isActionMove: Boolean
)

@SuppressLint("ClickableViewAccessibility")
fun View?.onTouch(
    onTouchResult: (result: ViewTouchResult) -> Boolean
) {
    this?.setOnTouchListener { view, motionEvent ->
        val action = motionEvent?.action
        val isActionDown = action == MotionEvent.ACTION_DOWN
        val isActionUp = action == MotionEvent.ACTION_UP
        val isActionMove = action == MotionEvent.ACTION_MOVE
        val result = ViewTouchResult(
            view = view,
            motionEvent = motionEvent,
            isActionDown = isActionDown,
            isActionUp = isActionUp,
            isActionMove = isActionMove
        )
        return@setOnTouchListener onTouchResult.invoke(result)
    }
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

@SuppressLint("QueryPermissionsNeeded")
fun Context.isMapsInstalled(): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="))
    return intent.resolveActivity(packageManager) != null
}

inline fun <reified T : Parcelable> Bundle?.getParcelableCompat(key: String): T? {
    return this?.let { bundle ->
        BundleCompat.getParcelable(bundle, key, T::class.java)
    }
}

inline fun <reified T : Parcelable> Bundle?.getParcelableArrayListCompat(key: String): ArrayList<T>? {
    return this?.let { bundle ->
        BundleCompat.getParcelableArrayList(bundle, key, T::class.java)
    }
}

fun Handler.postDelayed(delayInMillis: Long, runnable: Runnable) =
    postDelayed(runnable, delayInMillis)

fun Animation.setAnimationListener(
    onAnimationRepeat: (animation: Animation?) -> Unit = {},
    onAnimationStart: (animation: Animation?) -> Unit = {},
    onAnimationEnd: (animation: Animation?) -> Unit = {},
) {
    setAnimationListener(object : AnimationListener {
        override fun onAnimationStart(animation: Animation?) = onAnimationStart(animation)

        override fun onAnimationEnd(animation: Animation?) = onAnimationEnd(animation)

        override fun onAnimationRepeat(animation: Animation?) = onAnimationRepeat(animation)
    })
}

fun Context.isGooglePlayServicesAvailable(): Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)

    return resultCode == ConnectionResult.SUCCESS
}

fun View.delayOnLifecycle(
    durationInMillis: Long,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    block: () -> Unit
): Job? = findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
    lifecycleOwner.lifecycleScope.launch(dispatcher) {
        delay(durationInMillis)
        block()
    }
}