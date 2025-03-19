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

//TODO: ---- test code --------------------------------
var index = 0

@SuppressLint("ViewConstructor")
class MultiViewStateView(
    private val tvExoplayerView: ReactTVExoplayerView,
    labelsTranslation: LabelsTranslation?,
    focusable: Boolean,
    private val listener: OnVolumeChangedListener,
) : FrameLayout(tvExoplayerView.context), OnFocusChangeListener, OnClickListener,
    DorisPlayerOutput {

    interface OnVolumeChangedListener {
        fun onRequestMute(view: MultiViewStateView, mute: Boolean)
    }

    private val isRTL = I18nUtil.getInstance().isRTL(context)
    private val iconSize = (40 * tvExoplayerView.resources.displayMetrics.density).toInt()
    private val volumeIcon: ImageView = ImageView(tvExoplayerView.context)
    private val errorMsgView: TextView = TextView(tvExoplayerView.context)
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
        volumeIcon.visibility = View.INVISIBLE
        volumeIcon.isFocusable = false
        volumeIcon.onFocusChangeListener = this
        volumeIcon.setBackgroundResource(R.drawable.ic_multiview_circle_bg)
        volumeIcon.setOnClickListener(this)
        volumeIcon.setPadding(12)
        volumeIcon.scaleType = ImageView.ScaleType.CENTER_INSIDE
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

        //TODO: ---- test code --------------------------------
        // test indicator
        val textView = TextView(context)
        textView.setPadding(30, 20, 30, 20)
        textView.text = (index++).toString()
        textView.textSize = 48f
        textView.setTextColor(Color.MAGENTA)
        textView.setGravity(Gravity.CENTER)
        addView(
            textView, LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.RIGHT
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
        if (isError || !volumeIcon.isVisible) return
        listener.onRequestMute(this, !tvExoplayerView.exoDorisPlayerView.isMute)
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        isSelected = hasFocus
    }

    fun showVolumeIcon(show: Boolean) {
        volumeIcon.visibility = if (show) View.VISIBLE else View.INVISIBLE
        volumeIcon.isFocusable = show
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
        }
    }
}

internal fun swapView(view1: MultiViewStateView, view2: MultiViewStateView) {
    // check error state first
    val otherViewIsError = view2.isError
    view2.setError(view1.isError)
    view1.setError(otherViewIsError)
    // swap player
    val tag0PlayerView = (view1.getChildAt(0) as ReactTVExoplayerView).exoDorisPlayerView
    val tag1PlayerView = (view2.getChildAt(0) as ReactTVExoplayerView).exoDorisPlayerView
    val player = tag0PlayerView.player
    tag0PlayerView.player = null
    tag0PlayerView.player = tag1PlayerView.player
    tag0PlayerView.mute(false)
    tag1PlayerView.player = null
    tag1PlayerView.player = player
    tag1PlayerView.mute(true)
}
