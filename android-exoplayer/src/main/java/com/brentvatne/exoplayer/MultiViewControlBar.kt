package com.brentvatne.exoplayer

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isEmpty
import com.brentvatne.react.R

@SuppressLint("ViewConstructor")
class MultiViewControlBar(
    context: Context,
    private var multiViewControlBarListener:
    MultiViewControlBarListener? = null,
) : LinearLayout(context), OnClickListener {

    interface MultiViewControlBarListener {
        fun onMultiviewPipButtonClicked() {}
        fun onMultiviewSwapButtonClicked() {}
    }

    init {
        orientation = HORIZONTAL
    }

    private val multiViewIndicator: ImageView by lazy { findViewById(R.id.btn_multiview_indicator) }
    private val pipButton: ImageView by lazy { findViewById(R.id.btn_multiview_pip) }
    private val swapButton: ImageView by lazy { findViewById(R.id.btn_multiview_swap) }

    var multiViewSize: Int = 0

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_multiview_indicator -> { // _TODO
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
        if (visible && multiViewSize > 1 && isEmpty()) {
            inflate(context, R.layout.comp_multiview_controlbar, this)
            multiViewIndicator.setOnClickListener(this)
            pipButton.setOnClickListener(this)
            swapButton.setOnClickListener(this)
        }
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
                multiViewIndicator.setImageResource(R.drawable.ic_multiview_3screen_selector)
                pipButton.visibility = GONE
            }

            4 -> {
                multiViewIndicator.setImageResource(R.drawable.ic_multiview_4screen_selector)
                pipButton.visibility = GONE
            }
        }
    }
}