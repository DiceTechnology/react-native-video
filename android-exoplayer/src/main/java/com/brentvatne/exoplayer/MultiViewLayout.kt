package com.brentvatne.exoplayer

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.core.view.isInvisible
import com.brentvatne.react.R
import com.diceplatform.doris.ui.ExoDorisTvPlayerView
import com.facebook.react.modules.i18nmanager.I18nUtil

class MultiViewLayout(context: Context) : FrameLayout(context), MultiViewControlBar.MultiViewControlBarListener {

    private val isRTL = I18nUtil.getInstance().isRTL(context)
    private val childSizeRatio: Float = 16f / 9f // width / height
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
                requestLayout()
                resetPIP(value)
            }
        }

    private fun resetPIP(pipMode: Boolean) {
        // pip mode, hide focus indicator foreground image
        children.forEach { view ->
            (view as MultiViewFocusableView).apply {
                isFocusable = !pipMode
                volumeIcon.isInvisible = pipMode
            }
        }
        // fullscreen player should always has audio. the pip one mute.
        getChildByPositionTag(0).mute(false)
        getChildByPositionTag(1).mute(true)
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
            measureChildrenSelf(width, height, itemSpace)
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
                measureView(
                    child = getChildByPositionTag(0),
                    childWidth = if (pictureInPictureMode) parentMaxWidth else (parentMaxWidth - itemSpace * 3) / 2,
                    childHeight = if (pictureInPictureMode) parentMaxHeight else parentMaxHeight - itemSpace * 2
                )
                measureView(
                    child = getChildByPositionTag(1),
                    childWidth = if (pictureInPictureMode) parentMaxWidth / 4 else (parentMaxWidth - itemSpace * 3) / 2,
                    childHeight = if (pictureInPictureMode) parentMaxHeight / 4 else parentMaxHeight - itemSpace * 2
                )
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
                        child = getChildByPositionTag(0),
                        left = 0,
                        top = 0
                    )
                    if (isRTL) {
                        layoutViewByPosition(
                            child = getChildByPositionTag(1),
                            left = right - gap - getChildByPositionTag(1).measuredWidth,
                            top = bottom - gap - getChildByPositionTag(1).measuredHeight
                        )
                    } else {
                        layoutViewByPosition(
                            child = getChildByPositionTag(1),
                            left = gap,
                            top = bottom - gap - getChildByPositionTag(1).measuredHeight
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

    private fun getChildByPositionTag(position: Int): MultiViewFocusableView {
        return (children.find { it.getTagPosition() == position } ?: getChildAt(position)) as MultiViewFocusableView
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
    override fun onMultiviewPipButtonClicked() {
        pictureInPictureMode = true
    }

    override fun onMultiviewSwapButtonClicked() {
        if (pictureInPictureMode) {
//            val tag0 = getChildByPositionTag(0)
//            val tag1 = getChildByPositionTag(1)
            val tag0PlayerView = getDorisTvExoplayerView(0)
            val tag1PlayerView = getDorisTvExoplayerView(1)
            val player = tag0PlayerView.player
            tag0PlayerView.player = null
            tag0PlayerView.player = tag1PlayerView.player
            tag0PlayerView.mute(false)
            tag1PlayerView.player = null
            tag1PlayerView.player = player
            tag1PlayerView.mute(true)
//            tag0.setTagPosition(1)
//            tag1.setTagPosition(0)
        } else {
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
                children.forEach {
                    it.setTagPosition((it.getTagPosition() + 1) % childCount)
                }
            }
            requestLayout()
        }
    }

    private fun getDorisTvExoplayerView(position: Int): ExoDorisTvPlayerView {
        return (getChildByPositionTag(position).getChildAt(0) as ReactTVExoplayerView).exoDorisPlayerView
    }
}

internal fun View.getTagPosition(): Int {
    return getTag(R.id.multiview_position_tag) as Int
}

internal fun View.setTagPosition(position: Int) {
    setTag(R.id.multiview_position_tag, position)
}