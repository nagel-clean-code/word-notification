package com.nagel.wordnotification.presentation.addingwords.worddetails.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.algorithms.PlateauEffect
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.presentation.addingwords.worddetails.widget.model.ShowStepsWordDto
import java.lang.Float.max

class ShowAlgorithmSteps(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private lateinit var rect: Rect
    private lateinit var paintText: Paint
    private lateinit var paintTextSelected: Paint
    private lateinit var linePaint: Paint
    private lateinit var paintCircle: Paint
    private lateinit var paintCircleSelected: Paint
    private lateinit var paintCircleUnselected: Paint
    private var dataForRendering: ShowStepsWordDto? = null

    init {
        initPaint()
        if (isInEditMode) {
            dataForRendering =
                ShowStepsWordDto(
                    ModeSettingsDto(
                        0,
                        0L,
                        PlateauEffect,
                        false,
                        listOf(),
                        false,
                        Pair("", "")
                    ),
                    false,
                    5,
                    1701679204
                )
        }
    }

    private fun initPaint() {
        rect = Rect()
        paintCircle = Paint()
        paintCircle.color = context.getColor(R.color.purple_500)
        paintCircle.style = Paint.Style.FILL

        paintCircleSelected = Paint()
        paintCircleSelected.color = context.getColor(R.color.carrot)
        paintCircleSelected.style = Paint.Style.FILL

        paintCircleUnselected = Paint()
        paintCircleUnselected.color = context.getColor(R.color.purple)
        paintCircleUnselected.style = Paint.Style.FILL

        linePaint = Paint()
        linePaint.isAntiAlias = true
        linePaint.strokeWidth =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics)
        linePaint.style = Paint.Style.FILL
        linePaint.color = Color.parseColor("#dddddd")

        paintText = initPaintText(R.color.black)
        paintTextSelected = initPaintText(R.color.carrot)
    }

    private fun initPaintText(colorSource: Int) = Paint().apply {
        typeface = ResourcesCompat.getFont(context, R.font.montserrat_bold)
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            mTextSize,
            resources.displayMetrics
        )
        color = context.getColor(colorSource)
    }

    fun setData(data: ShowStepsWordDto) {
        dataForRendering = data
        requestLayout()
        invalidate() //TODO проверить надо ли
    }

    private val linesWidth = 5
    private val linesHeight = 100f
    private val radiusCircle = 15f
    private val marginTop = radiusCircle * 2
    private val marginLeft = radiusCircle
    private val marginTextLeft = 20f
    private val mTextSize = 14f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        dataForRendering?.mode?.selectedMode?.let { algorithm ->
            rect.left = marginLeft.toInt() - linesWidth / 2
            rect.right = rect.left + linesWidth
            for (i in 0 until algorithm.getCountSteps() - 1) {
                rect.top = i * linesHeight.toInt() + marginTop.toInt()
                rect.bottom = (i + 1) * linesHeight.toInt() + marginTop.toInt()
                canvas.drawRect(rect, linePaint)
            }
            for (i in 0..algorithm.getCountSteps()) {
                val cy = i * linesHeight + marginTop
                canvas.drawCircle(
                    marginLeft,
                    cy,
                    radiusCircle,
                    getPaintCircle(i)
                )

                canvas.drawText(
                    "dkdkdkdkdkkdkdkdkd",
                    marginLeft + radiusCircle * 2 + marginTextLeft,
                    cy + mTextSize,
                    getPaintText(i)
                )

            }
        }
    }

    private fun getPaintText(i: Int): Paint {
        return if (dataForRendering!!.learnStep - 1 == i) {
            paintTextSelected
        } else {
            paintText
        }
    }

    private fun getPaintCircle(i: Int): Paint {
        return if (dataForRendering!!.learnStep - 1 < i) {
            paintCircleUnselected
        } else if (dataForRendering!!.learnStep - 1 == i) {
            paintCircleSelected
        } else {
            paintCircle
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minimumWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val minimumHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        val newHeight = (dataForRendering?.mode?.selectedMode?.getCountSteps()
            ?: 0) * linesHeight
        val sumHeightCircle: Float = max(newHeight, minimumHeight.toFloat())

        val desiredWidth = Integer.max(sumHeightCircle.toInt(), minimumWidth)
        val desiredHeight = Integer.max(sumHeightCircle.toInt(), minimumHeight)

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    private fun getPixelsFromDP(dp: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

}