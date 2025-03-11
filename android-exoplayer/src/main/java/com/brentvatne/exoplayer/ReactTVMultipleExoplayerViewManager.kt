package com.brentvatne.exoplayer

import android.util.Log
import android.view.ViewGroup.LayoutParams
import androidx.core.content.res.ResourcesCompat
import com.brentvatne.react.R
import com.brentvatne.util.ReadableMapUtils
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp

@Suppress("UNUSED_PARAMETER")
class ReactTVMultipleExoplayerViewManager(reactApplicationContext: ReactApplicationContext) : ViewGroupManager<ReactTvMultipleExoplayerView>() {

    companion object Keys {
        // Source properties
        private const val PROP_SRC: String = "src"
        const val PROP_SRC_URI: String = "uri"
        private const val PROP_SRC_NOW_PLAYING: String = "nowPlaying"
        const val PROP_SRC_PLUGINS: String = "plugins"

        // Metadata properties
        private const val PROP_METADATA: String = "metadata"
        private const val PROP_THEME: String = "theme"
        private const val PROP_BUTTONS: String = "buttons"
        private const val PROP_RESIZE_MODE: String = "resizeMode"
        private const val PROP_REPEAT: String = "repeat"
        private const val PROP_SELECTED_AUDIO_TRACK: String = "selectedAudioTrack"
        private const val PROP_SELECTED_TEXT_TRACK: String = "selectedTextTrack"
        private const val PROP_PAUSED: String = "paused"
        private const val PROP_MUTED: String = "muted"
        private const val PROP_MEDIA_KEYS: String = "mediaKeys"
        private const val PROP_VOLUME: String = "volume"
        private const val PROP_BUFFER_CONFIG: String = "bufferConfig"
        private const val PROP_PROGRESS_UPDATE_INTERVAL: String = "progressUpdateInterval"
        private const val PROP_SEEK: String = "seek"
        private const val PROP_RATE: String = "rate"
        private const val PROP_PLAY_IN_BACKGROUND: String = "playInBackground"
        private const val PROP_DISABLE_FOCUS: String = "disableFocus"
        private const val PROP_USE_TEXTURE_VIEW: String = "useTextureView"
        private const val PROP_LIVE: String = "live"
        private const val PROP_EPG: String = "hasEpg"
        private const val PROP_STATS: String = "hasStats"
        private const val PROP_HIDE_AD_UI_ELEMENTS: String = "hideAdUiElements"
        private const val PROP_IS_WHY_THIS_AD_ENABLED: String = "isWhyThisAdIconEnabled"
        private const val PROP_CONTROLS: String = "controls"
        private const val PROP_CONTROLS_OPACITY: String = "controlsOpacity"
        private const val PROP_PROGRESS_BAR_MARGIN_BOTTOM: String = "progressBarMarginBottom"
        private const val PROP_STATE_OVERLAY: String = "stateOverlay"
        private const val PROP_OVERLAY_AUTO_HIDE_TIMEOUT: String = "overlayAutoHideTimeout"
        private const val PROP_STATE_MIDDLE_CORE_CONTROLS: String = "stateMiddleCoreControls"
        private const val PROP_STATE_PROGRESS_BAR: String = "stateProgressBar"
        private const val PROP_TRANSLATIONS: String = "translations"
        private const val PROP_RELATED_VIDEOS: String = "relatedVideos"
        private const val PROP_IS_FAVOURITE: String = "isFavourite"
        private const val PROP_MULTI_VIEW_MODE: String = "multiViewMode"
    }

    private val primaryViewManager: ReactTVExoplayerViewManager = ReactTVExoplayerViewManager(reactApplicationContext)
    private lateinit var primaryView: ReactTVExoplayerView
    private lateinit var rootView: ReactTvMultipleExoplayerView

    override fun getName(): String = "RCTVideo"

    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any>? = primaryViewManager.exportedCustomDirectEventTypeConstants

    override fun getExportedViewConstants(): Map<String, Any>? = primaryViewManager.exportedViewConstants

    override fun getCommandsMap(): Map<String, Int>? = primaryViewManager.commandsMap

