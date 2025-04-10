package com.brentvatne.exoplayer

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.children
import com.brentvatne.exoplayer.MultiViewLayout.MultiViewMode.FULLSCREEN
import com.brentvatne.exoplayer.MultiViewLayout.MultiViewMode.MULTIVIEW
import com.brentvatne.exoplayer.MultiViewLayout.MultiViewMode.NORMAL
import com.brentvatne.exoplayer.MultiViewLayout.MultiViewMode.PICTURE_IN_PICTURE
import com.brentvatne.react.R
import com.diceplatform.doris.custom.utils.ScreenUtils
import com.facebook.react.modules.i18nmanager.I18nUtil

class MultiViewLayout(context: Context) : FrameLayout(context), MultiViewControlBar.MultiViewControlBarListener {

    companion object {
        private const val PIP_MODE_WINDOW_SCALE_TO_PARENT = 3
        private const val CHILD_SIZE_RATIO = 16f / 9f       // width / height
        private const val DEFAULT_MULTIVIEW_LAYOUT_GAP = 16f // dp
    }

    enum class MultiViewMode {
        NORMAL,
        MULTIVIEW,
        FULLSCREEN,
        PICTURE_IN_PICTURE
    }

    private val isRTL = I18nUtil.getInstance().isRTL(context)
    private val multiViewLayoutGap: Int = ScreenUtils.convertDpToPixel(context, DEFAULT_MULTIVIEW_LAYOUT_GAP)
    private var swapChildViewPlayer = false
    private var pipViewBottomMargin: Int = 0
    var mode: MultiViewMode = NORMAL
        set(value) {
            if (field != value) {
                val oldMode = field
                field = value
                resetMode(value, oldMode)
            }
        }
    val isMultiViewMode: Boolean
        get() = mode == MULTIVIEW || mode == FULLSCREEN || mode == PICTURE_IN_PICTURE
    val isFullscreenMode: Boolean
        get() = mode == FULLSCREEN
    val isPictureInPictureMode: Boolean
        get() = mode == PICTURE_IN_PICTURE

    private fun resetMode(currentMode: MultiViewMode, oldMode: MultiViewMode) {
        if (currentMode == PICTURE_IN_PICTURE || oldMode == PICTURE_IN_PICTURE) {
            if (swapChildViewPlayer) {
                swapPipView()
            }
            requestLayout()
            resetPipViews(currentMode == PICTURE_IN_PICTURE)
        } else if (oldMode != NORMAL) {
            requestLayout()
        }
    }

    private fun resetPipViews(pipMode: Boolean) {
        // pip mode, hide focus indicator foreground image
        children.forEach { view ->
            (view as MultiViewStateView).apply {
                isFocusable = !pipMode
                setVolumeIconVisible(!pipMode)
                setVolumeIconFocusable(!pipMode, !pipMode)
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
        if (isMultiViewMode && width > 0 && height > 0) {
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
        if (mode == NORMAL || width <= 0 || height <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            setMeasuredDimension(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec)
            )
            val itemSpace = if (!isFullscreenMode) multiViewLayoutGap else 0
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
                if (isPictureInPictureMode) {
                    measureView(
                        child = getChildAt(0),
                        childWidth = parentMaxWidth,
                        childHeight = parentMaxHeight
                    )
                    measureView(
                        child = getChildAt(1),
                        childWidth = parentMaxWidth / PIP_MODE_WINDOW_SCALE_TO_PARENT,
                        childHeight = parentMaxHeight / PIP_MODE_WINDOW_SCALE_TO_PARENT
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
        if (childWidth * 1f / childHeight > CHILD_SIZE_RATIO) {
            adjustedWidth = (childHeight * CHILD_SIZE_RATIO).toInt()
        } else {
            adjustedHeight = (childWidth / CHILD_SIZE_RATIO).toInt()
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
        if (mode == NORMAL) {
            super.onLayout(changed, left, top, right, bottom)
            return
        }
        val itemSpace = if (!isFullscreenMode) multiViewLayoutGap else 0
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
                if (isPictureInPictureMode) {
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
                            top = bottom - pipWindowMargin2Parent - getChildAt(1).measuredHeight - pipViewBottomMargin
                        )
                    } else {
                        layoutViewByPosition(
                            child = getChildAt(1),
                            left = right - pipWindowMargin2Parent - getChildAt(1).measuredWidth,
                            top = bottom - pipWindowMargin2Parent - getChildAt(1).measuredHeight - pipViewBottomMargin
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

    fun setPipViewBottomMargin(margin: Int) {
        pipViewBottomMargin = margin
        if (isPictureInPictureMode) {
            requestLayout()
        }
    }

    // ----------------- MultiViewControlBar.MultiViewControlBarListener -----------------
    override fun onMultiviewIndicatorClick() {
        if (isPictureInPictureMode) {
            mode = FULLSCREEN
        }
    }

    override fun onMultiviewPipButtonClicked() {
        mode = PICTURE_IN_PICTURE
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
        if (isPictureInPictureMode) {
            swapPipView()
            return
        }
        requestLayout()
    }

    private fun swapPipView() {
        val primaryView = getChildAt(0) as MultiViewStateView
        val secondaryView = getChildAt(1) as MultiViewStateView
        swapView(primaryView, secondaryView, swapChildViewPlayer)
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
