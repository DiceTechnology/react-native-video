package com.brentvatne.exoplayer

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.children
import com.brentvatne.react.R
import com.facebook.react.modules.i18nmanager.I18nUtil

class MultiViewLayout(context: Context) : FrameLayout(context), MultiViewControlBar.MultiViewControlBarListener {

    private val pipModeWindowScaleToParent = 3
    private val isRTL = I18nUtil.getInstance().isRTL(context)
    private val childSizeRatio: Float = 16f / 9f // width / height
    private var swapChildViewPlayer = false
    var gap: Int = 30 // pix
    var multiViewMode = false
    var fullscreenMode = false
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
            }
        }
    var pictureInPictureMode = false
        set(value) {
            if (field != value) {
                field = value
                if (swapChildViewPlayer) {
                    swapPipView()
                }
                requestLayout()
                resetPIPViews(value)
            }
        }

    private fun resetPIPViews(pipMode: Boolean) {
        // pip mode, hide focus indicator foreground image
        children.forEach { view ->
            (view as MultiViewStateView).apply {
                isFocusable = !pipMode
                showVolumeIcon(!pipMode)
            }
        }
        // fullscreen player should always has audio. the pip one mute.
        if (pipMode) {
            (getChildAt(0) as MultiViewStateView).mute(false)
            (getChildAt(1) as MultiViewStateView).mute(true)
        } else {
            getChildByPositionTag(0).mute(false)
            getChildByPositionTag(1).mute(true)
        }
    }

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
        if (!multiViewMode || width <= 0 || height <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            setMeasuredDimension(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec)
            )
            val itemSpace = if (!fullscreenMode) gap else 0
            measureChildrenSelf(measuredWidth, measuredHeight, itemSpace)
        }
    }

    private fun measureChildrenSelf(
        parentMaxWidth: Int,
        parentMaxHeight: Int,
        itemSpace: Int,
    ) {
        when (childCount) {
            1 -> {
                measureView(
                    child = getChildByPositionTag(0),
                    childWidth = parentMaxWidth - itemSpace * 2,
                    childHeight = parentMaxHeight - itemSpace * 2
                )
            }

            2 -> {
                if (pictureInPictureMode) {
                    measureView(
                        child = getChildAt(0),
                        childWidth = parentMaxWidth,
                        childHeight = parentMaxHeight
                    )
                    measureView(
                        child = getChildAt(1),
                        childWidth = parentMaxWidth / pipModeWindowScaleToParent,
                        childHeight = parentMaxHeight / pipModeWindowScaleToParent
                    )
                } else {
                    measureView(
                        child = getChildByPositionTag(0),
                        childWidth = (parentMaxWidth - itemSpace * 3) / 2,
                        childHeight = parentMaxHeight - itemSpace * 2
                    )
                    measureView(
                        child = getChildByPositionTag(1),
                        childWidth = (parentMaxWidth - itemSpace * 3) / 2,
                        childHeight = parentMaxHeight - itemSpace * 2
                    )
                }
            }

            3 -> {
                measureView(
                    child = getChildByPositionTag(0),
                    childWidth = (parentMaxWidth - itemSpace * 3) * 2 / 3,
                    childHeight = parentMaxHeight - itemSpace * 2
                )
                measureView(
                    child = getChildByPositionTag(1),
                    childWidth = (parentMaxWidth - itemSpace * 3) * 1 / 3,
                    childHeight = (getChildByPositionTag(0).measuredHeight - itemSpace) / 2
                )
                measureView(
                    child = getChildByPositionTag(2),
                    childWidth = (parentMaxWidth - itemSpace * 3) * 1 / 3,
                    childHeight = (getChildByPositionTag(0).measuredHeight - itemSpace) / 2
                )
            }

            4 -> {
                for (i in 0 until childCount) {
                    measureView(
                        child = getChildByPositionTag(i),
                        childWidth = (parentMaxWidth - itemSpace * 3) / 2,
                        childHeight = (parentMaxHeight - itemSpace * 3) / 2
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
        val itemSpace = if (!fullscreenMode) gap else 0
        when (childCount) {
            1 -> {
                layoutViewInCenterByOffset(
                    child = getChildByPositionTag(0),
                    offsetX = -getChildByPositionTag(0).measuredWidth / 2,
                    offsetY = -getChildByPositionTag(0).measuredHeight / 2
                )
            }

            2 -> {
                // primary view size match as parent, secondary view locate in left|bottom or right|bottom.
                if (pictureInPictureMode) {
                    layoutViewByPosition(
                        child = getChildAt(0),
                        left = 0,
                        top = 0
                    )
                    val pipWindowMargin2Parent = getChildAt(1).measuredWidth / 8
                    if (isRTL) {
                        layoutViewByPosition(
                            child = getChildAt(1),
                            left = pipWindowMargin2Parent,
                            top = bottom - pipWindowMargin2Parent - getChildAt(1).measuredHeight
                        )
                    } else {
                        layoutViewByPosition(
                            child = getChildAt(1),
                            left = right - pipWindowMargin2Parent - getChildAt(1).measuredWidth,
                            top = bottom - pipWindowMargin2Parent - getChildAt(1).measuredHeight
                        )
                    }
                } else {
                    if (isRTL) {
                        layoutViewInCenterByOffset(
                            child = getChildByPositionTag(0),
                            offsetX = itemSpace / 2,
                            offsetY = -getChildByPositionTag(0).measuredHeight / 2
                        )
                        layoutViewInCenterByOffset(
                            child = getChildByPositionTag(1),
                            offsetX = -(getChildByPositionTag(1).measuredWidth + itemSpace / 2),
                            offsetY = -getChildByPositionTag(1).measuredHeight / 2
                        )
                    } else {
                        layoutViewInCenterByOffset(
                            child = getChildByPositionTag(0),
                            offsetX = -(getChildByPositionTag(0).measuredWidth + itemSpace / 2),
                            offsetY = -getChildByPositionTag(0).measuredHeight / 2
                        )
                        layoutViewInCenterByOffset(
                            child = getChildByPositionTag(1),
                            offsetX = itemSpace / 2,
                            offsetY = -getChildByPositionTag(1).measuredHeight / 2
                        )
                    }
                }
            }

            3 -> {
                if (isRTL) {
                    layoutViewByPosition(
                        child = getChildByPositionTag(1),
                        left = itemSpace,
                        top = height / 2 - itemSpace / 2 - getChildByPositionTag(1).measuredHeight
                    )
                    layoutViewByPosition(
                        child = getChildByPositionTag(2),
                        left = itemSpace,
                        top = height / 2 + itemSpace / 2
                    )
                    layoutViewByPosition(
                        child = getChildByPositionTag(0),
                        left = itemSpace * 2 + getChildByPositionTag(2).measuredWidth,
                        top = height / 2 - getChildByPositionTag(0).measuredHeight / 2
                    )
                } else {
                    layoutViewByPosition(
                        child = getChildByPositionTag(0),
                        left = itemSpace,
                        top = height / 2 - getChildByPositionTag(0).measuredHeight / 2
                    )
                    layoutViewByPosition(
                        child = getChildByPositionTag(1),
                        left = itemSpace * 2 + getChildByPositionTag(0).measuredWidth,
                        top = height / 2 - itemSpace / 2 - getChildByPositionTag(1).measuredHeight
                    )
                    layoutViewByPosition(
                        child = getChildByPositionTag(2),
                        left = itemSpace * 2 + getChildByPositionTag(0).measuredWidth,
                        top = height / 2 + itemSpace / 2
                    )
                }
            }

            4 -> {
                if (isRTL) {
                    layoutViewInCenterByOffset(
                        getChildByPositionTag(0),
                        offsetX = itemSpace / 2,
                        offsetY = -(itemSpace / 2 + getChildByPositionTag(0).measuredHeight)
                    )
                    layoutViewInCenterByOffset(
                        getChildByPositionTag(1),
                        offsetX = -(itemSpace / 2 + getChildByPositionTag(3).measuredWidth),
                        offsetY = -(itemSpace / 2 + getChildByPositionTag(3).measuredHeight)
                    )
                    layoutViewInCenterByOffset(
                        getChildByPositionTag(2),
                        offsetX = itemSpace / 2,
                        offsetY = itemSpace / 2
                    )
                    layoutViewInCenterByOffset(
                        getChildByPositionTag(3),
                        offsetX = -(itemSpace / 2 + getChildByPositionTag(2).measuredWidth),
                        offsetY = itemSpace / 2
                    )

                } else {
                    layoutViewInCenterByOffset(
                        getChildByPositionTag(0),
                        offsetX = -itemSpace / 2 - getChildByPositionTag(0).measuredWidth,
                        offsetY = -(itemSpace / 2 + getChildByPositionTag(0).measuredHeight)
                    )
                    layoutViewInCenterByOffset(
                        getChildByPositionTag(1),
                        offsetX = itemSpace / 2,
                        offsetY = -itemSpace / 2 - getChildByPositionTag(1).measuredHeight
                    )
                    layoutViewInCenterByOffset(
                        getChildByPositionTag(2),
                        offsetX = -(itemSpace / 2 + getChildByPositionTag(2).measuredWidth),
                        offsetY = itemSpace / 2
                    )
                    layoutViewInCenterByOffset(
                        getChildByPositionTag(3),
                        offsetX = itemSpace / 2,
                        offsetY = itemSpace / 2
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

    override fun addView(child: View) {
        child.setTagPosition(childCount)
        super.addView(child)
    }

    private fun getChildByPositionTag(position: Int): MultiViewStateView {
        return (children.find { it.getTagPosition() == position } ?: getChildAt(position)) as MultiViewStateView
    }

    override fun removeView(view: View) {
        val position = view.getTagPosition()
        super.removeView(view)
        children.forEach {
            if (it.getTagPosition() > position) {
                it.setTagPosition(it.getTagPosition() - 1)
            }
        }
    }

    // ----------------- MultiViewControlBar.MultiViewControlBarListener -----------------
    override fun onMultiviewIndicatorClick() {
        if (pictureInPictureMode) {
            pictureInPictureMode = false
        }
    }

    override fun onMultiviewPipButtonClicked() {
        pictureInPictureMode = true
    }

    override fun onMultiviewSwapButtonClicked() {
        if (childCount == 4) {
            val tag0 = getChildByPositionTag(0)
            val tag1 = getChildByPositionTag(1)
            val tag2 = getChildByPositionTag(2)
            val tag3 = getChildByPositionTag(3)
            tag0.setTagPosition(1)
            tag1.setTagPosition(3)
            tag2.setTagPosition(0)
            tag3.setTagPosition(2)
        } else {
            if (childCount == 2) {
                swapChildViewPlayer = !swapChildViewPlayer
            }
            children.forEach {
                it.setTagPosition((it.getTagPosition() + 1) % childCount)
            }
        }
        if (pictureInPictureMode) {
            swapPipView()
            return
        }
        requestLayout()
    }

    private fun swapPipView() {
        val primaryView = getChildAt(0) as MultiViewStateView
        val secondaryView = getChildAt(1) as MultiViewStateView
        swapView(primaryView, secondaryView)
    }

    override fun onMultiviewControlBarVisibleChanged(visible: Boolean) {
        children.forEach {
            (it as MultiViewStateView).showFocusUI(visible)
        }
    }
}

internal fun View.getTagPosition(): Int {
    return getTag(R.id.multiview_position_tag) as Int
}

internal fun View.setTagPosition(position: Int) {
    setTag(R.id.multiview_position_tag, position)
}