    override fun receiveCommand(root: ReactTvMultipleExoplayerView, commandId: String?, args: ReadableArray?) {
        primaryViewManager.receiveCommand(primaryView, commandId, args)
    }

    override fun createViewInstance(context: ThemedReactContext): ReactTvMultipleExoplayerView {
        if (this::rootView.isInitialized) { // createViewInstance may be called multiple times, so we need to clean up the previous instance first.
            rootView.dropView()
        }
        primaryView = primaryViewManager.createViewInstance(context)
        rootView = ReactTvMultipleExoplayerView(context)
        rootView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        rootView.addView(primaryView)
        return rootView
    }

    override fun onDropViewInstance(view: ReactTvMultipleExoplayerView) {
        view.dropView()
        primaryViewManager.onDropViewInstance(primaryView)
    }

    @ReactProp(name = PROP_SRC)
    fun setSrc(videoView: ReactTvMultipleExoplayerView, src: ReadableMap?) {
        Log.d(this.javaClass.simpleName, "setSrc: ${src?.getString("id")}")
        primaryViewManager.setSrc(primaryView, src)
    }

    @ReactProp(name = PROP_METADATA)
    fun setMetadata(videoView: ReactTvMultipleExoplayerView, metadata: ReadableMap?) {
        primaryViewManager.setMetadata(primaryView, metadata)
    }

    @ReactProp(name = PROP_SRC_NOW_PLAYING)
    fun setNowPlaying(videoView: ReactTvMultipleExoplayerView, nowPlayingMap: ReadableMap?) {
        primaryViewManager.setNowPlaying(primaryView, nowPlayingMap)
    }

    @ReactProp(name = PROP_THEME)
    fun setTheme(videoView: ReactTvMultipleExoplayerView, theme: ReadableMap?) {
        primaryViewManager.setTheme(primaryView, theme)
    }

    @ReactProp(name = PROP_RESIZE_MODE)
    fun setResizeMode(videoView: ReactTvMultipleExoplayerView?, resizeModeOrdinalString: String?) {
        primaryViewManager.setResizeMode(primaryView, resizeModeOrdinalString)
    }

    @ReactProp(name = PROP_REPEAT, defaultBoolean = false)
    fun setRepeat(videoView: ReactTvMultipleExoplayerView, repeat: Boolean) {
        primaryViewManager.setRepeat(primaryView, repeat)
    }

    @ReactProp(name = PROP_SELECTED_AUDIO_TRACK)
    fun setSelectedAudioTrack(
        videoView: ReactTvMultipleExoplayerView?,
        selectedAudioTrack: ReadableMap?,
    ) {
        // Deprecated, not used.
        primaryViewManager.setSelectedAudioTrack(primaryView, selectedAudioTrack)
    }

    @ReactProp(name = PROP_SELECTED_TEXT_TRACK)
    fun setSelectedTextTrack(
        videoView: ReactTvMultipleExoplayerView?,
        selectedTextTrack: ReadableMap?,
    ) {
        // Deprecated, not used.
        primaryViewManager.setSelectedTextTrack(primaryView, selectedTextTrack)
    }

    @ReactProp(name = PROP_PAUSED, defaultBoolean = false)
    fun setPaused(videoView: ReactTvMultipleExoplayerView, paused: Boolean) {
        primaryViewManager.setPaused(primaryView, paused)
    }

    @ReactProp(name = PROP_MUTED, defaultBoolean = false)
    fun setMuted(videoView: ReactTvMultipleExoplayerView, muted: Boolean) {
        primaryViewManager.setMuted(primaryView, muted)
    }

    @ReactProp(name = PROP_MEDIA_KEYS, defaultBoolean = true)
    fun setMediaKeys(videoView: ReactTvMultipleExoplayerView, visible: Boolean) {
        primaryViewManager.setMediaKeys(primaryView, visible)
    }

    @ReactProp(name = PROP_VOLUME, defaultFloat = 1.0f)
    fun setVolume(videoView: ReactTvMultipleExoplayerView, volume: Float) {
        primaryViewManager.setVolume(primaryView, volume)
    }

