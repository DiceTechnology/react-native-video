package com.brentvatne.exoplayer

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.facebook.react.modules.i18nmanager.I18nUtil

class MultiViewLayout(context: Context) : FrameLayout(context), MultiViewControlBar.MultiViewControlBarListener {

    init {
        isChildrenDrawingOrderEnabled = true
    }

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
            }
        }
    private var layoutSwapIndex = 0

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
                    child = getChildAt(0),
                    childWidth = parentMaxWidth - itemSpace * 2,
                    childHeight = parentMaxHeight - itemSpace * 2
                )
            }

            2 -> {
                measureView(
                    child = getChildAt(0),
                    childWidth = if (pictureInPictureMode) parentMaxWidth else (parentMaxWidth - itemSpace * 3) / 2,
                    childHeight = if (pictureInPictureMode) parentMaxHeight else parentMaxHeight - itemSpace * 2
                )
                measureView(
                    child = getChildAt(1),
                    childWidth = if (pictureInPictureMode) parentMaxWidth / 4 else (parentMaxWidth - itemSpace * 3) / 2,
                    childHeight = if (pictureInPictureMode) parentMaxHeight / 4 else parentMaxHeight - itemSpace * 2
                )
//                if (pictureInPictureMode) {
//                    val videoSurfaceView = (getChildAt(1) as ReactTVExoplayerView).exoDorisPlayerView.videoSurfaceView
//                    Log.d("MultiViewLayout", "videoSurfaceView: ${videoSurfaceView?.javaClass?.simpleName}")
//                    if (videoSurfaceView is SurfaceView) {
//                        videoSurfaceView.setZOrderOnTop(true)
//                    } else {
//                        getChildAt(1).bringToFront()
//                    }
//                }
            }

            3 -> {
                measureView(
                    child = getChildAt(0),
                    childWidth = (parentMaxWidth - itemSpace * 3) * 2 / 3,
                    childHeight = parentMaxHeight - itemSpace * 2
                )
                measureView(
                    child = getChildAt(1),
                    childWidth = getChildAt(0).measuredWidth / 2,
                    childHeight = (getChildAt(0).measuredHeight - itemSpace) / 2
                )
                measureView(
                    child = getChildAt(2),
                    childWidth = getChildAt(0).measuredWidth / 2,
                    childHeight = (getChildAt(0).measuredHeight - itemSpace) / 2
                )
            }

            4 -> {
                for (i in 0 until childCount) {
                    measureView(
                        child = getChildAt(i),
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
                    child = getChildAt(0),
                    offsetX = -getChildAt(0).measuredWidth / 2,
                    offsetY = -getChildAt(0).measuredHeight / 2
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
                    if (isRTL) {
                        layoutViewByPosition(
                            child = getChildAt(1),
                            left = right - gap - getChildAt(1).measuredWidth,
                            top = bottom - gap - getChildAt(1).measuredHeight
                        )
                    } else {
                        layoutViewByPosition(
                            child = getChildAt(1),
                            left = gap,
                            top = bottom - gap - getChildAt(1).measuredHeight
                        )
                    }
                } else {
                    if (isRTL) {
                        layoutViewInCenterByOffset(
                            child = getChildAt(0),
                            offsetX = itemSpace / 2,
                            offsetY = -getChildAt(0).measuredHeight / 2
                        )
                        layoutViewInCenterByOffset(
                            child = getChildAt(1),
                            offsetX = -(getChildAt(1).measuredWidth + itemSpace / 2),
                            offsetY = -getChildAt(1).measuredHeight / 2
                        )
                    } else {
                        layoutViewInCenterByOffset(
                            child = getChildAt(0),
                            offsetX = -(getChildAt(0).measuredWidth + itemSpace / 2),
                            offsetY = -getChildAt(0).measuredHeight / 2
                        )
                        layoutViewInCenterByOffset(
                            child = getChildAt(1),
                            offsetX = itemSpace / 2,
                            offsetY = -getChildAt(1).measuredHeight / 2
                        )
                    }
                }
            }

            3 -> {
                if (isRTL) {
                    layoutViewByPosition(
                        child = getChildAt(1),
                        left = itemSpace,
                        top = height / 2 - itemSpace / 2 - getChildAt(1).measuredHeight
                    )
                    layoutViewByPosition(
                        child = getChildAt(2),
                        left = itemSpace,
                        top = height / 2 + itemSpace / 2
                    )
                    layoutViewByPosition(
                        child = getChildAt(0),
                        left = itemSpace * 2 + getChildAt(2).measuredWidth,
                        top = height / 2 - getChildAt(0).measuredHeight / 2
                    )
                } else {
                    layoutViewByPosition(
                        child = getChildAt(0),
                        left = itemSpace,
                        top = height / 2 - getChildAt(0).measuredHeight / 2
                    )
                    layoutViewByPosition(
                        child = getChildAt(1),
                        left = itemSpace * 2 + getChildAt(0).measuredWidth,
                        top = height / 2 - itemSpace / 2 - getChildAt(1).measuredHeight
                    )
                    layoutViewByPosition(
                        child = getChildAt(2),
                        left = itemSpace * 2 + getChildAt(0).measuredWidth,
                        top = height / 2 + itemSpace / 2
                    )
                }
            }

            4 -> {
                if (isRTL) {
                    layoutViewInCenterByOffset(
                        getChildAt(0),
                        offsetX = itemSpace / 2,
                        offsetY = -(itemSpace / 2 + getChildAt(0).measuredHeight)
                    )
                    layoutViewInCenterByOffset(
                        getChildAt(1),
                        offsetX = -(itemSpace / 2 + getChildAt(3).measuredWidth),
                        offsetY = -(itemSpace / 2 + getChildAt(3).measuredHeight)
                    )
                    layoutViewInCenterByOffset(
                        getChildAt(2),
                        offsetX = -(itemSpace / 2 + getChildAt(2).measuredWidth),
                        offsetY = itemSpace / 2
                    )
                    layoutViewInCenterByOffset(
                        getChildAt(3),
                        offsetX = itemSpace / 2,
                        offsetY = itemSpace / 2
                    )

                } else {
                    layoutViewInCenterByOffset(
                        getChildAt(0),
                        offsetX = -itemSpace / 2 - getChildAt(0).measuredWidth,
                        offsetY = -(itemSpace / 2 + getChildAt(0).measuredHeight)
                    )
                    layoutViewInCenterByOffset(
                        getChildAt(1),
                        offsetX = itemSpace / 2,
                        offsetY = -itemSpace / 2 - getChildAt(1).measuredHeight
                    )
                    layoutViewInCenterByOffset(
                        getChildAt(2),
                        offsetX = itemSpace / 2,
                        offsetY = itemSpace / 2
                    )
                    layoutViewInCenterByOffset(
                        getChildAt(3),
                        offsetX = -(itemSpace / 2 + getChildAt(2).measuredWidth),
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

    override fun getChildAt(index: Int): View {
        return super.getChildAt((index + layoutSwapIndex) % childCount)
    }

    override fun getChildDrawingOrder(childCount: Int, drawingPosition: Int): Int {
//        if (pictureInPictureMode && childCount == 2) {
//            if (drawingPosition == 0) {
//                return layoutSwapIndex % 2
//            } else if (drawingPosition == 1) {
//                return (layoutSwapIndex + 1) % 2
//            }
//        }
        return super.getChildDrawingOrder(childCount, drawingPosition)
    }

    // ----------------- MultiViewControlBar.MultiViewControlBarListener -----------------
    override fun onMultiviewPipButtonClicked() {
        pictureInPictureMode = true
    }

    override fun onMultiviewSwapButtonClicked() {
        layoutSwapIndex++
        requestLayout()
    }
}