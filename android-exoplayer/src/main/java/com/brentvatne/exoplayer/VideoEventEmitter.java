package com.brentvatne.exoplayer;

import android.view.View;

import androidx.annotation.StringDef;
import androidx.media3.common.Metadata;
import androidx.media3.extractor.metadata.id3.Id3Frame;
import androidx.media3.extractor.metadata.id3.TextInformationFrame;

import com.facebook.react.ReactApplication;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

class VideoEventEmitter {

    private int viewId = View.NO_ID;
    private ReactContext reactContext;

    VideoEventEmitter(ReactContext reactContext) {
        this.reactContext = reactContext;
    }

    private static final String EVENT_LOAD_START = "onVideoLoadStart";
    private static final String EVENT_LOAD = "onVideoLoad";
    private static final String EVENT_ERROR = "onVideoError";
    private static final String EVENT_PROGRESS = "onVideoProgress";
    private static final String EVENT_SEEK = "onVideoSeek";
    private static final String EVENT_END = "onVideoEnd";
    private static final String EVENT_FULLSCREEN_WILL_PRESENT = "onVideoFullscreenPlayerWillPresent";
    private static final String EVENT_FULLSCREEN_DID_PRESENT = "onVideoFullscreenPlayerDidPresent";
    private static final String EVENT_FULLSCREEN_WILL_DISMISS = "onVideoFullscreenPlayerWillDismiss";
    private static final String EVENT_FULLSCREEN_DID_DISMISS = "onVideoFullscreenPlayerDidDismiss";
    private static final String EVENT_VIDEO_ABOUT_TO_END = "onVideoAboutToEnd";
    private static final String EVENT_REQUIRE_AD_PARAMETERS = "onRequireAdParameters";
    private static final String EVENT_RELOAD_CURRENT_SOURCE = "onReloadCurrentSource";
    private static final String EVENT_BEHIND_LIVE_WINDOW_ERROR = "onBehindLiveWindowError";

    private static final String EVENT_STALLED = "onPlaybackStalled";
    private static final String EVENT_RESUME = "onPlaybackResume";
    private static final String EVENT_READY = "onReadyForDisplay";
    private static final String EVENT_BUFFER = "onVideoBuffer";
    private static final String EVENT_IDLE = "onVideoIdle";
    private static final String EVENT_TIMED_METADATA = "onTimedMetadata";
    private static final String EVENT_AUDIO_BECOMING_NOISY = "onVideoAudioBecomingNoisy";
    private static final String EVENT_AUDIO_FOCUS_CHANGE = "onAudioFocusChanged";
    private static final String EVENT_PLAYBACK_RATE_CHANGE = "onPlaybackRateChange";
    private static final String EVENT_BOTTOM_RIGHT_ICON_CLICK = "onBottomRightIconClick";
    private static final String EVENT_CONTROLS_VISIBILITY_CHANGE = "onControlsVisibilityChange";
    private static final String EVENT_TOUCH_ACTION_MOVE = "onTouchActionMove";
    private static final String EVENT_TOUCH_ACTION_UP = "onTouchActionUp";
    private static final String EVENT_EPG_ICON_CLICK = "onEpgIconClick";
    private static final String EVENT_STATS_ICON_CLICK = "onStatsIconClick";
    private static final String EVENT_RELATED_VIDEO_CLICKED = "onRelatedVideoClicked";
    private static final String EVENT_RELATED_VIDEOS_ICON_CLICKED = "onRelatedVideosIconClicked";
    private static final String EVENT_FAVOURITE_BUTTON_CLICK = "onFavouriteButtonClick";
    private static final String EVENT_WATCHLIST_BUTTON_CLICK = "onWatchlistButtonClick";
    private static final String EVENT_ANNOTATIONS_BUTTON_CLICK = "onAnnotationsButtonClick";
    private static final String EVENT_SUBTITLE_TRACK_CHANGED = "onSubtitleTrackChanged";
    private static final String EVENT_AUDIO_TRACK_CHANGED = "onAudioTrackChanged";