    @ReactProp(name = PROP_PROGRESS_UPDATE_INTERVAL, defaultFloat = 250.0f)
    fun setProgressUpdateInterval(videoView: ReactTvMultipleExoplayerView, progressUpdateInterval: Float) {
        primaryViewManager.setProgressUpdateInterval(primaryView, progressUpdateInterval)
    }

    @ReactProp(name = PROP_SEEK)
    fun setSeek(videoView: ReactTvMultipleExoplayerView, seek: Float) {
        primaryViewManager.setSeek(primaryView, seek)
    }

    @ReactProp(name = PROP_RATE)
    fun setRate(videoView: ReactTvMultipleExoplayerView, rate: Float) {
        primaryViewManager.setRate(primaryView, rate)
    }

    @ReactProp(name = PROP_PLAY_IN_BACKGROUND, defaultBoolean = false)
    fun setPlayInBackground(videoView: ReactTvMultipleExoplayerView, playInBackground: Boolean) {
        primaryViewManager.setPlayInBackground(primaryView, playInBackground)
    }

    @ReactProp(name = PROP_DISABLE_FOCUS, defaultBoolean = false)
    fun setDisableFocus(videoView: ReactTvMultipleExoplayerView, disableFocus: Boolean) {
        primaryViewManager.setDisableFocus(primaryView, disableFocus)
    }

    @ReactProp(name = PROP_LIVE, defaultBoolean = false)
    fun setLive(videoView: ReactTvMultipleExoplayerView?, live: Boolean) {
        // Move the PROP_LIVE as PROP_SRC_LIVE
        primaryViewManager.setLive(primaryView, live)
    }

    @ReactProp(name = PROP_EPG, defaultBoolean = false)
    fun setEpg(videoView: ReactTvMultipleExoplayerView, hasEpg: Boolean) {
        primaryViewManager.setEpg(primaryView, hasEpg)
    }

    @ReactProp(name = PROP_STATS, defaultBoolean = false)
    fun setStats(videoView: ReactTvMultipleExoplayerView, hasStats: Boolean) {
        primaryViewManager.setStats(primaryView, hasStats)
    }

    @ReactProp(name = PROP_HIDE_AD_UI_ELEMENTS, defaultBoolean = false)
    fun setHideAdUiElements(videoView: ReactTvMultipleExoplayerView, hideAdUiElements: Boolean) {
        primaryViewManager.setHideAdUiElements(primaryView, hideAdUiElements)
    }

    @ReactProp(name = PROP_IS_WHY_THIS_AD_ENABLED)
    fun setIsWhyThisAdIconEnabled(videoView: ReactTvMultipleExoplayerView, isWhyThisAdIconEnabled: Boolean) {
        primaryViewManager.setIsWhyThisAdIconEnabled(primaryView, isWhyThisAdIconEnabled)
    }

    @ReactProp(name = PROP_CONTROLS)
    fun setControls(videoView: ReactTvMultipleExoplayerView, visible: Boolean) {
        primaryViewManager.setControls(primaryView, visible)
    }

    @ReactProp(name = PROP_CONTROLS_OPACITY)
    fun setControlsOpacity(videoView: ReactTvMultipleExoplayerView, opacity: Float) {
        primaryViewManager.setControlsOpacity(primaryView, opacity)
    }

    @ReactProp(name = PROP_USE_TEXTURE_VIEW, defaultBoolean = false)
    fun setUseTextureView(videoView: ReactTvMultipleExoplayerView, useTextureView: Boolean) {
        primaryViewManager.setUseTextureView(primaryView, useTextureView)
    }

    @ReactProp(name = PROP_BUFFER_CONFIG)
    fun setBufferConfig(videoView: ReactTvMultipleExoplayerView, bufferConfig: ReadableMap?) {
        primaryViewManager.setBufferConfig(primaryView, bufferConfig)
    }

    @ReactProp(name = PROP_PROGRESS_BAR_MARGIN_BOTTOM, defaultInt = 0)
    fun setProgressBarMarginBottom(videoView: ReactTvMultipleExoplayerView, margin: Int) {
        primaryViewManager.setProgressBarMarginBottom(primaryView, margin)
    }

    @ReactProp(name = PROP_STATE_OVERLAY)
    fun setStateOverlay(videoView: ReactTvMultipleExoplayerView, state: String?) {
        primaryViewManager.setStateOverlay(primaryView, state)
    }

