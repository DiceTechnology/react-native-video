package com.brentvatne.exoplayer

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp

@Suppress("UNUSED_PARAMETER")
class ReactTVMultipleExoplayerViewManager(private val reactApplicationContext: ReactApplicationContext) : ViewGroupManager<ReactTvMultipleExoplayerView>() {

    companion object Keys {
        // Source properties
        private const val PROP_SRC: String = "src"
        const val PROP_SRC_URI: String = "uri"
        private const val PROP_SRC_CONTENT_TYPE: String = "contentType"
        private const val PROP_SRC_SUBTITLES: String = "subtitles"
        private const val PROP_SRC_ID: String = "id"
        private const val PROP_SRC_TYPE: String = "type"
        private const val PROP_SRC_DRM: String = "drm"
        private const val PROP_SRC_IMA: String = "ima"
        private const val PROP_SRC_CHANNEL_ID: String = "channelId"
        private const val PROP_SRC_SERIES_ID: String = "seriesId"
        private const val PROP_SRC_SEASON_ID: String = "seasonId"
        private const val PROP_SRC_PLAYLIST_ID: String = "playlistId"
        private const val PROP_SRC_DURATION: String = "duration"
        private const val PROP_SRC_CHANNEL_NAME: String = "channelName"
        private const val PROP_SRC_CONFIG: String = "config"
        private const val PROP_SRC_MUX_DATA: String = "muxData"
        private const val PROP_SRC_HEADERS: String = "requestHeaders"
        private const val PROP_SRC_APS: String = "aps"
        private const val PROP_SRC_APS_TEST_MODE: String = "testMode"
        private const val PROP_SRC_METADATA: String = "metadata"
        private const val PROP_SRC_LIMIT_RANGE: String = "limitedSeekableRange"
        private const val PROP_SRC_SAVE_SUBTITLE_SELECTION: String = "shouldSaveSubtitleSelection"
        private const val PROP_SRC_NOW_PLAYING: String = "nowPlaying"
        private const val PROP_SRC_BIF_URL: String = "thumbnailsPreview"
        private const val PROP_SRC_SELECTED_SUBTITLE_TRACK: String = "selectedSubtitleTrack"
        private const val PROP_SRC_PREFERRED_AUDIO_TRACKS: String = "preferredAudioTracks"
        private const val PROP_SRC_DVR_SEEK_BACKWARD_INTERVAL: String = "dvrSeekBackwardInterval"
        private const val PROP_SRC_DVR_SEEK_FORWARD_INTERVAL: String = "dvrSeekForwardInterval"
        const val PROP_SRC_PLUGINS: String = "plugins"
        private const val PROP_SRC_LIVE: String = "live"

        // Metadata properties
        private const val PROP_METADATA: String = "metadata"
        private const val PROP_METADATA_CHANNEL_LOGO_URL: String = "channelLogoUrl"
        private const val PROP_METADATA_DESCRIPTION: String = "description"
        private const val PROP_METADATA_THUMBNAIL_URL: String = "thumbnailUrl"
        private const val PROP_METADATA_DURATION: String = "duration"
        private const val PROP_METADATA_TITLE: String = "title"
        private const val PROP_METADATA_TYPE: String = "type"
        private const val PROP_METADATA_EPISODE_INFO: String = "episodeInfo"
        private const val PROP_THEME: String = "theme"
        private const val PROP_DRM_CRO_TOKEN: String = "croToken"
        private const val PROP_BUTTONS: String = "buttons"
        private const val PROP_WATCHLIST_BUTTON: String = "watchlist"
        private const val PROP_FAVOURITE_BUTTON: String = "favourite"
        private const val PROP_EPG_BUTTON: String = "epg"
        private const val PROP_STATS_BUTTON: String = "stats"
        private const val PROP_ANNOTATIONS_BUTTON: String = "annotations"
        private const val PROP_RESIZE_MODE: String = "resizeMode"
        private const val PROP_REPEAT: String = "repeat"
        private const val PROP_SELECTED_AUDIO_TRACK: String = "selectedAudioTrack"
        private const val PROP_SELECTED_AUDIO_TRACK_TYPE: String = "type"
        private const val PROP_SELECTED_AUDIO_TRACK_VALUE: String = "value"
        private const val PROP_SELECTED_TEXT_TRACK: String = "selectedTextTrack"
        private const val PROP_SELECTED_TEXT_TRACK_TYPE: String = "type"
        private const val PROP_SELECTED_TEXT_TRACK_VALUE: String = "value"
        private const val PROP_PAUSED: String = "paused"
        private const val PROP_MUTED: String = "muted"
        private const val PROP_MEDIA_KEYS: String = "mediaKeys"
        private const val PROP_VOLUME: String = "volume"
        private const val PROP_BUFFER_CONFIG: String = "bufferConfig"
        private const val PROP_BUFFER_CONFIG_MIN_BUFFER_MS: String = "minBufferMs"
        private const val PROP_BUFFER_CONFIG_MAX_BUFFER_MS: String = "maxBufferMs"
        private const val PROP_BUFFER_CONFIG_BUFFER_FOR_PLAYBACK_MS: String = "bufferForPlaybackMs"
        private const val PROP_BUFFER_CONFIG_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS: String = "bufferForPlaybackAfterRebufferMs"
        private const val PROP_PROGRESS_UPDATE_INTERVAL: String = "progressUpdateInterval"
        private const val PROP_SEEK: String = "seek"
        private const val PROP_RATE: String = "rate"
        private const val PROP_PLAY_IN_BACKGROUND: String = "playInBackground"
        private const val PROP_DISABLE_FOCUS: String = "disableFocus"
        private const val PROP_USE_TEXTURE_VIEW: String = "useTextureView"
        private const val PROP_COLOR_PROGRESS_BAR: String = "colorProgressBar"
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
        private const val PROP_LABEL_FONT_NAME: String = "labelFontName"
        private const val PROP_RELATED_VIDEOS: String = "relatedVideos"
        private const val PROP_RELATED_VIDEOS_ITEMS: String = "items"
        private const val PROP_RELATED_VIDEOS_HEAD_INDEX: String = "headIndex"
        private const val PROP_RELATED_VIDEOS_HAS_MORE: String = "hasMore"
        private const val PROP_RELATED_VIDEOS_SUBTITLE: String = "subtitle"
        private const val PROP_IS_FAVOURITE: String = "isFavourite"
        private const val PROP_SKIP_MARKERS: String = "skipMarkers"
        private const val PROP_MULTI_VIEW_MODE: String = "mutliViewMode"

        private const val COMMAND_SEEK_TO_NOW: Int = 1
        private const val COMMAND_SEEK_TO_TIMESTAMP: Int = 2
        private const val COMMAND_SEEK_TO_RESUME_POSITION: Int = 3
        private const val COMMAND_SEEK_TO_POSITION: Int = 4
        private const val COMMAND_REPLACE_AD_TAG_PARAMETERS: Int = 5
        private const val COMMAND_LIMIT_SEEKABLE_RANGE: Int = 6
    }

