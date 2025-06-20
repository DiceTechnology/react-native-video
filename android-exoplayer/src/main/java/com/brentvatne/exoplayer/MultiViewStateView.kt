package com.brentvatne.exoplayer

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnFocusChangeListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.brentvatne.react.R
import com.diceplatform.doris.DorisPlayerOutput
import com.diceplatform.doris.entity.DorisPlayerEvent
import com.diceplatform.doris.entity.State
import com.diceplatform.doris.ui.entity.LabelsTranslation
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.i18nmanager.I18nUtil

@SuppressLint("ViewConstructor")
class MultiViewStateView(
    private val tvExoplayerView: ReactTVExoplayerView,
    labelsTranslation: LabelsTranslation?,
    focusable: Boolean,
    private val listener: OnVolumeChangedListener,
) : FrameLayout(tvExoplayerView.context), OnFocusChangeListener, OnClickListener,
    DorisPlayerOutput {

    companion object {
        private const val ICON_SIZE_DP = 50f
        private const val ICON_MARGIN_DP = 10f
        private const val ERROR_MSG_PADDING_DP = 16f
    }

    interface OnVolumeChangedListener {
        fun onRequestMute(view: MultiViewStateView, mute: Boolean)
    }


    private val isRTL = I18nUtil.getInstance().isRTL(context)
    private val iconSize = dp(ICON_SIZE_DP)
    private val volumeIcon: ImageView = ImageView(tvExoplayerView.context)
    private val errorMsgView: TextView = TextView(tvExoplayerView.context)
    private var stateViewFocusable = focusable
    private val isMute: Boolean
        get() = tvExoplayerView.exoDorisPlayerView.isMute
    val isError: Boolean
        get() = errorMsgView.isVisible

    init {
        layoutDirection = if (isRTL) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
        isFocusable = focusable
        foreground = ResourcesCompat.getDrawable(tvExoplayerView.resources, R.drawable.ic_item_focus_selector, null)
        // add player view
        addView(tvExoplayerView)

        // add volume icon
        resetVolumeIcon()
        volumeIcon.visibility = if (focusable) View.VISIBLE else View.INVISIBLE
        volumeIcon.isFocusable = false
        volumeIcon.onFocusChangeListener = this
        volumeIcon.setBackgroundResource(R.drawable.ic_multiview_circle_bg)
        volumeIcon.setOnClickListener(this)
        volumeIcon.scaleType = ImageView.ScaleType.CENTER_INSIDE
        addView(
            volumeIcon,
            LayoutParams(iconSize, iconSize).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = dp(ICON_MARGIN_DP)
                bottomMargin = dp(ICON_MARGIN_DP)
                leftMargin = dp(ICON_MARGIN_DP)
                rightMargin = dp(ICON_MARGIN_DP)
            }
        )

        // add error msg view
        errorMsgView.apply {
            setTextAppearance(tvExoplayerView.context, R.style.TextAppearance_MultiviewErrorMsg)
            text = labelsTranslation?.get("multiViewPlaybackError")
            gravity = Gravity.CENTER
            visibility = View.GONE
            setBackgroundColor(Color.BLACK)
            setPadding(dp(ERROR_MSG_PADDING_DP))
        }
        addView(
            errorMsgView, LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            ).apply {
                gravity = Gravity.CENTER
            }
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        tvExoplayerView.exoDorisPlayerView.setDorisPlayerOutput(this)
    }

    override fun onDetachedFromWindow() {
        tvExoplayerView.exoDorisPlayerView.setDorisPlayerOutput(null)
        super.onDetachedFromWindow()
    }

    override fun onPlayerEvent(event: DorisPlayerEvent) {
        if (tvExoplayerView.exoDorisPlayerView.isMultipleViewMode) {
            if (event is DorisPlayerEvent.Error
                || (event is DorisPlayerEvent.StateChanged && event.state == State.FAILED)
            ) {
                showErrorView(true)
            } else if (event == DorisPlayerEvent.ReloadWithDrmL3) {
                // multiview not support reload with drm l3, so show error message
                showErrorView(true)
                (tvExoplayerView.tag as? ReadableMap)?.let { src ->
                    src.getString("id")?.let { id ->
                        tvExoplayerView.eventEmitter.error(
                            id,
                            "RELOAD_WITH_DRM_L3 error",
                            Exception("RELOAD_WITH_DRM_L3 error")
                        )
                    }
                }
            }
        }
    }

    fun showErrorView(error: Boolean) {
        errorMsgView.visibility = if (error) View.VISIBLE else View.GONE
    }

    override fun onClick(v: View) {
        if (isError || !volumeIcon.isVisible) return
        listener.onRequestMute(this, !tvExoplayerView.exoDorisPlayerView.isMute)
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        isSelected = hasFocus
    }

    fun setVolumeIconVisible(show: Boolean) {
        volumeIcon.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    fun setVolumeIconFocusable(focusable: Boolean, parentFocusable: Boolean = true) {
        volumeIcon.isFocusable = focusable
        if (focusable) {
            this@MultiViewStateView.isFocusable = false
        } else {
            this@MultiViewStateView.isFocusable = stateViewFocusable && parentFocusable
            this.isSelected = false
        }
    }

    fun mute(mute: Boolean) {
        tvExoplayerView.mute(mute)
        resetVolumeIcon()
    }

    private fun resetVolumeIcon() {
        volumeIcon.setImageResource(
            if (isMute)
                R.drawable.ic_multiview_mute_selector
            else
                R.drawable.ic_multiview_volume_selector
        )
    }

    fun showFocusUI(show: Boolean) {
        if (volumeIcon.isFocusable) {
            // show focus UI for volume icon only, conflict with pip mode. so only change it when it's focusable.
            volumeIcon.visibility = if (show) View.VISIBLE else View.INVISIBLE
            if (!show) {
                this.isSelected = false
            }
        } else {
            this.isSelected = false
        }
    }
}

internal fun swapView(view1: MultiViewStateView, view2: MultiViewStateView, swapped: Boolean) {
    // check error state first
    val otherViewIsError = view2.isError
    view2.showErrorView(view1.isError)
    view1.showErrorView(otherViewIsError)
    // swap track max bitrate
    val tag1ReactView = view1.getChildAt(0) as ReactTVExoplayerView
    val tag2ReactView = view2.getChildAt(0) as ReactTVExoplayerView
    if (swapped) {
        tag1ReactView.onSizeChanged(tag2ReactView.measuredWidth, tag2ReactView.measuredHeight, 0, 0)
        tag2ReactView.onSizeChanged(tag1ReactView.measuredWidth, tag1ReactView.measuredHeight, 0, 0)
    } else {
        tag1ReactView.onSizeChanged(tag1ReactView.measuredWidth, tag1ReactView.measuredHeight, 0, 0)
        tag2ReactView.onSizeChanged(tag2ReactView.measuredWidth, tag2ReactView.measuredHeight, 0, 0)
    }

    // swap player
    val tag1PlayerView = tag1ReactView.exoDorisPlayerView
    val tag2PlayerView = tag2ReactView.exoDorisPlayerView
    val player = tag1PlayerView.player
    tag1PlayerView.player = null
    tag1PlayerView.player = tag2PlayerView.player
    tag1PlayerView.mute(false)
    tag2PlayerView.player = null
    tag2PlayerView.player = player
    tag2PlayerView.mute(true)
}
