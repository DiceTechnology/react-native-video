package com.brentvatne.exoplayer

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import com.brentvatne.react.R
import com.facebook.react.modules.i18nmanager.I18nUtil

@SuppressLint("ViewConstructor")
class MultiViewControlBar(
    context: Context,
    private var multiViewControlBarListener:
    MultiViewControlBarListener? = null,
) : LinearLayout(context), OnClickListener, View.OnFocusChangeListener {

    interface MultiViewControlBarListener {
        fun onMultiviewIndicatorClick() {}
        fun onMultiviewPipButtonClicked() {}
        fun onMultiviewSwapButtonClicked() {}
    }

    private val isRTL = I18nUtil.getInstance().isRTL(context)
    private val multiViewIndicator: ImageView by lazy { findViewById(R.id.btn_multiview_indicator) }
    private val pipButton: ImageView by lazy { findViewById(R.id.btn_multiview_pip) }
    private val swapButton: ImageView by lazy { findViewById(R.id.btn_multiview_swap) }

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
        setVisible(false)
    }

    override fun onFocusChange(child: View, hasFocus: Boolean) {
        ViewCompat.animate(child)
            .scaleX(if (hasFocus) 1.2f else 1.0f)
            .scaleY(if (hasFocus) 1.2f else 1.0f)
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

    fun setVisible(visible: Boolean) {
        visibility = if (visible) VISIBLE else GONE
        if (visible) {
            resetButtonsVisible()
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