    private val primaryViewManager: ReactTVExoplayerViewManager = ReactTVExoplayerViewManager(reactApplicationContext)
    private lateinit var primaryView: ReactTVExoplayerView
    private lateinit var rootView: ReactTvMultipleExoplayerView

    private val deviceEventEmitter = reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)

    override fun getName(): String = "RCTVideo"

    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any>? = primaryViewManager.exportedCustomDirectEventTypeConstants

    override fun getExportedViewConstants(): Map<String, Any>? = primaryViewManager.exportedViewConstants

    override fun getCommandsMap(): Map<String, Int>? = primaryViewManager.commandsMap

    @Deprecated("Deprecated in Java")
    override fun receiveCommand(root: ReactTvMultipleExoplayerView, commandId: Int, args: ReadableArray?) {
        primaryViewManager.receiveCommand(primaryView, commandId, args)
    }

    override fun createViewInstance(context: ThemedReactContext): ReactTvMultipleExoplayerView {
        primaryView = primaryViewManager.createViewInstance(context)
        rootView = ReactTvMultipleExoplayerView(context)
        rootView.addView(primaryView)
        return rootView
    }

    override fun onDropViewInstance(view: ReactTvMultipleExoplayerView) {
        view.dropView()
        primaryViewManager.onDropViewInstance(primaryView)
    }

    private val eventEmitter: VideoEventEmitter = VideoEventEmitter(reactApplicationContext)

    @ReactProp(name = PROP_SRC)
    fun setSrc(videoView: ReactTvMultipleExoplayerView, src: ReadableMap?) {
        rootView.setSrc(src)
        primaryViewManager.setSrc(primaryView, src)

        Handler(Looper.getMainLooper()).postDelayed({
            Toast.makeText(reactApplicationContext, "onSetMultiViewMode", Toast.LENGTH_SHORT).show()
            eventEmitter.setMultiViewMode(true)
        }, 5000L)
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
    fun setMultiVideos(videoView: ReactTvMultipleExoplayerView, list: ReadableArray?) {
        Log.d(this.javaClass.simpleName, "multiViewSources: $list")
        if (list == null || list.size() == 0) return
        val manager = ReactTVExoplayerViewManager(reactApplicationContext)
        val playerView = manager.createViewInstance(rootView.themedReactContext)
        manager.setSrc(playerView, list.getMap(list.size() - 1))
        rootView.addView(playerView)
    }

    @ReactProp(name = PROP_MULTI_VIEW_MODE)
    fun setMultiViewMode(videoView: ReactTvMultipleExoplayerView, multiViewMode: Boolean) {
        Log.d(this.javaClass.simpleName, "setMultiViewMode: $multiViewMode")
    }
}