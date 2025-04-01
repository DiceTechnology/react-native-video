package com.brentvatne.exoplayer

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import com.brentvatne.util.Logger
import com.brentvatne.util.ReadableMapUtils
import com.diceplatform.doris.ui.entity.LabelsTranslation
import com.facebook.react.ReactApplication
import com.facebook.react.ReactRootView
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext

@SuppressLint("ViewConstructor")
class ReactTvMultipleExoplayerView(val themedReactContext: ThemedReactContext) : FrameLayout(themedReactContext),
    MultiViewStateView.OnVolumeChangedListener {

    private val fullscreenControlBarHeight = (96 * themedReactContext.resources.displayMetrics.density).toInt()
    private val multiViewLayout: MultiViewLayout = MultiViewLayout(themedReactContext)
    private val bottomContainer: FrameLayout = FrameLayout(themedReactContext)
    private val multiViewControlBar: MultiViewControlBar = MultiViewControlBar(themedReactContext)
    private val centerFocusAnchorView: View = View(themedReactContext) // all children focusable views hided, the parent will lose focus and can not receive dispatch key event fun.
    private val multiViewGuide: MultiViewGuide = MultiViewGuide(themedReactContext)

    var labelsTranslation: LabelsTranslation? = null
    val multiViewMode
        get() = multiViewLayout.multiViewMode

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
        addView(
            multiViewGuide,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )
        addView(
            centerFocusAnchorView.apply {
                visibility = View.GONE
                isFocusable = true
            },
            LayoutParams(
                1, 1, Gravity.CENTER
            )
        )
        multiViewControlBar.multiViewControlBarListener = object : MultiViewControlBarListenerWrapper(multiViewLayout) {
            override fun onMultiviewControlBarVisibleChangedBefore(visible: Boolean) {
                centerFocusAnchorView.visibility = if (visible) View.GONE else View.VISIBLE
            }
        }
    }

    fun dropView() {
        (bottomContainer.getChildAt(0) as? ReactRootView)?.unmountReactApplication()
        multiViewLayout.removeAllViews()
        unloadButtonOverlayComponent()
    }

    fun getExoplayerChildrenList(): List<ReactTVExoplayerView> {
        return multiViewLayout.children.map { (it as ViewGroup)[0] as ReactTVExoplayerView }.toList()
    }

    private fun getFocusableChildrenList(): List<MultiViewStateView> {
        return multiViewLayout.children.map { it as MultiViewStateView }.toList()
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
                reactRootView.setShouldLogContentAppeared(true)
                reactRootView.layoutParams = LayoutParams(
                    if (width > 0) width else LayoutParams.MATCH_PARENT,
                    if (height > 0) height else LayoutParams.WRAP_CONTENT
                )
                Logger.log(this, "startReactApplication: $component")
                reactRootView.startReactApplication(
                    (context.applicationContext as ReactApplication)
                        .reactNativeHost.reactInstanceManager, component, null
                )
                bottomContainer.addView(reactRootView)
            }
        }
    }

    fun unloadButtonOverlayComponent() {
        bottomContainer.removeAllViews()
    }

    fun setMultiViewMode(multiViewMode: Boolean) {
        multiViewLayout.multiViewMode = multiViewMode
    }

    override fun setId(id: Int) {
        super.setId(id)
        getExoplayerChildrenList().forEach { view ->
            (view as? ReactTVExoplayerView)?.id = id
        }
    }

    fun addMultiViewChild(child: View, multiViewMode: Boolean) {
        if (child is ReactTVExoplayerView) {
            child.id = id
            child.setMultipleViewMode(multiViewMode)
            child.setShowBottomComponent(!multiViewMode)
            child.setOnFocusChangeListener(childViewOnFocusChangeListener)
            multiViewLayout.addView(MultiViewStateView(child, labelsTranslation, multiViewMode, this))
            requestLayout()
            // fix only one view in multiview mode, this view can not receive dispatch key event fun.
            if (!child.exoDorisPlayerView.isMute) {
                post { multiViewLayout.children.last().requestFocus() }
            }

            multiViewGuide.show(labelsTranslation)
        }
    }

    private val childViewOnFocusChangeListener = OnFocusChangeListener { playerView, hasFocus ->
        (playerView as ReactTVExoplayerView).mute(!hasFocus)
    }

    override fun onRequestMute(view: MultiViewStateView, mute: Boolean) {
        if (mute) {
            view.mute(true)
        } else {
            getFocusableChildrenList().forEach { child ->
                if (child.getTagPosition() == view.getTagPosition()) {
                    child.mute(false)
                } else {
                    child.mute(true)
                }
            }
        }
    }

    override fun removeView(view: View) {
        multiViewLayout.children.find { (it as ViewGroup)[0] == view }?.let { child ->
            multiViewLayout.removeView(child)
            (child as? ViewGroup)?.removeViewAt(0)
            requestLayout()
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
                left,
                bottom - multiViewControlBar.measuredHeight,
                right,
                bottom
            )
        }
        if (multiViewGuide.isVisible) {
            multiViewGuide.layout(
                left,
                top,
                left + multiViewGuide.measuredWidth,
                top + multiViewGuide.measuredHeight
            )
        }
        if (centerFocusAnchorView.isVisible) {
            centerFocusAnchorView.layout(
                measuredWidth / 2,
                measuredHeight / 2,
                measuredWidth / 2 + 1,
                measuredHeight / 2 + 1,
            )
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (multiViewMode) { // Exit multi view mode when back key is pressed.
            if (multiViewControlBar.onKeyEvent(event)) {
                return true
            }
            if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    return true
                } else if (event.action == KeyEvent.ACTION_UP) {
                    if (multiViewLayout.pictureInPictureMode) {
                        multiViewLayout.pictureInPictureMode = false
                    } else if (multiViewLayout.fullscreenMode) {
                        setFullscreenMode(false)
                    } else {
                        getExoplayerChildrenList().find { it.isNotEmpty() }?.exitMultiViewMode()
                    }
                    return true
                }
            } else if (event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                // only allow more than one child to enter fullscreen mode
                if (focusedChild is MultiViewLayout && getExoplayerChildrenList().size > 1) {
                    setFullscreenMode(true)
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun setFullscreenMode(fullscreen: Boolean) {
        centerFocusAnchorView.takeIf { !fullscreen }?.visibility = View.GONE
        bottomContainer.visibility = if (fullscreen) View.GONE else View.VISIBLE
        multiViewLayout.fullscreenMode = fullscreen
        multiViewControlBar.multiViewSize = getExoplayerChildrenList().size
        multiViewControlBar.setCanShowControlBar(fullscreen)
        multiViewLayout.children.forEach { child ->
            (child as MultiViewStateView).apply {
                isFocusable = !fullscreen
                setVolumeIconFocusable(fullscreen)
                setVolumeIconVisible(true)
            }
        }
    }
}
