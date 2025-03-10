package com.brentvatne.exoplayer

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.isEmpty
import com.brentvatne.util.ReadableMapUtils
import com.facebook.react.ReactApplication
import com.facebook.react.ReactRootView
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext

@SuppressLint("ViewConstructor")
class ReactTvMultipleExoplayerView(val themedReactContext: ThemedReactContext) : LinearLayout(themedReactContext) {

    private val multiViewLayout: MultiViewLayout = MultiViewLayout(themedReactContext)
    private val bottomContainer: FrameLayout = FrameLayout(themedReactContext)
    var multiViewMode = false
        set(value) {
            field = value
            multiViewLayout.multiViewMode = value
        }

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL
        addView(
            multiViewLayout,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                1f
            )
        )
        addView(
            bottomContainer,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
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

    override fun setId(id: Int) {
        super.setId(id)
        multiViewLayout.children.forEach { view ->
            (view as? ReactTVExoplayerView)?.id = id
        }
    }

    override fun addView(child: View) {
        if (child is ReactTVExoplayerView) {
            child.id = id
            multiViewLayout.addView(child)
            requestLayout()
        }
    }

    override fun removeView(view: View) {
        multiViewLayout.removeView(view)
        requestLayout()
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

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (multiViewMode) {
            if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    return true
                } else if (event.action == KeyEvent.ACTION_UP) {
                    (multiViewLayout.getChildAt(0) as? ReactTVExoplayerView)?.eventEmitter?.setMultiViewMode(false)
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
