package com.brentvatne.exoplayer

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.facebook.react.modules.i18nmanager.I18nUtil

class MultiViewLayout(context: Context) : FrameLayout(context) {

    private val isRTL = I18nUtil.getInstance().isRTL(context)
    private val childSizeRatio: Float = 16f / 9f // width / height
    var divider: Int = 30 // pix
    var multiViewMode = false

    override fun requestLayout() {
        super.requestLayout()
        if (multiViewMode && width > 0 && height > 0) {
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

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        if (multiViewMode && width > 0 && height > 0) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
            measureChildrenSelf(width, height, divider)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    private fun measureChildrenSelf(
        parentMaxWidth: Int,
        parentMaxHeight: Int,
        borderSpace: Int,
    ) {
        when (childCount) {
            1 -> {
                measureView(
                    child = getChildAt(0),
                    childWidth = parentMaxWidth - borderSpace * 2,
                    childHeight = parentMaxHeight - borderSpace * 2
                )
            }

            2 -> {
                measureView(
                    child = getChildAt(0),
                    childWidth = (parentMaxWidth - borderSpace * 3) / 2,
                    childHeight = parentMaxHeight - borderSpace * 2
                )
                measureView(
                    child = getChildAt(1),
                    childWidth = (parentMaxWidth - borderSpace * 3) / 2,
                    childHeight = parentMaxHeight - borderSpace * 2
                )
            }

            3 -> {
                measureView(
                    child = getChildAt(0),
                    childWidth = (parentMaxWidth - borderSpace * 3) * 2 / 3,
                    childHeight = parentMaxHeight - borderSpace * 2
                )
                measureView(
                    child = getChildAt(1),
                    childWidth = getChildAt(0).measuredWidth / 2,
                    childHeight = (getChildAt(0).measuredHeight - borderSpace) / 2
                )
                measureView(
                    child = getChildAt(2),
                    childWidth = getChildAt(0).measuredWidth / 2,
                    childHeight = (getChildAt(0).measuredHeight - borderSpace) / 2
                )
            }

            4 -> {
                for (i in 0 until childCount) {
                    measureView(
                        child = getChildAt(i),
                        childWidth = (parentMaxWidth - borderSpace * 3) / 2,
                        childHeight = (parentMaxHeight - borderSpace * 3) / 2
                    )
                }
            }
        }
    }

    private fun measureView(
        child: View,
        childWidth: Int,
        childHeight: Int,
    ) {
        var adjustedWidth = childWidth
        var adjustedHeight = childHeight
        if (childWidth * 1f / childHeight > childSizeRatio) {
            adjustedWidth = (childHeight * childSizeRatio).toInt()
        } else {
            adjustedHeight = (childWidth / childSizeRatio).toInt()
        }
        child.measure(
            MeasureSpec.makeMeasureSpec(adjustedWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(adjustedHeight, MeasureSpec.EXACTLY)
        )
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ) {
        if (!multiViewMode) {
            super.onLayout(changed, left, top, right, bottom)
            return
        }
        when (childCount) {
            1 -> {
                layoutViewInCenterByOffset(
                    child = getChildAt(0),
                    offsetX = -getChildAt(0).measuredWidth / 2,
                    offsetY = -getChildAt(0).measuredHeight / 2
                )
            }

            2 -> {
                if (isRTL) {
                    layoutViewInCenterByOffset(
                        child = getChildAt(0),
                        offsetX = divider / 2,
                        offsetY = -getChildAt(0).measuredHeight / 2
                    )
                    layoutViewInCenterByOffset(
                        child = getChildAt(1),
                        offsetX = -(getChildAt(1).measuredWidth + divider / 2),
                        offsetY = -getChildAt(1).measuredHeight / 2
                    )
                } else {
                    layoutViewInCenterByOffset(
                        child = getChildAt(0),
                        offsetX = -(getChildAt(0).measuredWidth + divider / 2),
                        offsetY = -getChildAt(0).measuredHeight / 2
                    )
                    layoutViewInCenterByOffset(
                        child = getChildAt(1),
                        offsetX = divider / 2,
                        offsetY = -getChildAt(1).measuredHeight / 2
                    )
                }
            }

            3 -> {
                if (isRTL) {
                    layoutViewByPosition(
                        child = getChildAt(1),
                        left = divider,
                        top = height / 2 - divider / 2 - getChildAt(1).measuredHeight
                    )
                    layoutViewByPosition(
                        child = getChildAt(2),
                        left = divider,
                        top = height / 2 + divider / 2
                    )
                    layoutViewByPosition(
                        child = getChildAt(0),
                        left = divider * 2 + getChildAt(2).measuredWidth,
                        top = height / 2 - getChildAt(0).measuredHeight / 2
                    )
                } else {
                    layoutViewByPosition(
                        child = getChildAt(0),
                        left = divider,
                        top = height / 2 - getChildAt(0).measuredHeight / 2
                    )
                    layoutViewByPosition(
                        child = getChildAt(1),
                        left = divider * 2 + getChildAt(0).measuredWidth,
                        top = height / 2 - divider / 2 - getChildAt(1).measuredHeight
                    )
                    layoutViewByPosition(
                        child = getChildAt(2),
                        left = divider * 2 + getChildAt(0).measuredWidth,
                        top = height / 2 + divider / 2
                    )
                }
            }

            4 -> {
                if (isRTL) {
                    layoutViewInCenterByOffset(
                        getChildAt(0),
                        offsetX = divider / 2,
                        offsetY = -(divider / 2 + getChildAt(0).measuredHeight)
                    )
                    layoutViewInCenterByOffset(
                        getChildAt(1),
                        offsetX = divider / 2,
                        offsetY = divider / 2
                    )
                    layoutViewInCenterByOffset(
                        getChildAt(2),
                        offsetX = -(divider / 2 + getChildAt(2).measuredWidth),
                        offsetY = divider / 2
                    )
                    layoutViewInCenterByOffset(
                        getChildAt(3),
                        offsetX = -(divider / 2 + getChildAt(3).measuredWidth),
                        offsetY = -(divider / 2 + getChildAt(3).measuredHeight)
                    )
                } else {
                    layoutViewInCenterByOffset(
                        getChildAt(0),
                        offsetX = -divider / 2 - getChildAt(0).measuredWidth,
                        offsetY = -(divider / 2 + getChildAt(0).measuredHeight)
                    )
                    layoutViewInCenterByOffset(
                        getChildAt(1),
                        offsetX = divider / 2,
                        offsetY = -divider / 2 - getChildAt(1).measuredHeight
                    )
                    layoutViewInCenterByOffset(
                        getChildAt(2),
                        offsetX = divider / 2,
                        offsetY = divider / 2
                    )
                    layoutViewInCenterByOffset(
                        getChildAt(3),
                        offsetX = -(divider / 2 + getChildAt(2).measuredWidth),
                        offsetY = divider / 2
                    )
                }
            }
        }
    }

    private fun layoutViewInCenterByOffset(
        child: View,
        offsetX: Int,
        offsetY: Int,
    ) {
        layoutViewByPosition(child, width / 2 + offsetX, height / 2 + offsetY)
    }

    private fun layoutViewByPosition(
        child: View,
        left: Int,
        top: Int,
    ) {
        child.layout(left, top, left + child.measuredWidth, top + child.measuredHeight)
    }
}