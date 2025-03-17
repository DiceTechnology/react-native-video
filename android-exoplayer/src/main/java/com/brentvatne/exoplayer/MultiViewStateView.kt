package com.brentvatne.exoplayer

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnFocusChangeListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import com.brentvatne.react.R
import com.diceplatform.doris.DorisPlayerOutput
import com.diceplatform.doris.entity.DorisPlayerEvent
import com.diceplatform.doris.ui.entity.LabelsTranslation

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

    private val iconSize = (24 * tvExoplayerView.resources.displayMetrics.density).toInt()
    val volumeIcon: ImageView = ImageView(tvExoplayerView.context)
    private val errorMsgView: TextView = TextView(tvExoplayerView.context)
    private val isMute: Boolean
        get() = tvExoplayerView.exoDorisPlayerView.isMute

    init {
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
            text = labelsTranslation?.get("multiViewPlaybackError")
            gravity = Gravity.CENTER
            visibility = View.GONE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTextColor(Color.WHITE)
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
            errorMsgView.visibility = View.VISIBLE
        }
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

    private fun resetVolumeIcon() {
        volumeIcon.setImageResource(
            if (isMute)
                R.drawable.ic_multiview_mute_selector
            else
                R.drawable.ic_multiview_volume_selector
        )
    }
}
