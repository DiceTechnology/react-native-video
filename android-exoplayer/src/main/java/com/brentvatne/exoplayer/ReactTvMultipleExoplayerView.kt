package com.brentvatne.exoplayer

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import com.brentvatne.util.ReadableMapUtils
import com.facebook.react.ReactApplication
import com.facebook.react.ReactRootView
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext

@SuppressLint("ViewConstructor")
class ReactTvMultipleExoplayerView(val themedReactContext: ThemedReactContext) : FrameLayout(themedReactContext) {

    private val fullscreenControlBarHeight = (96 * themedReactContext.resources.displayMetrics.density).toInt()
    private val multiViewLayout: MultiViewLayout = MultiViewLayout(themedReactContext)
    private val bottomContainer: FrameLayout = FrameLayout(themedReactContext)
    private val multiViewControlBar: MultiViewControlBar = MultiViewControlBar(themedReactContext, multiViewLayout)
    var multiViewMode = false
        set(value) {
            field = value
            multiViewLayout.multiViewMode = value
        }
    private var fullscreenMode = false
        set(value) {
            field = value
            bottomContainer.visibility = if (value) View.GONE else View.VISIBLE
            multiViewLayout.fullscreenMode = value
            multiViewControlBar.multiViewSize = getMultiViewChildrenList().size
            multiViewControlBar.setVisible(value)
        }

    init {
        addView(
            multiViewLayout,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                Gravity.TOP
            )
        )
        addView(
            bottomContainer,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            )
        )
        addView(
            multiViewControlBar,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                fullscreenControlBarHeight,
                Gravity.BOTTOM
            )
        )
    }

    fun dropView() {
        (bottomContainer.getChildAt(0) as? ReactRootView)?.unmountReactApplication()
        multiViewLayout.removeAllViews()
        bottomContainer.removeAllViews()
    }

    fun getMultiViewChildrenList(): List<View> {
        return multiViewLayout.children.toList()
    }

    fun loadBottomOverlayComponent(src: ReadableMap) {
        if (src.hasKey(ReactTVMultipleExoplayerViewManager.PROP_SRC_PLUGINS)) {
            val uriString = ReadableMapUtils.getString(src, ReactTVMultipleExoplayerViewManager.PROP_SRC_URI)
            val bottomPlugin = ReadableMapUtils.getMap(src.getMap(ReactTVMultipleExoplayerViewManager.PROP_SRC_PLUGINS), "bottom")
            if (bottomPlugin != null && bottomContainer.isEmpty()) {
                val width = ReadableMapUtils.getInt(bottomPlugin, "width", -1)
                val height = ReadableMapUtils.getInt(bottomPlugin, "height", -1)
                val component = ReadableMapUtils.getString(bottomPlugin, "name")
                val reactRootView = ReactRootView(context)
                reactRootView.tag = uriString
                reactRootView.layoutParams = LayoutParams(
                    if (width > 0) width else LayoutParams.MATCH_PARENT,
                    if (height > 0) height else LayoutParams.WRAP_CONTENT
                )
                reactRootView.startReactApplication(
                    (context.applicationContext as ReactApplication)
                        .reactNativeHost.reactInstanceManager, component, null
                )
                bottomContainer.addView(reactRootView)
            }
        }
    }

    override fun setId(id: Int) {
        super.setId(id)
        multiViewLayout.children.forEach { view ->
            (view as? ReactTVExoplayerView)?.id = id
        }
    }

    override fun addView(child: View) {
        if (child is ReactTVExoplayerView) {
            child.id = id
            child.setOnFocusChangeListener(childViewOnFocusChangeListener)
            multiViewLayout.addView(child)
            requestLayout()
        }
    }

    private val childViewOnFocusChangeListener = OnFocusChangeListener { playerView, hasFocus ->
        (playerView as ReactTVExoplayerView).mute(!hasFocus)
    }

    override fun removeView(view: View) {
        multiViewLayout.removeView(view)
        requestLayout()
    }

    override fun requestLayout() {
        super.requestLayout()
        if (width > 0 && width > 0) {
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (bottomContainer.measuredHeight > 1 && bottomContainer.isVisible) {
            multiViewLayout.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height - bottomContainer.measuredHeight, MeasureSpec.EXACTLY)
            )
        }
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ) {
        multiViewLayout.layout(
            left,
            top,
            left + multiViewLayout.measuredWidth,
            top + multiViewLayout.measuredHeight
        )
        if (bottomContainer.isVisible && bottomContainer.measuredHeight > 0) {
            bottomContainer.layout(
                left,
                top + multiViewLayout.measuredHeight,
                right,
                top + multiViewLayout.measuredHeight + bottomContainer.measuredHeight
            )
        }
        if (multiViewControlBar.isVisible && multiViewControlBar.measuredHeight > 0) {
            multiViewControlBar.layout(
                left, bottom - multiViewControlBar.measuredHeight, right, bottom
            )
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (multiViewMode) { // Exit multi view mode when back key is pressed.
            if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    return true
                } else if (event.action == KeyEvent.ACTION_UP) {
                    if (multiViewLayout.pictureInPictureMode) {
                        multiViewLayout.pictureInPictureMode = false
                    } else if (fullscreenMode) {
                        fullscreenMode = false
                    } else {
                        (multiViewLayout.getChildAt(0) as? ReactTVExoplayerView)?.eventEmitter?.setMultiViewMode(false)
                    }
                    return true
                }
            } else if (event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                // enter fullscreen mode
                if (focusedChild is MultiViewLayout && getMultiViewChildrenList().size > 1) {
                    fullscreenMode = true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