    static final String[] Events = {
            EVENT_LOAD_START,
            EVENT_LOAD,
            EVENT_ERROR,
            EVENT_PROGRESS,
            EVENT_SEEK,
            EVENT_END,
            EVENT_FULLSCREEN_WILL_PRESENT,
            EVENT_FULLSCREEN_DID_PRESENT,
            EVENT_FULLSCREEN_WILL_DISMISS,
            EVENT_FULLSCREEN_DID_DISMISS,
            EVENT_STALLED,
            EVENT_RESUME,
            EVENT_READY,
            EVENT_BUFFER,
            EVENT_IDLE,
            EVENT_TIMED_METADATA,
            EVENT_AUDIO_BECOMING_NOISY,
            EVENT_AUDIO_FOCUS_CHANGE,
            EVENT_PLAYBACK_RATE_CHANGE,
            EVENT_BOTTOM_RIGHT_ICON_CLICK,
            EVENT_CONTROLS_VISIBILITY_CHANGE,
            EVENT_TOUCH_ACTION_MOVE,
            EVENT_TOUCH_ACTION_UP,
            EVENT_EPG_ICON_CLICK,
            EVENT_STATS_ICON_CLICK,
            EVENT_RELATED_VIDEO_CLICKED,
            EVENT_RELATED_VIDEOS_ICON_CLICKED,
            EVENT_VIDEO_ABOUT_TO_END,
            EVENT_FAVOURITE_BUTTON_CLICK,
            EVENT_WATCHLIST_BUTTON_CLICK,
            EVENT_ANNOTATIONS_BUTTON_CLICK,
            EVENT_SUBTITLE_TRACK_CHANGED,
            EVENT_AUDIO_TRACK_CHANGED,
            EVENT_REQUIRE_AD_PARAMETERS,
            EVENT_RELOAD_CURRENT_SOURCE,
            EVENT_BEHIND_LIVE_WINDOW_ERROR
    };

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            EVENT_LOAD_START,
            EVENT_LOAD,
            EVENT_ERROR,
            EVENT_PROGRESS,
            EVENT_SEEK,
            EVENT_END,
            EVENT_FULLSCREEN_WILL_PRESENT,
            EVENT_FULLSCREEN_DID_PRESENT,
            EVENT_FULLSCREEN_WILL_DISMISS,
            EVENT_FULLSCREEN_DID_DISMISS,
            EVENT_STALLED,
            EVENT_RESUME,
            EVENT_READY,
            EVENT_BUFFER,
            EVENT_IDLE,
            EVENT_TIMED_METADATA,
            EVENT_AUDIO_BECOMING_NOISY,
            EVENT_AUDIO_FOCUS_CHANGE,
            EVENT_PLAYBACK_RATE_CHANGE,
            EVENT_BOTTOM_RIGHT_ICON_CLICK,
            EVENT_CONTROLS_VISIBILITY_CHANGE,
            EVENT_TOUCH_ACTION_MOVE,
            EVENT_TOUCH_ACTION_UP,
            EVENT_EPG_ICON_CLICK,
            EVENT_STATS_ICON_CLICK,
            EVENT_RELATED_VIDEO_CLICKED,
            EVENT_RELATED_VIDEOS_ICON_CLICKED,
            EVENT_VIDEO_ABOUT_TO_END,
            EVENT_FAVOURITE_BUTTON_CLICK,
            EVENT_WATCHLIST_BUTTON_CLICK,
            EVENT_ANNOTATIONS_BUTTON_CLICK,
            EVENT_SUBTITLE_TRACK_CHANGED,
            EVENT_AUDIO_TRACK_CHANGED,
            EVENT_REQUIRE_AD_PARAMETERS,
            EVENT_RELOAD_CURRENT_SOURCE,
            EVENT_BEHIND_LIVE_WINDOW_ERROR
    })
    @interface VideoEvents {
    }

    private static final String EVENT_PROP_FAST_FORWARD = "canPlayFastForward";
    private static final String EVENT_PROP_SLOW_FORWARD = "canPlaySlowForward";
    private static final String EVENT_PROP_SLOW_REVERSE = "canPlaySlowReverse";
    private static final String EVENT_PROP_REVERSE = "canPlayReverse";
    private static final String EVENT_PROP_STEP_FORWARD = "canStepForward";
    private static final String EVENT_PROP_STEP_BACKWARD = "canStepBackward";

    private static final String EVENT_PROP_ID = "id";
    private static final String EVENT_PROP_TYPE = "type";
    private static final String EVENT_PROP_DURATION = "duration";

    private static final String EVENT_PROP_PLAYABLE_DURATION = "playableDuration";
    private static final String EVENT_PROP_SEEKABLE_DURATION = "seekableDuration";
    private static final String EVENT_PROP_CURRENT_DATE = "currentDate";
    private static final String EVENT_PROP_CURRENT_TIME = "currentTime";

    private static final String EVENT_PROP_SEEK_TIME = "seekTime";
    private static final String EVENT_PROP_NATURAL_SIZE = "naturalSize";
    private static final String EVENT_PROP_WIDTH = "width";
    private static final String EVENT_PROP_HEIGHT = "height";
    private static final String EVENT_PROP_ORIENTATION = "orientation";
    private static final String EVENT_PROP_AUDIO_TRACKS = "audioTracks";
    private static final String EVENT_PROP_TEXT_TRACKS = "textTracks";
    private static final String EVENT_PROP_HAS_AUDIO_FOCUS = "hasAudioFocus";
    private static final String EVENT_PROP_IS_BUFFERING = "isBuffering";
    private static final String EVENT_PROP_PLAYBACK_RATE = "playbackRate";
    private static final String EVENT_PROP_CONTROLS_VISIBLE = "controlsVisible";
    private static final String EVENT_PROP_TOUCH_ACTION_MOVE_DX = "dx";
    private static final String EVENT_PROP_TOUCH_ACTION_MOVE_DY = "dy";
    private static final String EVENT_PROP_IS_ABOUT_TO_END = "isAboutToEnd";
    private static final String EVENT_PROP_DATE = "date";
    private static final String EVENT_PROP_IS_BLOCKING = "isBlocking";
    private static final String EVENT_PROP_LANGUAGE = "language";

    private static final String EVENT_PROP_ERROR = "error";
    private static final String EVENT_PROP_ERROR_STRING = "errorString";
    private static final String EVENT_PROP_ERROR_EXCEPTION = "errorException";

    private static final String EVENT_PROP_TIMED_METADATA = "metadata";

    void setViewId(int viewId) {
        this.viewId = viewId;
    }

    void loadStart() {
        receiveEvent(EVENT_LOAD_START, null);
    }

    void load(
            double duration, double currentPosition, int videoWidth, int videoHeight,
            WritableArray audioTracks, WritableArray textTracks) {
        WritableMap event = Arguments.createMap();
        event.putDouble(EVENT_PROP_DURATION, duration / 1000D);
        event.putDouble(EVENT_PROP_CURRENT_TIME, currentPosition / 1000D);

        WritableMap naturalSize = Arguments.createMap();
        naturalSize.putInt(EVENT_PROP_WIDTH, videoWidth);
        naturalSize.putInt(EVENT_PROP_HEIGHT, videoHeight);
        if (videoWidth > videoHeight) {
            naturalSize.putString(EVENT_PROP_ORIENTATION, "landscape");
        } else {
            naturalSize.putString(EVENT_PROP_ORIENTATION, "portrait");
        }
        event.putMap(EVENT_PROP_NATURAL_SIZE, naturalSize);

        event.putArray(EVENT_PROP_AUDIO_TRACKS, audioTracks);
        event.putArray(EVENT_PROP_TEXT_TRACKS, textTracks);

        // TODO: Actually check if you can.
        event.putBoolean(EVENT_PROP_FAST_FORWARD, true);
        event.putBoolean(EVENT_PROP_SLOW_FORWARD, true);
        event.putBoolean(EVENT_PROP_SLOW_REVERSE, true);
        event.putBoolean(EVENT_PROP_REVERSE, true);
        event.putBoolean(EVENT_PROP_FAST_FORWARD, true);
        event.putBoolean(EVENT_PROP_STEP_BACKWARD, true);
        event.putBoolean(EVENT_PROP_STEP_FORWARD, true);

        receiveEvent(EVENT_LOAD, event);
    }

    void progressChanged(
            double currentDate,
            double currentPosition,
            double bufferedDuration,
            double seekableDuration) {
        WritableMap event = Arguments.createMap();
        event.putDouble(EVENT_PROP_CURRENT_DATE, currentDate / 1000D);
        event.putDouble(EVENT_PROP_CURRENT_TIME, currentPosition / 1000D);
        event.putDouble(EVENT_PROP_PLAYABLE_DURATION, bufferedDuration / 1000D);
        event.putDouble(EVENT_PROP_SEEKABLE_DURATION, seekableDuration / 1000D);
        receiveEvent(EVENT_PROGRESS, event);
    }

    void seek(long currentPosition, long seekTime) {
        WritableMap event = Arguments.createMap();
        event.putDouble(EVENT_PROP_CURRENT_TIME, currentPosition / 1000D);
        event.putDouble(EVENT_PROP_SEEK_TIME, seekTime / 1000D);
        receiveEvent(EVENT_SEEK, event);
    }

    void ready() {
        receiveEvent(EVENT_READY, null);
    }

    void buffering(boolean isBuffering) {
        WritableMap map = Arguments.createMap();
        map.putBoolean(EVENT_PROP_IS_BUFFERING, isBuffering);
        receiveEvent(EVENT_BUFFER, map);
    }

    void idle() {
        receiveEvent(EVENT_IDLE, null);
    }

    void end() {
        receiveEvent(EVENT_END, null);
    }

    void endLiveChannelAsVod() {
        WritableMap map = Arguments.createMap();
        map.putString(EVENT_PROP_TYPE, "VOD");
        receiveEvent(EVENT_END, map);
    }

    void fullscreenWillPresent() {
        receiveEvent(EVENT_FULLSCREEN_WILL_PRESENT, null);
    }

    void fullscreenDidPresent() {
        receiveEvent(EVENT_FULLSCREEN_DID_PRESENT, null);
    }

    void fullscreenWillDismiss() {
        receiveEvent(EVENT_FULLSCREEN_WILL_DISMISS, null);
    }

    void fullscreenDidDismiss() {
        receiveEvent(EVENT_FULLSCREEN_DID_DISMISS, null);
    }

    void error(String errorString, Exception exception) {
        WritableMap error = Arguments.createMap();
        error.putString(EVENT_PROP_ERROR_STRING, errorString);
        error.putString(EVENT_PROP_ERROR_EXCEPTION, android.util.Log.getStackTraceString(exception));
        WritableMap event = Arguments.createMap();
        event.putMap(EVENT_PROP_ERROR, error);
        receiveEvent(EVENT_ERROR, event);
    }

    void playbackRateChange(float rate) {
        WritableMap map = Arguments.createMap();
        map.putDouble(EVENT_PROP_PLAYBACK_RATE, (double) rate);
        receiveEvent(EVENT_PLAYBACK_RATE_CHANGE, map);
    }

    void bottomRightIconClick() {
        receiveEvent(EVENT_BOTTOM_RIGHT_ICON_CLICK, null);
    }

    void epgIconClick() {
        receiveEvent(EVENT_EPG_ICON_CLICK, null);
    }

    void statsIconClick() {
        receiveEvent(EVENT_STATS_ICON_CLICK, null);
    }

    void relatedVideoClick(int id, String type) {
        WritableMap map = Arguments.createMap();
        map.putInt(EVENT_PROP_ID, id);
        map.putString(EVENT_PROP_TYPE, type);
        receiveEvent(EVENT_RELATED_VIDEO_CLICKED, map);
    }

    void relatedVideosIconClicked() {
        receiveEvent(EVENT_RELATED_VIDEOS_ICON_CLICKED, null);
    }

    void controlsVisibilityChange(boolean visible) {
        WritableMap map = Arguments.createMap();
        map.putBoolean(EVENT_PROP_CONTROLS_VISIBLE, visible);
        receiveEvent(EVENT_CONTROLS_VISIBILITY_CHANGE, map);
    }

    void touchActionMove(double dx, double dy) {
        WritableMap map = Arguments.createMap();
        map.putDouble(EVENT_PROP_TOUCH_ACTION_MOVE_DX, dx);
        map.putDouble(EVENT_PROP_TOUCH_ACTION_MOVE_DY, dy);
        receiveEvent(EVENT_TOUCH_ACTION_MOVE, map);
    }

    void touchActionUp() {
        receiveEvent(EVENT_TOUCH_ACTION_UP, null);
    }

    void timedMetadata(Metadata metadata) {
        WritableArray metadataArray = null;
        for (int i = 0; i < metadata.length(); i++) {
            Metadata.Entry entry = metadata.get(i);
            if (!(entry instanceof Id3Frame)) {
                continue;
            }

            if (metadataArray == null) {
                metadataArray = Arguments.createArray();
            }
            String value = "";
            Id3Frame frame = (Id3Frame) entry;
            if (frame instanceof TextInformationFrame) {
                TextInformationFrame txxxFrame = (TextInformationFrame) frame;
                value = txxxFrame.value;
            }
            String identifier = frame.id;
            WritableMap map = Arguments.createMap();
            map.putString("identifier", identifier);
            map.putString("value", value);
            metadataArray.pushMap(map);
        }

        if (metadataArray == null) {
            return;
        }
        WritableMap event = Arguments.createMap();
        event.putArray(EVENT_PROP_TIMED_METADATA, metadataArray);
        receiveEvent(EVENT_TIMED_METADATA, event);
    }

    void audioFocusChanged(boolean hasFocus) {
        WritableMap map = Arguments.createMap();
        map.putBoolean(EVENT_PROP_HAS_AUDIO_FOCUS, hasFocus);
        receiveEvent(EVENT_AUDIO_FOCUS_CHANGE, map);
    }

    void audioBecomingNoisy() {
        receiveEvent(EVENT_AUDIO_BECOMING_NOISY, null);
    }

    private void receiveEvent(@VideoEvents String type, WritableMap event) {
        // Using current react context to create RCTEventEmitter solves broken bridge issue after JS reload
        ((ReactApplication) reactContext.getApplicationContext()).getReactNativeHost().getReactInstanceManager()
                .getCurrentReactContext().getJSModule(RCTEventEmitter.class).receiveEvent(viewId, type, event);
    }

    void videoAboutToEnd(boolean isAboutToEnd) {
        WritableMap map = Arguments.createMap();
        map.putBoolean(EVENT_PROP_IS_ABOUT_TO_END, isAboutToEnd);
        receiveEvent(EVENT_VIDEO_ABOUT_TO_END, map);
    }

    void favouriteButtonClick() {
        receiveEvent(EVENT_FAVOURITE_BUTTON_CLICK, null);
    }

    void watchlistButtonClick() {
        receiveEvent(EVENT_WATCHLIST_BUTTON_CLICK, null);
    }

    void annotationsButtonClick() {
        receiveEvent(EVENT_ANNOTATIONS_BUTTON_CLICK, null);
    }

    void subtitleTrackChanged(String language) {
        WritableMap map = Arguments.createMap();
        map.putString(EVENT_PROP_LANGUAGE, language);
        receiveEvent(EVENT_SUBTITLE_TRACK_CHANGED, map);
    }

    void audioTrackChanged(String language) {
        WritableMap map = Arguments.createMap();
        map.putString(EVENT_PROP_LANGUAGE, language);
        receiveEvent(EVENT_AUDIO_TRACK_CHANGED, map);
    }

    void requireAdParameters(double date, boolean isBlocking) {
        WritableMap map = Arguments.createMap();
        map.putDouble(EVENT_PROP_DATE, date);
        map.putBoolean(EVENT_PROP_IS_BLOCKING, isBlocking);
        receiveEvent(EVENT_REQUIRE_AD_PARAMETERS, map);
    }

    void reloadCurrentSource(String id, String type) {
        WritableMap event = Arguments.createMap();
        event.putString(EVENT_PROP_ID, id);
        event.putString(EVENT_PROP_TYPE, type);
        receiveEvent(EVENT_RELOAD_CURRENT_SOURCE, event);
    }

    void behindLiveWindowError() {
        receiveEvent(EVENT_BEHIND_LIVE_WINDOW_ERROR, null);
    }
}
