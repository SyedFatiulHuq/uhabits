package org.isoron.uhabits.activities.common.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button

class AnimatingButton : View {
    interface OnCustomButtonClickListener {
        fun onCustomButtonClick()
    }

    private var paint: Paint = Paint()
    private var buttonText : String = ""
    private var clickListener: OnCustomButtonClickListener? = null
    private var isLongPress: Boolean = false


    private val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()*4
    private var longPressRunnable: Runnable? = null
    private var scaleFactor: Float = 1f
    private var buttonColor: Int = Color.YELLOW

    private var scaleAnimator: ValueAnimator? = null
    private var colorAnimator: ValueAnimator? = null

    fun setOnCustomButtonClickListener(listener: OnCustomButtonClickListener?) {
        this.clickListener = listener
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
//        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES

        paint.color = Color.GRAY
        paint.style = Paint.Style.FILL
        paint.textSize = 50f
    }

    fun setText(buttonText: String) {
        this.buttonText = buttonText
        contentDescription = buttonText
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the button background
        paint.color = buttonColor
        val centerX = width / 2f
        val centerY = height / 2f
        val scaledWidth = (width / 2f) * scaleFactor
        val scaledHeight = (height / 2f) * scaleFactor

        canvas.drawRect(
            centerX - scaledWidth,
            centerY - scaledHeight,
            centerX + scaledWidth,
            centerY + scaledHeight,
            paint
        )

        // Draw button text
        val textWidth = paint.measureText(buttonText)
        val textX = centerX - (textWidth / 2f)
        val textY = centerY - ((paint.descent() + paint.ascent()) / 2)
        paint.color = Color.BLACK
        canvas.drawText(buttonText, textX, textY, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isLongPress = false
                startScaleAndColorAnimation() // Start animation
                longPressRunnable = Runnable {
                    isLongPress = true
                    clickListener?.onCustomButtonClick()
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) // Optional haptic feedback
                    resetScaleAndColorAnimation() // Reset animation after long press
                }
                postDelayed(longPressRunnable!!, longPressTimeout)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Cancel animation if the press is released or interrupted
                removeCallbacks(longPressRunnable)
                resetScaleAndColorAnimation()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun startScaleAndColorAnimation() {
        // Cancel any running animations
        scaleAnimator?.cancel()
        colorAnimator?.cancel()

        // Scale animation
        scaleAnimator = ValueAnimator.ofFloat(1f, 0.8f).apply {
            duration = longPressTimeout
            addUpdateListener {
                scaleFactor = it.animatedValue as Float
                invalidate() // Redraw with new scale factor
            }
        }

        // Color animation
        colorAnimator = ValueAnimator.ofArgb(Color.YELLOW, Color.RED).apply {
            duration = longPressTimeout
            addUpdateListener {
                buttonColor = it.animatedValue as Int
                invalidate() // Redraw with new color
            }
        }

        // Start both animations
        scaleAnimator?.start()
        colorAnimator?.start()
    }

    private fun resetScaleAndColorAnimation() {
        // Cancel any running animations
        scaleAnimator?.cancel()
        colorAnimator?.cancel()

        // Reset scale factor
        scaleAnimator = ValueAnimator.ofFloat(scaleFactor, 1f).apply {
            duration = 200 // Quick reset
            addUpdateListener {
                scaleFactor = it.animatedValue as Float
                invalidate()
            }
        }

        // Reset color
        colorAnimator = ValueAnimator.ofArgb(buttonColor, Color.YELLOW).apply {
            duration = 200 // Quick reset
            addUpdateListener {
                buttonColor = it.animatedValue as Int
                invalidate()
            }
        }

        // Start both reset animations
        scaleAnimator?.start()
        colorAnimator?.start()
    }

    override fun performClick(): Boolean {
        // Handle the button click event here
        // Implement your custom button click logic
        return super.performClick()
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = Button::class.java.name  // Declare it as a button
    }
}

