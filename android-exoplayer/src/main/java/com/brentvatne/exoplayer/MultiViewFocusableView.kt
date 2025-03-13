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
    child: ReactTVExoplayerView,
    focusable: Boolean,
    private val listener: OnVolumeChangedListener,
) : FrameLayout(child.context), OnFocusChangeListener, OnClickListener {

    interface OnVolumeChangedListener {
        fun onRequestVolume(view: ReactTVExoplayerView)
    }

    private val iconSize = (24 * child.resources.displayMetrics.density).toInt()
    private val volumeIcon: ImageView = ImageView(child.context)

    init {
        isFocusable = focusable
        foreground = ResourcesCompat.getDrawable(child.resources, R.drawable.ic_item_focus_selector, null)
        // add player view
        addView(child)

        // add volume icon
        volumeIcon.setImageResource(R.drawable.ic_multiview_mute_selector)
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
        listener.onRequestVolume(getChildAt(0) as ReactTVExoplayerView)
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        isSelected = hasFocus
    }

    fun showVolumeIcon(show: Boolean) {
        isFocusable = !show
        volumeIcon.visibility = if (show) View.VISIBLE else View.INVISIBLE
        volumeIcon.isFocusable = show
    }
}