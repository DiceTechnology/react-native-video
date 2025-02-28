package com.brentvatne.exoplayer

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.children
import com.brentvatne.react.R
import com.brentvatne.util.ReadableMapUtils
import com.facebook.react.ReactApplication
import com.facebook.react.ReactRootView
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext

@SuppressLint("ViewConstructor")
class ReactTvMultipleExoplayerView(val themedReactContext: ThemedReactContext) : FrameLayout(themedReactContext) {

    private val multipleLayout: MultipleLayout = MultipleLayout(themedReactContext)

    init {
        addView(
            multipleLayout, 0, android.view.ViewGroup.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )
    }

    fun dropView() {
        (children.find { it is ReactRootView } as? ReactRootView)?.unmountReactApplication()
    }

    override fun addView(view: View) {
        if (view is ReactTVExoplayerView) {
            view.setMultipleViewMode(true)
            multipleLayout.addView(view)
        } else {
            super.addView(view)
        }
    }

    override fun removeView(view: View) {
        if (view is ReactTVExoplayerView) {
            multipleLayout.removeView(view)
        } else {
            super.removeView(view)
        }
    }

    fun setSrc(src: ReadableMap?) {
        src ?: return
        if (src.hasKey(ReactTVMultipleExoplayerViewManager.PROP_SRC_PLUGINS)) {
            val uriString = ReadableMapUtils.getString(src, ReactTVMultipleExoplayerViewManager.PROP_SRC_URI)
            val bottomPlugin = ReadableMapUtils.getMap(src.getMap(ReactTVMultipleExoplayerViewManager.PROP_SRC_PLUGINS), "bottom")
            if (bottomPlugin != null && !hasAttachedBottomView()) {
                setBottomOverlayComponent(
                    uriString,
                    ReadableMapUtils.getString(bottomPlugin, "name"),
                    ReadableMapUtils.getInt(bottomPlugin, "width", -1),
                    ReadableMapUtils.getInt(bottomPlugin, "height", -1)
                )
            }
        }
    }

    private fun setBottomOverlayComponent(key: String?, component: String?, width: Int, height: Int) {
        if (component == null || component.isEmpty()) return
        if (TextUtils.equals(getTag(R.id.bottomComponentTag) as? String, key)) return
        // add frameLayout to ExoPlayerView, ReactRootView load data first, move to ExoPlayerControllerView.
//        val frameLayout = ReactRootFrameLayout(context)
//        frameLayout.setOnSizeChangedListener { reactRootFrameLayout: ReactRootFrameLayout, childView: View ->
//            reactRootFrameLayout.removeView(childView)
//            childView.layoutParams = LayoutParams(
//                if (width > 0) width else LayoutParams.WRAP_CONTENT,
//                if (height > 0) height else LayoutParams.WRAP_CONTENT
//            )
//            exoDorisPlayerView.setBottomComponentView(childView, FocusProcessor { view: View? ->
//                if (view != null) {
//                    val className = view.javaClass.name
//                    if (className == "androidx.compose.ui.platform.AndroidComposeView") {
//                        return@setBottomComponentView true
//                    }
//                }
//                view is ReactViewGroup
//            })
//            exoDorisPlayerView.removeView(reactRootFrameLayout)
//        }
        val reactRootView = ReactRootView(context)
        reactRootView.tag = R.id.bottom_overlay_component
        reactRootView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 300).apply {
            gravity = Gravity.BOTTOM
        }
        reactRootView.startReactApplication(
            (context.applicationContext as ReactApplication)
                .reactNativeHost.reactInstanceManager, component, null
        )
        addView(reactRootView)
//        exoDorisPlayerView.addView(frameLayout, LayoutParams(LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT))
//        exoDorisPlayerView.setTag(R.id.bottomComponentTag, key)
    }

    private fun hasAttachedBottomView(): Boolean {
        for (i in 0 until childCount) {
            if (getChildAt(i).tag == R.id.bottom_overlay_component) {
                return true
            }
        }
        return false
    }
}