    @ReactProp(name = PROP_OVERLAY_AUTO_HIDE_TIMEOUT)
    fun setOverlayAutoHideTimeout(videoView: ReactTvMultipleExoplayerView, hideTimeout: Int?) {
        primaryViewManager.setOverlayAutoHideTimeout(primaryView, hideTimeout)
    }

    @ReactProp(name = PROP_STATE_MIDDLE_CORE_CONTROLS)
    fun setStateMiddleCoreControls(videoView: ReactTvMultipleExoplayerView, state: String?) {
        primaryViewManager.setStateMiddleCoreControls(primaryView, state)
    }

    @ReactProp(name = PROP_STATE_PROGRESS_BAR)
    fun setStateProgressBar(videoView: ReactTvMultipleExoplayerView, state: String?) {
        primaryViewManager.setStateProgressBar(primaryView, state)
    }

    @ReactProp(name = PROP_TRANSLATIONS)
    fun setTranslations(videoView: ReactTvMultipleExoplayerView, translations: ReadableMap?) {
        primaryViewManager.setTranslations(primaryView, translations)
    }

    @ReactProp(name = PROP_RELATED_VIDEOS)
    fun setRelatedVideos(videoView: ReactTvMultipleExoplayerView, relatedVideosMap: ReadableMap?) {
        primaryViewManager.setRelatedVideos(primaryView, relatedVideosMap)
    }

    @ReactProp(name = PROP_BUTTONS)
    fun setButtons(videoView: ReactTvMultipleExoplayerView, buttons: ReadableMap?) {
        primaryViewManager.setButtons(primaryView, buttons)
    }

    @ReactProp(name = PROP_IS_FAVOURITE)
    fun setIsFavourite(videoView: ReactTvMultipleExoplayerView, isFavourite: Boolean) {
        primaryViewManager.setIsFavourite(primaryView, isFavourite)
    }

    @ReactProp(name = "multiViewSources")
    fun setMultiVideos(videoView: ReactTvMultipleExoplayerView, array: ReadableArray?) {
        if (array == null || array.size() == 0) {
            rootView.getMultiViewChildrenList().filter { it != primaryView }.forEach {
                rootView.removeView(it)
            }
            return
        }
        if (array.size() == 1 && rootView.multiViewMode) {
            rootView.loadBottomOverlayComponent(array.getMap(0))
        }

        if (array.size() > rootView.getMultiViewChildrenList().size) {
            val list = rootView.getMultiViewChildrenList().mapNotNull { it.tag as? ReadableMap }
            for (i in 0 until array.size()) {
                val src = array.getMap(i)
                if (list.isEmpty() || src !in list) {
                    val playerView = primaryViewManager.createViewInstance(rootView.themedReactContext)
                    playerView.tag = src
                    playerView.mute(true)
                    playerView.setMultipleViewMode(true)
                    playerView.isFocusable = true
                    playerView.foreground = ResourcesCompat.getDrawable(rootView.resources, R.drawable.ic_item_focus_selector, null)
                    playerView.setShowBottomComponent(false)
                    //TODO: ---- test code --------------------------
                    playerView.setTextView((i + 1).toString())
                    primaryViewManager.setSrc(playerView, src)
                    rootView.addView(playerView)
                    break
                }
            }
        } else if (array.size() < rootView.getMultiViewChildrenList().size) {
            val removeView = rootView.getMultiViewChildrenList().find { view ->
                val tagSrc = view.tag as? ReadableMap
                tagSrc != null && !ReadableMapUtils.contain(array, tagSrc)
            }
            removeView?.let {
                (it as? ReactTVExoplayerView)?.stopPlayback()
                rootView.removeView(it)
            }
        }
    }

    @ReactProp(name = PROP_MULTI_VIEW_MODE)
    fun setMultiViewMode(videoView: ReactTvMultipleExoplayerView, multiViewMode: Boolean) {
        Log.d(this.javaClass.simpleName, "setMultiViewMode: $multiViewMode")
        rootView.multiViewMode = multiViewMode
        if (multiViewMode) {
            rootView.removeView(primaryView)
        }
    }
}
