//
//  RNNewPlayerView.swift
//  RNDReactNativeDiceVideo
//
//  Created by Yaroslav Lvov on 05.03.2021.
//

import React
import AVDoris
import AVKit
import RNDReactNativeDiceVideo

class NewPlayerView: UIView, JSInputProtocol {
    var jsBridge: RCTBridge?
    //Events
    //used
    @objc var onBackButton: RCTBubblingEventBlock?
    @objc var onVideoLoadStart: RCTBubblingEventBlock?
    @objc var onVideoLoad: RCTBubblingEventBlock?
    @objc var onVideoError: RCTBubblingEventBlock?
    @objc var onVideoProgress: RCTBubblingEventBlock?
    @objc var onVideoEnd: RCTBubblingEventBlock?
    @objc var onPlaybackRateChange: RCTBubblingEventBlock?
    @objc var onRequireAdParameters: RCTBubblingEventBlock?
    @objc var onRelatedVideoClicked: RCTBubblingEventBlock?
    @objc var onSubtitleTrackChanged: RCTBubblingEventBlock?
    
    //not used
    @objc var onVideoBuffer: RCTBubblingEventBlock?
    @objc var onVideoSeek: RCTBubblingEventBlock?
    @objc var onTimedMetadata: RCTBubblingEventBlock?
    @objc var onVideoAudioBecomingNoisy: RCTBubblingEventBlock?
    @objc var onVideoFullscreenPlayerWillPresent: RCTBubblingEventBlock?
    @objc var onVideoFullscreenPlayerDidPresent: RCTBubblingEventBlock?
    @objc var onVideoFullscreenPlayerWillDismiss: RCTBubblingEventBlock?
    @objc var onVideoFullscreenPlayerDidDismiss: RCTBubblingEventBlock?
    @objc var onReadyForDisplay: RCTBubblingEventBlock?
    @objc var onPlaybackStalled: RCTBubblingEventBlock?
    @objc var onPlaybackResume: RCTBubblingEventBlock?
    @objc var onVideoAboutToEnd: RCTBubblingEventBlock?
    @objc var onFavouriteButtonClick: RCTBubblingEventBlock?
    @objc var onRelatedVideosIconClicked: RCTBubblingEventBlock?
    @objc var onStatsIconClick: RCTBubblingEventBlock?
    @objc var onEpgIconClick: RCTBubblingEventBlock?
    @objc var onAnnotationsButtonClick: RCTBubblingEventBlock?
    
    
    
    //Props
    //MARK: Differs (source)
    @objc var src: NSDictionary? {
        didSet {
            if let source = try? Source(dict: src), source.uri.absoluteString != jsProps.source.value?.uri.absoluteString {
                jsProps.source.value = try? Source(dict: src)
            }
        }
    }
    @objc var partialVideoInformation: NSDictionary? {
        didSet { jsProps.partialVideoInformation.value = try? PartialVideoInformation(dict: partialVideoInformation) } }
    @objc var translations: NSDictionary? {
        didSet { jsProps.translations.value = try? Translations(dict: translations) } }
    @objc var buttons: NSDictionary? {
        didSet { jsProps.buttons.value = try? Buttons(dict: buttons) } }
    @objc var theme: NSDictionary? {
        didSet { jsProps.theme.value = try? Theme(dict: theme) } }
    @objc var relatedVideos: NSDictionary? {
        didSet { jsProps.relatedVideos.value = try? RelatedVideos(dict: relatedVideos) } }
    @objc var metadata: NSDictionary? {
        didSet { jsProps.metadata.value = try? Metadata(dict: metadata) } }
    @objc var overlayConfig: NSDictionary? {
        didSet { jsProps.overlayConfig.value = try? OverlayConfig(dict: overlayConfig) } }
    @objc var isFavourite: Bool = false {
        didSet {
            jsPlayerView?.dorisGlue?.doris?.viewModel.toggles.isFavourite = isFavourite
            jsProps.isFavourite.value = isFavourite
        }
    }
    @objc var controls: Bool = false {
        didSet {
            jsPlayerView?.dorisGlue?.doris?.viewModel.toggles.isUIEnabled = controls
            jsProps.controls.value = controls
        }
    }
    @objc var nowPlaying: NSDictionary? {
        didSet {
            jsPlayerView?.nowPlaying = nowPlaying
            jsProps.nowPlaying.value = try? JSNowPlaying(dict: nowPlaying)
        }
    }
    
    //FIXME: review unused variables
    @objc var selectedTextTrack: NSDictionary?
    @objc var selectedAudioTrack: NSDictionary?
    @objc var seek: NSDictionary?
    @objc var playNextSource: NSDictionary?
    @objc var playlist: NSDictionary?
    @objc var annotations: NSArray?
    @objc var playNextSourceTimeoutMillis: NSNumber?
    @objc var resizeMode: NSString?
    @objc var textTracks: NSArray?
    @objc var ignoreSilentSwitch: NSString?
    @objc var volume: NSNumber?
    @objc var rate: NSNumber?
    @objc var currentTime: NSNumber?
    @objc var progressUpdateInterval: NSNumber?

