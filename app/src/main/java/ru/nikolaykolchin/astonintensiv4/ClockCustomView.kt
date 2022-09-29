package ru.nikolaykolchin.astonintensiv4

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val HOUR_TYPE = "hour_hand_type"
private const val MINUTE_TYPE = "minute_hand_type"
private const val SECOND_TYPE = "second_hand_type"

class ClockCustomView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val margin = 50
    private var fontSize = 0
    private var secondHandLength = 0
    private var minuteHandLength = 0
    private var hourHandLength = 0
    private var radius = 0
    private var isInit = false
    private val rect = Rect()
    private var userColorSecondHand = 0
    private var userLengthSecondHand = 0

    private val paintWhite by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
        }
    }
    private val paintSecondHand by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.RED
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
    }
    private val paintBlack by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            strokeWidth = 4f
            style = Paint.Style.STROKE
            textSize = fontSize.toFloat()
        }
    }

    init {
        val attributes: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.ClockCustomView)
        userColorSecondHand =
            attributes.getColor(R.styleable.ClockCustomView_second_hand_color, Color.RED)
        userLengthSecondHand = attributes.getInt(R.styleable.ClockCustomView_second_hand_length, -1)
        attributes.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        if (!isInit) initClock()

        canvas.drawColor(Color.rgb(30, 144, 255))
        drawClockFace(canvas)
        drawNumbers(canvas)
        drawMarking(canvas)
        drawHands(canvas)

        postInvalidateDelayed(1000)
        invalidate()
    }

    private fun initClock() {
        paintSecondHand.color = userColorSecondHand
        fontSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 16f,
            resources.displayMetrics
        ).toInt()
        val min = min(height, width)
        radius = min / 2 - margin
        minuteHandLength = radius - min / 20
        secondHandLength = if (userLengthSecondHand > 0) userLengthSecondHand else minuteHandLength
        hourHandLength = radius - min / 5
        isInit = true
        println(secondHandLength)
    }

    private fun drawClockFace(canvas: Canvas) {
        val w = (width / 2).toFloat()
        val h = (height / 2).toFloat()
        canvas.drawCircle(w, h, radius + margin - 10f, paintWhite)
        canvas.drawCircle(w, h, radius + margin - 10f, paintBlack)
        canvas.drawCircle(w, h, radius / 100f, paintBlack)
    }

    private fun drawNumbers(canvas: Canvas) {
        for (number in 1..12) {
            val tmp = number.toString()
            paintBlack.getTextBounds(tmp, 0, tmp.length, rect)
            val angle = Math.PI / 6 * (number - 3)
            val x = (width / 2 + cos(angle) * radius - rect.width() / 2).toFloat()
            val y = (height / 2 + sin(angle) * radius + rect.height() / 2).toFloat()
            canvas.drawText(tmp, x, y, paintBlack)
        }
    }

    private fun drawMarking(canvas: Canvas) {
        val w = (width / 2).toFloat()
        val h = (height / 2).toFloat()

        for (i in 1..60) {
            val angle = Math.PI * i / 30 - Math.PI / 2
            canvas.drawLine(
                (w + cos(angle) * (radius + 30)).toFloat(),
                (h + sin(angle) * (radius + 30)).toFloat(),
                (w + cos(angle) * (radius + 40)).toFloat(),
                (h + sin(angle) * (radius + 40)).toFloat(),
                paintBlack
            )
        }
    }

    private fun drawHands(canvas: Canvas) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR).toDouble()
        val minute = calendar.get(Calendar.MINUTE).toDouble()
        val second = calendar.get(Calendar.SECOND).toDouble()

        drawHand(canvas, (hour + minute / 60) * 5, HOUR_TYPE)
        drawHand(canvas, minute, MINUTE_TYPE)
        drawHand(canvas, second, SECOND_TYPE)
    }

    private fun drawHand(canvas: Canvas, loc: Double, handType: String) {
        val w = (width / 2).toFloat()
        val h = (height / 2).toFloat()
        val angle = Math.PI * loc / 30 - Math.PI / 2
        val handLength = when (handType) {
            HOUR_TYPE -> hourHandLength
            MINUTE_TYPE -> minuteHandLength
            else -> secondHandLength
        }
        val paintHand = if (handType == SECOND_TYPE) paintSecondHand else paintBlack
        canvas.drawLine(
            w, h, (w + cos(angle) * handLength).toFloat(),
            (h + sin(angle) * handLength).toFloat(), paintHand
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = min(measuredWidth, measuredHeight)
        setMeasuredDimension(size, size)
    }
}