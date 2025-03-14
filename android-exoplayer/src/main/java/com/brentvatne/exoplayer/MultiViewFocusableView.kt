package com.brentvatne.exoplayer

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnFocusChangeListener
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.brentvatne.react.R

@SuppressLint("ViewConstructor")
class MultiViewFocusableView(
    private val tvExoplayerView: ReactTVExoplayerView,
    focusable: Boolean,
    private val listener: OnVolumeChangedListener,
) : FrameLayout(tvExoplayerView.context), OnFocusChangeListener, OnClickListener {

    interface OnVolumeChangedListener {
        fun onRequestVolume(view: MultiViewFocusableView)
    }

    private val iconSize = (24 * tvExoplayerView.resources.displayMetrics.density).toInt()
    val volumeIcon: ImageView = ImageView(tvExoplayerView.context)
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