    @objc var isFullScreen: Bool = false
    @objc var allowAirplay: Bool = false
    @objc var isAnnotationsOn: Bool = false
    @objc var isStatsOpen: Bool = false
    @objc var isJSOverlayShown: Bool = false
    @objc var canMinimise: Bool = false
    @objc var allowsExternalPlayback: Bool = false
    @objc var muted: Bool = false
    @objc var playInBackground: Bool = true
    @objc var playWhenInactive: Bool = true
    @objc var fullscreen: Bool = false
    @objc var `repeat`: Bool = false
    @objc var paused: Bool = false {
        didSet { paused ? jsPlayerView?.dorisGlue?.doris?.player.pause() : jsPlayerView?.dorisGlue?.doris?.player.play() }
    }
//    var jsDoris: JSDoris?
    var jsProps = JSProps()
    var jsPlayerView: RNDReactNativeDiceVideo.JSPlayerView?
    
    func seekToNow() {
        //TODO
    }
    
    func seekToTimestamp(isoDate: String) {
        //TODO
    }
    
    //TODO: pass this value as part of source
    func seekToPosition(position: Double) {
        jsPlayerView?.dorisGlue?.doris?.player.seek(.position(position))
    }
    
    func replaceAdTagParameters(payload: NSDictionary) {
        if let payload = payload as? Dictionary<String, Any> {
            jsPlayerView?.replaceAdTagParameters(adTagParameters: payload, validFrom: nil, validUntil: nil)
        }
    }

    private func setupDoris() {
        if let jsBridge = self.jsBridge {
            let jsProbs = PlayerViewProxy.convertRNVideoJSPropsToRNDV(jsProps: self.jsProps)
            var jsPlayerView = RNDReactNativeDiceVideo.JSPlayerView(overlayBuilder: JSOverlayBuilder(bridge: jsBridge), jsProps: jsProbs)
            self.addSubview(jsPlayerView)
            jsPlayerView.setRCTBubblingEventBlock(onVideoProgress: self.onVideoProgress,
                                                  onBackButton: self.onBackButton,
                                                  onVideoError: self.onVideoError,
                                                  onRequestPlayNextSource: self.onRelatedVideoClicked,
                                                  onFullScreenButton: nil,
                                                  onVideoStart: self.onVideoLoadStart,
                                                  onVideoEnded: self.onVideoEnd,
                                                  onVideoPaused: self.onPlaybackRateChange,
                                                  onRequireAdParameters: self.onRequireAdParameters,
                                                  onVideoLoad: self.onVideoLoad,
                                                  onVideoStalled: nil,
                                                  onAdBreakStarted: nil,
                                                  onAdStarted: nil,
                                                  onAdEnded: nil,
                                                  onAdBreakEnded: nil,
                                                  onSeekToLive: nil,
                                                  onAdPause: nil,
                                                  onAdResume: nil,
                                                  onSubtitleTrackChanged: self.onSubtitleTrackChanged,
                                                  onAudioTrackChanged: nil,
                                                  onSeekEvent: nil,
                                                  onPlaylistEvent: nil,
                                                  onPlaybackQualityChanged: nil,
                                                  onShareButton: nil,
                                                  onRequestHighlightUrl: nil)
            
            jsPlayerView.translatesAutoresizingMaskIntoConstraints = false
            let leading = jsPlayerView.leadingAnchor.constraint(equalTo: self.leadingAnchor)
            let trailing = jsPlayerView.trailingAnchor.constraint(equalTo: self.trailingAnchor)
            let top = jsPlayerView.topAnchor.constraint(equalTo: self.topAnchor)
            let bottom = jsPlayerView.bottomAnchor.constraint(equalTo: self.bottomAnchor)
            self.addConstraints([leading, trailing, top, bottom])
            
            self.jsPlayerView = jsPlayerView
        }
    }
    
    func setInitialSeek(position: Double) {
        jsProps.startAt.value = position
    }
    
    func setupLimitedSeekableRange(with range: JSLimitedSeekableRange?) {
        let start = Date(timeIntervalSince1970InMilliseconds: range?.start)
        let end = Date(timeIntervalSince1970InMilliseconds: range?.end)
        if let end = end, end > Date() {
            //avoid finishing playback when ongoing live program reaches its end
            jsPlayerView?.dorisGlue?.doris?.player.setLimitedSeekableRange(range: (start: start, end: nil))
        } else {
            jsPlayerView?.dorisGlue?.doris?.player.setLimitedSeekableRange(range: (start: start, end: end))
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        if jsPlayerView == nil {
            setupDoris()
        }
    }
}

