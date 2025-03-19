package com.brentvatne.exoplayer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.brentvatne.exoplayer.MultiViewControlBar.MultiViewControlBarListener
import com.brentvatne.react.R
import com.facebook.react.modules.i18nmanager.I18nUtil

@SuppressLint("ViewConstructor")
class MultiViewControlBar(context: Context) : LinearLayout(context), OnClickListener, View.OnFocusChangeListener,
    ControlBarHandler.Callback {

    interface MultiViewControlBarListener {
        fun onMultiviewIndicatorClick() {}
        fun onMultiviewPipButtonClicked() {}
        fun onMultiviewSwapButtonClicked() {}
        fun onMultiviewControlBarVisibleChanged(visible: Boolean) {}
    }

    private val scalePercent = 1.3f
    private val isRTL = I18nUtil.getInstance().isRTL(context)
    private val multiViewIndicator: ImageView by lazy { findViewById(R.id.btn_multiview_indicator) }
    private val pipButton: ImageView by lazy { findViewById(R.id.btn_multiview_pip) }
    private val swapButton: ImageView by lazy { findViewById(R.id.btn_multiview_swap) }
    private val controlBarHandler = ControlBarHandler(this)
    var multiViewControlBarListener: MultiViewControlBarListener? = null

    private var canShowControlBar: Boolean = false
    var multiViewSize: Int = 0

    init {
        inflate(context, R.layout.comp_multiview_controlbar, this)
        orientation = HORIZONTAL
        multiViewIndicator.onFocusChangeListener = this
        multiViewIndicator.setOnClickListener(this)
        pipButton.onFocusChangeListener = this
        pipButton.setImageResource(
            if (isRTL) R.drawable.ic_multiview_pip_rtl_selector
            else R.drawable.ic_multiview_pip_selector
        )
        pipButton.setOnClickListener(this)
        swapButton.onFocusChangeListener = this
        swapButton.setImageResource(
            if (isRTL) R.drawable.ic_multiview_swap_rtl_selector
            else R.drawable.ic_multiview_swap_selector
        )
        swapButton.setOnClickListener(this)
        setCanShowControlBar(false)
    }

    override fun onFocusChange(child: View, hasFocus: Boolean) {
        ViewCompat.animate(child)
            .scaleX(if (hasFocus) scalePercent else 1.0f)
            .scaleY(if (hasFocus) scalePercent else 1.0f)
            .translationZ(if (hasFocus) 1f else 0f)
            .start()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_multiview_indicator -> {
                multiViewControlBarListener?.onMultiviewIndicatorClick()
            }

            R.id.btn_multiview_pip -> {
                multiViewControlBarListener?.onMultiviewPipButtonClicked()
            }

            R.id.btn_multiview_swap -> {
                multiViewControlBarListener?.onMultiviewSwapButtonClicked()
            }
        }
    }

    override fun onDetachedFromWindow() {
        multiViewControlBarListener = null
        super.onDetachedFromWindow()
    }

    override fun onHandlerCallback() {
        setControlBarVisible(false)
    }

    private fun setControlBarVisible(visible: Boolean) {
        if (isVisible != visible) {
            multiViewControlBarListener?.onMultiviewControlBarVisibleChanged(visible)
        }
        visibility = if (visible) VISIBLE else GONE
    }

    fun onKeyEvent(event: KeyEvent): Boolean {
        if (canShowControlBar) {
            if (isVisible
                && event.keyCode == KeyEvent.KEYCODE_BACK
                && event.action == KeyEvent.ACTION_UP
            ) {
                setControlBarVisible(false)
                controlBarHandler.stop()
                return true
            } else if (isVisible) {
                controlBarHandler.start()
            } else if (!isVisible && isDpadKeyEvent(event)) {
                setControlBarVisible(true)
                controlBarHandler.start()
                return true
            }
        }
        return false
    }

    private fun isDpadKeyEvent(event: KeyEvent): Boolean {
        return event.keyCode == KeyEvent.KEYCODE_DPAD_UP
                || event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                || event.keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                || event.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                || event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || event.keyCode == KeyEvent.KEYCODE_ENTER
    }

    fun setCanShowControlBar(showControlBar: Boolean) {
        canShowControlBar = showControlBar
        setControlBarVisible(showControlBar)
        if (showControlBar) {
            resetButtonsVisible()
            controlBarHandler.start()
        } else {
            controlBarHandler.stop()
        }
    }

    private fun resetButtonsVisible() {
        when (multiViewSize) {
            2 -> {
                multiViewIndicator.setImageResource(R.drawable.ic_multiview_2screen_selector)
                pipButton.visibility = VISIBLE
            }

            3 -> {
                multiViewIndicator.setImageResource(
                    if (isRTL) R.drawable.ic_multiview_3screen_rtl_selector
                    else R.drawable.ic_multiview_3screen_selector
                )
                pipButton.visibility = GONE
            }

            4 -> {
                multiViewIndicator.setImageResource(R.drawable.ic_multiview_4screen_selector)
                pipButton.visibility = GONE
            }
        }
    }
}

internal class ControlBarHandler(private val callback: Callback) : Handler(Looper.getMainLooper()) {

    interface Callback {
        fun onHandlerCallback() {}
    }

    private val msg = 1
    private var delayMillis: Long = 5 * 1000L

    override fun handleMessage(message: Message) {
        callback.onHandlerCallback()
    }

    fun start() {
        stop()
        sendEmptyMessageDelayed(msg, delayMillis)
    }

    fun stop() {
        removeMessages(msg)
    }
}

internal open class MultiViewControlBarListenerWrapper(private val listener: MultiViewControlBarListener) : MultiViewControlBarListener by listener {
    override fun onMultiviewControlBarVisibleChanged(visible: Boolean) {
        onMultiviewControlBarVisibleChangedBefore(visible)
        listener.onMultiviewControlBarVisibleChanged(visible)
    }

    open fun onMultiviewControlBarVisibleChangedBefore(visible: Boolean) {}
}