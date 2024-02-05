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
    @objc var onVideoLoad: RCTBubblingEventBlock?
    @objc var onVideoError: RCTBubblingEventBlock?
    @objc var onVideoProgress: RCTBubblingEventBlock?
    @objc var onVideoEnd: RCTBubblingEventBlock?
    @objc var onPlaybackRateChange: RCTBubblingEventBlock?
    @objc var onRequireAdParameters: RCTBubblingEventBlock?
    @objc var onRelatedVideoClicked: RCTBubblingEventBlock?
    @objc var onSubtitleTrackChanged: RCTBubblingEventBlock?
    @objc var onVideoBuffer: RCTBubblingEventBlock?
    @objc var onVideoAboutToEnd: RCTBubblingEventBlock?
    @objc var onFavouriteButtonClick: RCTBubblingEventBlock?
    @objc var onRelatedVideosIconClicked: RCTBubblingEventBlock?
    @objc var onStatsIconClick: RCTBubblingEventBlock?
    @objc var onEpgIconClick: RCTBubblingEventBlock?
    @objc var onAnnotationsButtonClick: RCTBubblingEventBlock?
    @objc var onWatchlistButtonClick: RCTBubblingEventBlock?
    
    //not used
    @objc var onVideoLoadStart: RCTBubblingEventBlock?
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
        DorisLogger.logFilter = DorisLogType.allCases
        if let jsBridge = self.jsBridge {
            let jsProbs = PlayerViewProxy.convertRNVideoJSPropsToRNDV(jsProps: self.jsProps)
            let jsPlayerView = RNDReactNativeDiceVideo.JSPlayerView(overlayBuilder: RNVJSOverlayBuilder(bridge: jsBridge), jsProps: jsProbs)
            self.addSubview(jsPlayerView)
            
            jsPlayerView.onVideoProgress = { [weak self] value in
                if let currentTime = value?["currentTime"] as? Double {
                    self?.onVideoProgress?(["currentTime": currentTime])
                }
            }
            jsPlayerView.onBackButton = self.onBackButton
            jsPlayerView.onVideoError = self.onVideoError
            jsPlayerView.onRequestPlayNextSource = { [weak self] value in
                if let id = value?["id"] as? String, let type = value?["type"] as? String {
                    self?.onRelatedVideoClicked?(["id": id, "type": type])
               }
            }
            jsPlayerView.onVideoEnded = self.onVideoEnd
            jsPlayerView.onVideoPaused = { [weak self] value in
                if let isPaused = value?["isPaused"] as? Bool {
                    self?.onPlaybackRateChange?(["playbackRate": isPaused ? 0.0 : 1.0])
               }
            }
            jsPlayerView.onRequireAdParameters = self.onRequireAdParameters
            jsPlayerView.onVideoLoad = self.onVideoLoad
            jsPlayerView.onSubtitleTrackChanged = self.onSubtitleTrackChanged
            jsPlayerView.onVideoBuffer = self.onVideoBuffer
            jsPlayerView.onVideoAboutToEnd = self.onVideoAboutToEnd
            jsPlayerView.onFavouritesButton =  self.onFavouriteButtonClick
            jsPlayerView.onRelatedVideosIcon =  self.onRelatedVideosIconClicked
            jsPlayerView.onStatsIcon =  self.onStatsIconClick
            jsPlayerView.onEpgIcon =  self.onEpgIconClick
            jsPlayerView.onAnnotationsButton =  self.onAnnotationsButtonClick
            jsPlayerView.onWatchlistButton =  self.onWatchlistButtonClick
            
            jsPlayerView.translatesAutoresizingMaskIntoConstraints = false
            jsPlayerView.leftAnchor.constraint(equalTo: self.leftAnchor, constant: 0).isActive = true
            jsPlayerView.rightAnchor.constraint(equalTo: self.rightAnchor, constant: 0).isActive = true
            jsPlayerView.topAnchor.constraint(equalTo: self.topAnchor, constant: 0).isActive = true
            jsPlayerView.bottomAnchor.constraint(equalTo: self.bottomAnchor, constant: 0).isActive = true
            self.jsPlayerView = jsPlayerView
        }
    }
    
    func setInitialSeek(position: Double) {
        jsProps.startAt.value = position
    }
    
    func setupLimitedSeekableRange(with range: Source.LimitedSeekableRange?) {
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


class RNVJSOverlayBuilder: OverlayBuilderProtocol {
    private let bridge: RCTBridge
    
    init(bridge: RCTBridge) {
        self.bridge = bridge
    }
    
    func buildOverlay(from config: JSOverlayConfig?, tvxManager: TvxManagerProtocol?) -> (type: OverlayType,
                                                                                          button: String?,
                                                                                          setupAction: (() -> Void)?,
                                                                                          cleanupAction: (() -> Void)?) {
        guard let config = config else { return (.none, nil, nil, nil) }
        
        switch config.type {
        case .expanded:
            guard
                let sideComponent = config.components.first(where: {$0.type == .side}),
                let bottomComponent = config.components.first(where: {$0.type == .bottom})
            else {
                return (.none, nil, nil, nil)
            }
            
            let initialSideComponentProps = ["componentId": sideComponent.initialProps?["componentId"] ?? ""]
            let initialBottomComponentProps = ["componentId": bottomComponent.initialProps?["componentId"] ?? ""]
            
            let sideJSComponentView = RCTRootView(bridge: bridge,
                                                  moduleName: sideComponent.name,
                                                  initialProperties: initialSideComponentProps)
            sideJSComponentView.backgroundColor = .clear
            
            let bottomJSComponentView = RCTRootView(bridge: bridge,
                                                    moduleName: bottomComponent.name,
                                                    initialProperties: initialBottomComponentProps)
            bottomJSComponentView.backgroundColor = .clear
            
            let overlayType = OverlayType.rightAndBottom(rightView: sideJSComponentView,
                                                         bottomView: bottomJSComponentView,
                                                         closeAction: nil)
            
            let setupAction: () -> Void = {
                sideJSComponentView.appProperties = sideComponent.initialProps
                bottomJSComponentView.appProperties = bottomComponent.initialProps
            }
            
            let cleanupAction: () -> Void = {
                sideJSComponentView.appProperties = initialSideComponentProps
                bottomJSComponentView.appProperties = initialBottomComponentProps
            }
            
            return (type: overlayType,
                    button: config.buttonIconUrl,
                    setupAction: setupAction,
                    cleanupAction: cleanupAction)
                    
        case .side:
            guard
                let sideComponent = config.components.first(where: {$0.type == .side})
            else {
                return (.none, nil, nil, nil)
            }
            let initialSideComponentProps = ["componentId": sideComponent.initialProps?["componentId"] ?? ""]

            let sideJSComponentView = RCTRootView(bridge: bridge,
                                                  moduleName: sideComponent.name,
                                                  initialProperties: initialSideComponentProps)
            sideJSComponentView.backgroundColor = .clear

            let overlayType = OverlayType.right(rightView: sideJSComponentView, closeAction: nil)
            
            let setupAction: () -> Void = {
                sideJSComponentView.appProperties = sideComponent.initialProps
            }
            
            let cleanupAction: () -> Void = {
                sideJSComponentView.appProperties = initialSideComponentProps
            }
            
            return (type: overlayType,
                    button: config.buttonIconUrl,
                    setupAction: setupAction,
                    cleanupAction: cleanupAction)
            
        default: return (.none, nil, nil, nil)
        }
    }
}
