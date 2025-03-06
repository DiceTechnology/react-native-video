package com.brentvatne.exoplayer

import android.content.Context
import android.view.View
import android.widget.FrameLayout

class MultiViewLayout(context: Context) : FrameLayout(context) {

    override fun requestLayout() {
        super.requestLayout()
        if (width > 0 && width > 0) {
            post(measureAndLayout)
        }
    }

    private val measureAndLayout = Runnable {
        measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
        layout(left, top, right, bottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (width > 0 && height > 0) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
            measureChildrenSelf()
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    private fun measureChildrenSelf() {
        when (childCount) {
            1 -> {
                measureView(getChildAt(0), width, height)
            }

            2 -> {
                measureView(getChildAt(0), width / 2, height)
                measureView(getChildAt(1), width / 2, height)
            }

            3 -> {
                measureView(getChildAt(0), width / 2, height)
                measureView(getChildAt(1), width / 2, height / 2)
                measureView(getChildAt(2), width / 2, height / 2)
            }

            4 -> {
                for (i in 0 until childCount) {
                    measureView(getChildAt(i), width / 2, height / 2)
                }
            }
        }
    }

    private fun measureView(child: View, childWidth: Int, childHeight: Int) {
        child.measure(
            MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY)
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        when (childCount) {
            1 -> {
                layoutView(getChildAt(0), 0, 0)
            }

            2 -> {
                layoutView(getChildAt(0), 0, 0)
                layoutView(getChildAt(1), measuredWidth / 2, 0)
            }

            3 -> {
                layoutView(getChildAt(0), 0, 0)
                layoutView(getChildAt(1), measuredWidth / 2, 0)
                layoutView(getChildAt(2), measuredWidth / 2, measuredHeight / 2)
            }

            4 -> {
                layoutView(getChildAt(0), 0, 0)
                layoutView(getChildAt(1), measuredWidth / 2, 0)
                layoutView(getChildAt(2), measuredWidth / 2, measuredHeight / 2)
                layoutView(getChildAt(3), 0, measuredHeight / 2)
            }
        }
    }

    private fun layoutView(child: View, left: Int, top: Int) {
        child.layout(left, top, left + child.measuredWidth, top + child.measuredHeight)
    }
}