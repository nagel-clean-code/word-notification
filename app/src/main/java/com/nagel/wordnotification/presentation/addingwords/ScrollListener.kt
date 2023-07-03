package com.nagel.wordnotification.presentation.addingwords

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView


class ScrollViewExt : ScrollView {
    private var scrollViewListener: OnScrollChangeListener? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    fun setScrollViewListener(scrollViewListener: OnScrollChangeListener?) {
        this.scrollViewListener = scrollViewListener
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        scrollViewListener?.onScrollChange(this, l, t, oldl, oldt)
    }
}