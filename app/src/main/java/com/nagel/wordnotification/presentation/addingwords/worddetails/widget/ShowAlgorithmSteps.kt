package com.nagel.wordnotification.presentation.addingwords.worddetails.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.algorithms.AlgorithmHelper
import com.nagel.wordnotification.data.dictionaries.entities.NotificationHistoryItem
import com.nagel.wordnotification.presentation.addingwords.worddetails.widget.model.ShowStepsWordDto
import java.lang.Float.max
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    private lateinit var linePaintLast: Paint
    private lateinit var paintCircle: Paint
    private lateinit var paintCircleSelected: Paint
    private lateinit var paintCircleUnselected: Paint
    private var dataForRendering: ShowStepsWordDto? = null
    private var isAlgorithmEnabled: Boolean = false
    private val mTextSize = 14f
    private val linesWidth = 5
    private val linesWidthSelected = 7
    private val linesHeight = 100f
    private val radiusCircle = 15f
    private val marginTop = radiusCircle * 2
    private val marginLeft = radiusCircle
    private val marginTextLeft = 20f
    private var currentIx = 0
    private lateinit var textList: List<String>
    private val dateTemplate = SimpleDateFormat("(yyyy.MM.dd, HH:mm:ss)", Locale.getDefault())

    init {
        initPaint()
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
        linePaint.color = context.getColor(R.color.gray_0)

        linePaintLast = Paint()
        linePaintLast.isAntiAlias = true
        linePaintLast.strokeWidth =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics)
        linePaintLast.style = Paint.Style.FILL
        linePaintLast.color = context.getColor(R.color.purple)

        paintText = initPaintText(R.color.gray_3)
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

    fun setData(data: ShowStepsWordDto, isAlgorithmEnabled: Boolean) {
        this.isAlgorithmEnabled = isAlgorithmEnabled
        dataForRendering = data
        textList = generationText()
        requestLayout()
    }

    /**
     * @param isWords = true - отображаем даты шагов словами
     *                  false - даты датами
     */
    fun displayStepsIn(isWords: Boolean) {
        textList = generationText(isWords)
        requestLayout()
    }

    private fun generationText(isWords: Boolean = false): List<String> {
        val historyList = dataForRendering?.historyList ?: emptyList()
        val steps = dataForRendering?.mode?.selectedMode?.getCountSteps() ?: return listOf()
        val mode = dataForRendering?.mode ?: return listOf()
        val resultList = mutableListOf<String>()
        var lastDate = dataForRendering?.lastDateMention ?: -1
        val currentTime = Date().time
        for (i in 0 until steps) {
            if (i < historyList.size) {
                if (i == historyList.size - 1) {
                    currentIx = i
                }
                val current: NotificationHistoryItem = historyList[i]
                var text = context.getString(R.string.step)
                text += " №${current.learnStep + 1}   "
                if (isWords) {
                    mode.selectedMode?.getDateText(i + 1)?.let {
                        text += context?.resources?.getString(it)
                    }
                } else {
                    text += dateTemplate.format(current.dateMention)
                }
                resultList.add(text)
            } else {
                var nextDate = dataForRendering!!.mode.selectedMode?.getNewDate(i, lastDate)
                if (nextDate == null) {
                    resultList.add(context.resources.getString(R.string.completed))
                    continue
                }
                nextDate = AlgorithmHelper.nextAvailableDate(nextDate, mode)
                if (nextDate < currentTime) {
                    nextDate = AlgorithmHelper.nextAvailableDate(currentTime, mode)
                }
                var text = context.getString(R.string.step)
                text += " №${i + 1}   "
                if (isAlgorithmEnabled && !isWords) {
                    text += dateTemplate.format(nextDate)
                }
                if (isWords) {
                    mode.selectedMode?.getDateText(i + 1)?.let {
                        text += context?.resources?.getString(it)
                    }
                }
                resultList.add(text)
                lastDate = nextDate
            }
        }
        return resultList
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        dataForRendering?.mode?.selectedMode?.let { algorithm ->
            for (i in 0 until algorithm.getCountSteps() - 1) {
                rect.top = i * linesHeight.toInt() + marginTop.toInt()
                rect.bottom = (i + 1) * linesHeight.toInt() + marginTop.toInt()
                val currentLinesWidth = if (currentIx < i) {
                    linesWidth
                } else {
                    linesWidthSelected
                }
                rect.left = marginLeft.toInt() - currentLinesWidth / 2
                rect.right = rect.left + currentLinesWidth
                canvas.drawRect(rect, getPaintLine(i))
            }
            for (i in 0 until algorithm.getCountSteps()) {
                val offsetTop = i * linesHeight + marginTop
                canvas.drawCircle(
                    marginLeft,
                    offsetTop,
                    radiusCircle,
                    getPaintCircle(i)
                )
                val text = if (i >= textList.size) {
                    "Ошибка"
                } else {
                    textList[i]
                }
                canvas.drawText(
                    text,
                    marginLeft + radiusCircle * 2 + marginTextLeft,
                    offsetTop + mTextSize,
                    getPaintText(i)
                )
            }
        }
    }

    private fun getPaintText(i: Int): Paint {
        return if (currentIx == i) {
            paintTextSelected
        } else {
            paintText
        }
    }

    private fun getPaintLine(i: Int): Paint {
        return if (currentIx > i) {
            linePaintLast
        } else {
            linePaint
        }
    }

    private fun getPaintCircle(i: Int): Paint {
        return if (currentIx < i) {
            paintCircleUnselected
        } else if (currentIx == i) {
            paintCircleSelected
        } else {
            paintCircle
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minimumWidth =
            suggestedMinimumWidth + paddingLeft + paddingRight + (mTextSize * 60).toInt()
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