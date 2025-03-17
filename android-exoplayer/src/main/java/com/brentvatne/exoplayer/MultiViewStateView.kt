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
import com.diceplatform.doris.ui.entity.LabelsTranslation
import com.facebook.react.modules.i18nmanager.I18nUtil

@SuppressLint("ViewConstructor")
class MultiViewStateView(
    private val tvExoplayerView: ReactTVExoplayerView,
    labelsTranslation: LabelsTranslation?,
    focusable: Boolean,
    private val listener: OnVolumeChangedListener,
) : FrameLayout(tvExoplayerView.context), OnFocusChangeListener, OnClickListener,
    DorisPlayerOutput {

    interface OnVolumeChangedListener {
        fun onRequestVolume(view: MultiViewStateView)
    }

    private val isRTL = I18nUtil.getInstance().isRTL(context)
    private val iconSize = (24 * tvExoplayerView.resources.displayMetrics.density).toInt()
    val volumeIcon: ImageView = ImageView(tvExoplayerView.context)
    val errorMsgView: TextView = TextView(tvExoplayerView.context)
    private val isMute: Boolean
        get() = tvExoplayerView.exoDorisPlayerView.isMute
    private val isError: Boolean
        get() = errorMsgView.isVisible

    init {
        layoutDirection = if (isRTL) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
        isFocusable = focusable
        foreground = ResourcesCompat.getDrawable(tvExoplayerView.resources, R.drawable.ic_item_focus_selector, null)
        // add player view
        addView(tvExoplayerView)

        // add volume icon
        resetVolumeIcon()
        volumeIcon.visibility = View.INVISIBLE
        volumeIcon.isFocusable = false
        volumeIcon.onFocusChangeListener = this
        volumeIcon.setOnClickListener(this)
        addView(
            volumeIcon,
            LayoutParams(iconSize, iconSize).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = 32
                bottomMargin = 32
                leftMargin = 32
                rightMargin = 32
            }
        )

        // add error msg view
        errorMsgView.apply {
            setTextAppearance(tvExoplayerView.context, R.style.TextAppearance_MultiviewErrorMsg)
            text = labelsTranslation?.get("multiViewPlaybackError")
            gravity = Gravity.CENTER
            visibility = View.GONE
            setBackgroundColor(Color.BLACK)
            setPadding(16)
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
        if (event.event == DorisPlayerEvent.Event.ERROR) {
            setError(true)
        }
    }

    fun setError(error: Boolean) {
        errorMsgView.visibility = if (error) View.VISIBLE else View.GONE
    }

    override fun onClick(v: View) {
        listener.onRequestVolume(this)
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        isSelected = hasFocus
    }

    fun showVolumeIcon(show: Boolean) {
        isFocusable = !show
        volumeIcon.visibility = if (show) View.VISIBLE else View.INVISIBLE
        volumeIcon.isFocusable = show
    }

    fun mute(mute: Boolean) {
        tvExoplayerView.mute(mute)
        resetVolumeIcon()
    }

    fun swap(otherView: MultiViewStateView) {
        // check error state first
        val otherViewIsError = otherView.isError
        otherView.setError(isError)
        this.setError(otherViewIsError)
        // swap player
        val tag0PlayerView = (this.getChildAt(0) as ReactTVExoplayerView).exoDorisPlayerView
        val tag1PlayerView = (otherView.getChildAt(0) as ReactTVExoplayerView).exoDorisPlayerView
        val player = tag0PlayerView.player
        tag0PlayerView.player = null
        tag0PlayerView.player = tag1PlayerView.player
        tag0PlayerView.mute(false)
        tag1PlayerView.player = null
        tag1PlayerView.player = player
        tag1PlayerView.mute(true)
    }

    private fun resetVolumeIcon() {
        volumeIcon.setImageResource(
            if (isMute)
                R.drawable.ic_multiview_mute_selector
            else
                R.drawable.ic_multiview_volume_selector
        )
    }
}
