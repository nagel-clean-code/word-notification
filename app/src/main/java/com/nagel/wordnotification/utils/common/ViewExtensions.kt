package com.nagel.wordnotification.utils.common

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton

const val BUNDLE_DTO = "DTO"

fun View.showKeyboard() {
    this.requestFocus()
    val controller = ViewCompat.getWindowInsetsController(this)
    controller?.show(WindowInsetsCompat.Type.ime())
}

fun View.hideKeyboard() {
    val controller = ViewCompat.getWindowInsetsController(this)
    controller?.hide(WindowInsetsCompat.Type.ime())
    this.clearFocus()
}

fun Activity.showKeyboard() {
    try {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        val view = currentFocus ?: View(this)
        imm?.showSoftInput(view, 0)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Activity.hideKeyboard() {
    try {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        val view = currentFocus ?: View(this)
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun View.isKeyboardVisible(): Boolean {
    val windowInsetsCompat = ViewCompat.getRootWindowInsets(this)
    return windowInsetsCompat?.isVisible(WindowInsetsCompat.Type.ime()) ?: false
}

fun TextView.setTextOrGone(value: String?) = when (value.isNullOrEmpty()) {
    true -> isVisible = false
    else -> {
        text = value
        isVisible = true
    }
}

fun TextView.setTextOrGone(valueResId: Int?) = when (valueResId == null) {
    true -> isVisible = false
    else -> {
        setText(valueResId)
        isVisible = true
    }
}

fun EditText.setTextIfChanged(data: String?) {
    if (text.toString() != data) {
        setText(data)
    }
}

fun TextView.setTextColorRes(id: Int?) {
    if (id != null && context != null) {
        val color = ContextCompat.getColor(context, id)
        setTextColor(color)
    }
}

inline fun <reified DTO : Parcelable, T : Fragment> T.setDto(dto: DTO) = apply {
    arguments = bundleOf(BUNDLE_DTO to dto)
}

inline fun <reified DTO : Parcelable> Fragment.getDto(): DTO? =
    arguments?.getParcelableCompat(BUNDLE_DTO)

fun <V : View> BottomSheetBehavior<V>.addBottomSheetCallback(
    onSlide: (bottomSheet: View, slideOffset: Float) -> Unit = { _, _ -> },
    onStateChanged: (bottomSheet: View, newState: Int) -> Unit = { _, _ -> }
) {
    addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            onStateChanged(bottomSheet, newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            onSlide(bottomSheet, slideOffset)
        }
    })
}

fun View.updateMargin(
    left: Int = 0,
    top: Int = 0,
    right: Int = 0,
    bottom: Int = 0
) {
    (layoutParams as? MarginLayoutParams)?.apply {
        leftMargin = left
        topMargin = top
        rightMargin = right
        bottomMargin = bottom
    }
}

fun ViewPager2.registerOnPageChangeCallback(
    onPageScrolled: (position: Int, positionOffset: Float, positionOffsetPixels: Int) -> Unit = { _, _, _ -> },
    onPageScrollStateChanged: (state: Int) -> Unit = { },
    onPageSelected: (position: Int) -> Unit = { },
) {
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            onPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            onPageSelected(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            onPageScrollStateChanged(state)
        }
    })
}

fun FloatingActionButton.hide(
    onShown: (fab: FloatingActionButton?) -> Unit = {},
    onHidden: (fab: FloatingActionButton?) -> Unit = {},
) = hide(object : FloatingActionButton.OnVisibilityChangedListener() {
    override fun onShown(fab: FloatingActionButton?) {
        super.onShown(fab)
        onShown(fab)
    }

    override fun onHidden(fab: FloatingActionButton?) {
        super.onHidden(fab)
        onHidden(fab)
    }
})

fun RecyclerView.addOnScrollListener(
    onScrolled: (recyclerView: RecyclerView, dx: Int, dy: Int) -> Unit = { _, _, _ -> },
    onScrollStateChanged: (recyclerView: RecyclerView, newState: Int) -> Unit = { _, _ -> }
) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int): Unit =
            onScrollStateChanged(recyclerView, newState)

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int): Unit =
            onScrolled(recyclerView, dx, dy)
    })
}

fun View.toBitmap(): Bitmap {
    measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    val bitmap = Bitmap.createBitmap(
        measuredWidth,
        measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    layout(0, 0, measuredWidth, measuredHeight)
    draw(canvas)
    return bitmap
}