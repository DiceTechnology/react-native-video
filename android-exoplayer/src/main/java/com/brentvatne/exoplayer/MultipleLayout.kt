package com.brentvatne.exoplayer

import android.content.Context
import android.view.View
import android.widget.FrameLayout

class MultipleLayout(context: Context) : FrameLayout(context) {

    override fun addView(child: View) {
        if (childCount >= 4) {
            throw IllegalStateException("Cannot add more than 4 child views")
        }
        super.addView(child)
        resetChildLayout()
    }

    override fun removeView(view: View) {
        super.removeView(view)
        resetChildLayout()
    }

    private fun resetChildLayout() {
        when (childCount) {
            1 -> {
                setViewMatchParent(getChildAt(0))
            }

            2 -> {
                setViewHalfParent(getChildAt(0), true)
                setViewHalfParent(getChildAt(1), false)
            }

            3 -> {
                setViewHalfParent(getChildAt(0), true)
                setViewRightTopCorner(getChildAt(1))
                setViewRightBottomCorner(getChildAt(2))
            }

            4 -> {
                setViewLeftTopCorner(getChildAt(0))
                setViewRightTopCorner(getChildAt(1))
                setViewLeftBottomCorner(getChildAt(2))
                setViewRightBottomCorner(getChildAt(3))
            }
        }
    }

    private fun setViewMatchParent(view: View) {
        manuallyLayoutChild(
            view,
            left = 0,
            top = 0,
            width = measuredWidth,
            height = measuredHeight,
        )
    }

    private fun setViewHalfParent(view: View, isLeft: Boolean) {
        if (isLeft) {
            manuallyLayoutChild(
                view,
                left = 0,
                top = 0,
                width = measuredWidth / 2,
                height = measuredHeight,
            )
        } else {
            manuallyLayoutChild(
                view,
                left = measuredWidth / 2,
                top = 0,
                width = measuredWidth / 2,
                height = measuredHeight,
            )
        }
    }

    private fun setViewLeftTopCorner(view: View) {
        manuallyLayoutChild(
            view,
            left = 0,
            top = 0,
            width = measuredWidth / 2,
            height = measuredHeight / 2,
        )
    }

    private fun setViewLeftBottomCorner(view: View) {
        manuallyLayoutChild(
            view,
            left = 0,
            top = measuredHeight / 2,
            width = measuredWidth / 2,
            height = measuredHeight / 2,
        )
    }

    private fun setViewRightTopCorner(view: View) {
        manuallyLayoutChild(
            view,
            left = measuredWidth / 2,
            top = 0,
            width = measuredWidth / 2,
            height = measuredHeight / 2,
        )
    }

    private fun setViewRightBottomCorner(view: View) {
        manuallyLayoutChild(
            view,
            left = measuredWidth / 2,
            top = measuredHeight / 2,
            width = measuredWidth / 2,
            height = measuredHeight / 2,
        )
    }

    private fun manuallyLayoutChild(
        child: View,
        left: Int,
        top: Int,
        width: Int,
        height: Int,
    ) {
        child.left = left
        child.top = top
        child.right = left + width
        child.bottom = top + height
        child.measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
        child.requestLayout()
    }


}