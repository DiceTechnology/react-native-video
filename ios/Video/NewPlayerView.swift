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
    @objc var onBackButton: RCTBubblingEventBlock?
    @objc var onVideoLoadStart: RCTBubblingEventBlock?
    @objc var onVideoLoad: RCTBubblingEventBlock?
    @objc var onVideoBuffer: RCTBubblingEventBlock?
    @objc var onVideoError: RCTBubblingEventBlock?
    @objc var onVideoProgress: RCTBubblingEventBlock?
    @objc var onVideoSeek: RCTBubblingEventBlock?
    @objc var onVideoEnd: RCTBubblingEventBlock?
    @objc var onTimedMetadata: RCTBubblingEventBlock?
    @objc var onVideoAudioBecomingNoisy: RCTBubblingEventBlock?
    @objc var onVideoFullscreenPlayerWillPresent: RCTBubblingEventBlock?
    @objc var onVideoFullscreenPlayerDidPresent: RCTBubblingEventBlock?
    @objc var onVideoFullscreenPlayerWillDismiss: RCTBubblingEventBlock?
    @objc var onVideoFullscreenPlayerDidDismiss: RCTBubblingEventBlock?
    @objc var onReadyForDisplay: RCTBubblingEventBlock?
    @objc var onPlaybackStalled: RCTBubblingEventBlock?
    @objc var onPlaybackResume: RCTBubblingEventBlock?
    @objc var onPlaybackRateChange: RCTBubblingEventBlock?
    @objc var onRequireAdParameters: RCTBubblingEventBlock?
    @objc var onVideoAboutToEnd: RCTBubblingEventBlock?
    @objc var onFavouriteButtonClick: RCTBubblingEventBlock?
    @objc var onRelatedVideoClicked: RCTBubblingEventBlock?
    @objc var onRelatedVideosIconClicked: RCTBubblingEventBlock?
    @objc var onStatsIconClick: RCTBubblingEventBlock?
    @objc var onEpgIconClick: RCTBubblingEventBlock?
    @objc var onAnnotationsButtonClick: RCTBubblingEventBlock?
    @objc var onSubtitleTrackChanged: RCTBubblingEventBlock?
    
    
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
        didSet { jsProps.isFavourite.value = isFavourite } }
    @objc var controls: Bool = false {
        didSet { jsProps.controls.value = controls } }
    @objc var nowPlaying: NSDictionary? {
        didSet { jsProps.nowPlaying.value = try? JSNowPlaying(dict: nowPlaying) } }
    

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
//        jsDoris?.doris?.player.seek(.position(position))
    }
    
    func replaceAdTagParameters(payload: NSDictionary) {
//        jsPlayerView?.replaceAdTagParameters(adTagParameters: AdTagParameters(payload: payload), validFrom: nil, validUntil: nil)
//        jsDoris?.replaceAdTagParameters(parameters: AdTagParameters(payload: payload),
//                                        extraInfo: AdTagParametersModifierInfo(viewWidth: frame.width,
//                                                                               viewHeight: frame.height))
    }
    
    func convertRNVideoJSPropsToRNDV() -> RNDReactNativeDiceVideo.JSProps {
        let rndvJsProps = RNDReactNativeDiceVideo.JSProps()
        rndvJsProps.isFullScreen.value = true
        rndvJsProps.isMinimised.value = false
        rndvJsProps.highlightUrl.value = nil
        
        var rndvJSSource: RNDReactNativeDiceVideo.JSSource?
        if let sourceValue = self.jsProps.source.value {
            rndvJsProps.nowPlaying.value = RNDReactNativeDiceVideo.JSNowPlaying(
                title: sourceValue.nowPlaying?.title,
                channelLogoUrl: sourceValue.nowPlaying?.channelLogoUrl,
                startDate: sourceValue.nowPlaying?.startDate,
                endDate: sourceValue.nowPlaying?.endDate)
            var rndvJSIma: RNDReactNativeDiceVideo.JSIma?
            if let sourceIma = sourceValue.ima {
                rndvJSIma = RNDReactNativeDiceVideo.JSIma(
                    videoId: sourceIma.videoId,
                    adTagParameters: sourceIma.adTagParameters,
                    endDate: sourceIma.endDate,
                    startDate: sourceIma.startDate,
                    assetKey: sourceIma.assetKey,
                    contentSourceId: sourceIma.contentSourceId,
                    authToken: sourceIma.authToken)
            }
            var rndvJSDrm: RNDReactNativeDiceVideo.JSDrm?
            if let drm = sourceValue.drm {
                rndvJSDrm = RNDReactNativeDiceVideo.JSDrm(
                    contentUrl: drm.contentUrl,
                    drmScheme: drm.drmScheme,
                    offlineLicense: nil,
                    id: drm.id,
                    croToken: drm.croToken,
                    licensingServerUrl: drm.licensingServerUrl)
            }
            let jsPartialVideoInformation = JSPartialVideoInformation(
                title: sourceValue.partialVideoInformation?.title,
                imageUri: sourceValue.partialVideoInformation?.imageUri)
            
            let jsMuxData = JSConfig.JSMuxData(
                envKey: sourceValue.config.muxData.envKey,
                videoTitle: sourceValue.config.muxData.videoTitle,
                viewerUserId: sourceValue.config.muxData.viewerUserId,
                playerVersion: nil,
                videoId: sourceValue.config.muxData.videoId,
                playerName: sourceValue.config.muxData.playerName,
                videoStreamType: sourceValue.config.muxData.videoStreamType,
                subPropertyId: sourceValue.config.muxData.subPropertyId,
                videoIsLive: sourceValue.config.muxData.videoIsLive,
                experimentName: sourceValue.config.muxData.experimentName,
                videoSeries: nil,
                videoCdn: nil,
                videoDuration: nil)
            let jsBeacon = JSConfig.JSBeacon(
                authUrl: sourceValue.config.beacon?.authUrl.absoluteString,
                url: sourceValue.config.beacon?.url.absoluteString,
                headers: nil)
            var jsConfig = JSConfig(muxData: jsMuxData, beacon: jsBeacon, convivaData: nil)
            
            var jsAds: RNDReactNativeDiceVideo.JSAds?
            if let adUnits = sourceValue.ads?.adUnits.map({ adUnit -> RNDReactNativeDiceVideo.JSAds.AdUnit in
                let queryParams = adUnit.adManifestParams?.map { param -> RNDReactNativeDiceVideo.JSAds.AdUnit.QueryParam in
                    return RNDReactNativeDiceVideo.JSAds.AdUnit.QueryParam(key: param.key, value: param.value)
                }
                return RNDReactNativeDiceVideo.JSAds.AdUnit(
                    insertionType: RNDReactNativeDiceVideo.JSAds.AdUnit.AdInsertionType(rawValue: adUnit.insertionType.rawValue)!,
                    adFormat: RNDReactNativeDiceVideo.JSAds.AdUnit.AdFormat(rawValue: adUnit.adFormat.rawValue)!,
                    adProvider: RNDReactNativeDiceVideo.JSAds.AdUnit.AdProvider(rawValue: adUnit.adProvider?.rawValue ?? ""),
                    adTagUrl: adUnit.adTagUrl,
                    adManifestParams: queryParams)
            }) {
                jsAds = RNDReactNativeDiceVideo.JSAds(adUnits: adUnits)
            }
            
            var jsSubtitles = [RNDReactNativeDiceVideo.JSSubtitles]()
            if let subtitles = sourceValue.subtitles {
                for subtitle in subtitles {
                    jsSubtitles.append(RNDReactNativeDiceVideo.JSSubtitles(language: subtitle.language, uri: subtitle.uri))
                }
            }
            
            var jsLimitedSeekableRange = RNDReactNativeDiceVideo.JSLimitedSeekableRange(start: sourceValue.limitedSeekableRange?.start, end: sourceValue.limitedSeekableRange?.end, seekToStart: sourceValue.limitedSeekableRange?.seekToStart)
            
            var jsNowPlaying = RNDReactNativeDiceVideo.JSNowPlaying(title: sourceValue.nowPlaying?.title, channelLogoUrl: sourceValue.nowPlaying?.channelLogoUrl, startDate: sourceValue.nowPlaying?.startDate, endDate: sourceValue.nowPlaying?.endDate)
           
            rndvJSSource = RNDReactNativeDiceVideo.JSSource(
                id: sourceValue.id ?? "",
                ima: rndvJSIma,
                uri: sourceValue.uri,
                drm: rndvJSDrm,
                progressUpdateInterval: sourceValue.progressUpdateInterval ?? 6,
                type: sourceValue.type,
                title: sourceValue.title ?? "",
                live: sourceValue.live,
                partialVideoInformation: jsPartialVideoInformation,
                isAudioOnly: sourceValue.isAudioOnly,
                config: jsConfig,
                imageUri: sourceValue.imageUri,
                thumbnailsPreview: sourceValue.thumbnailsPreview,
                resumePosition: nil,
                delay: nil,
                ads: jsAds,
                subtitles: jsSubtitles,
                limitedSeekableRange: jsLimitedSeekableRange,
                selectedAudioTrack: nil,
                selectedSubtitleTrack: sourceValue.selectedSubtitleTrack,
                selectedPlaybackQuality: nil,
                nowPlaying: jsNowPlaying)
        }
        
        var jsTranslations: RNDReactNativeDiceVideo.JSTranslations?
        if let translationsValue = self.jsProps.translations.value {
            var dorisTranslationsViewModel = DorisTranslationsViewModel()
            dorisTranslationsViewModel.play = translationsValue.playerPlayButton
            dorisTranslationsViewModel.pause = translationsValue.playerPauseButton
            dorisTranslationsViewModel.stats = translationsValue.playerStatsButton
            dorisTranslationsViewModel.audioAndSubtitles = translationsValue.playerAudioAndSubtitlesButton
            dorisTranslationsViewModel.live = translationsValue.live
//            dorisTranslationsViewModel.goLive = translationsValue.goLive
            dorisTranslationsViewModel.favourites = translationsValue.favourite
//            dorisTranslationsViewModel.watchlist = translationsValue.watchlist
            dorisTranslationsViewModel.moreVideos = translationsValue.moreVideos
//            dorisTranslationsViewModel.captions = translationsValue.captions
            dorisTranslationsViewModel.rewind = translationsValue.rewind
            dorisTranslationsViewModel.fastForward = translationsValue.fastForward
            dorisTranslationsViewModel.audio = translationsValue.audioTracks
            dorisTranslationsViewModel.info = translationsValue.info
            dorisTranslationsViewModel.adsCountdownAd = translationsValue.adsCountdownAd
            dorisTranslationsViewModel.adsCountdownOf = translationsValue.adsCountdownOf
            dorisTranslationsViewModel.annotations = translationsValue.annotations
            dorisTranslationsViewModel.playingLive = translationsValue.playingLive
            dorisTranslationsViewModel.nowPlaying = translationsValue.nowPlaying
//            dorisTranslationsViewModel.tvPlayerEPG = translationsValue.tvPlayerEPG
            jsTranslations = RNDReactNativeDiceVideo.JSTranslations(beaconTranslations: nil, dorisTranslations: dorisTranslationsViewModel)
        }
        
        let jsButtons = JSButtons(
            fullscreen: self.jsProps.buttons.value?.fullscreen,
            stats: self.jsProps.buttons.value?.stats,
            favourite: self.jsProps.buttons.value?.favourite,
            zoom: self.jsProps.buttons.value?.zoom,
            back: self.jsProps.buttons.value?.back,
            settings: self.jsProps.buttons.value?.settings,
            info: self.jsProps.buttons.value?.info,
            share: nil,
            watchlist: self.jsProps.buttons.value?.watchlist,
            epg: self.jsProps.buttons.value?.epg,
            annotations: self.jsProps.buttons.value?.annotations)
        
        var jsTheme: JSTheme?
        if let themeValue = self.jsProps.theme.value {
            let fonts = JSTheme.JSFonts(
                secondaryFontName: themeValue.fonts.secondary,
                primaryFontName: themeValue.fonts.primary,
                tertiaryFontName: "")
            //need confirm
            let colors = JSTheme.JSColors(
                accentColor: themeValue.colors.primary,
                backgroundColor: themeValue.colors.secondary)
            jsTheme = JSTheme(fonts: fonts, colors: colors)
        }
        
        var jsOverlayConfig: JSOverlayConfig?
        if let overlayConfigValue = self.jsProps.overlayConfig.value {
            let overlayType = JSOverlayConfig.JSOverlayConfigType(rawValue: overlayConfigValue.type.rawValue)
            let buttonIconUrl = overlayConfigValue.button
            let componentsArray = overlayConfigValue.components
            let components: [JSOverlayConfig.JSOverlayComponent] = componentsArray.map { component in
                let componentType = JSOverlayConfig.JSOverlayConfigType(rawValue: component.type.rawValue)
                let name = component.name
                let initialProps = component.initialProps
                return JSOverlayConfig.JSOverlayComponent(type: componentType ?? .side, name: name, initialProps: initialProps)
            }
            jsOverlayConfig = JSOverlayConfig(type: overlayType ?? .side, buttonIconUrl: buttonIconUrl, components: components)
        }
        
        var jsTracksPolicy: RNDReactNativeDiceVideo.JSTracksPolicy?
        if let items = self.jsProps.source.value?.tracksPolicy?.items.map({ trackPolicyPair -> RNDReactNativeDiceVideo.JSTrackPolicyPair in
            return RNDReactNativeDiceVideo.JSTrackPolicyPair(audio: trackPolicyPair.audio, subtitle: trackPolicyPair.subtitle)
        }) {
            jsTracksPolicy = RNDReactNativeDiceVideo.JSTracksPolicy(items: items)
        }
        
        let rndvJSVideoDataConfig = RNDReactNativeDiceVideo.JSVideoData.JSVideoDataConfig(
            translations: jsTranslations,
            buttons: jsButtons,
            theme: jsTheme,
            playlist: nil,
            testIdentifiers: nil,
            annotations: nil,
            overlayConfig: jsOverlayConfig,
            tracksPolicy: jsTracksPolicy,
            isFullScreen: self.isFullScreen,
            allowAirplay: self.allowAirplay,
            canMinimise: self.canMinimise,
            isPipEnabled: nil,
            canShareplay: nil, 
            isPlaybackQualityChangeAllowed: nil,
            isAutoPlayNextEnabled: false)
        
        if let rndvJSSource = rndvJSSource {
            let jsVideoData = RNDReactNativeDiceVideo.JSVideoData(source: rndvJSSource, config: rndvJSVideoDataConfig)
            rndvJsProps.videoData.value = jsVideoData
        }
        return rndvJsProps
    }
    

    private func setupDoris() {
        if let jsBridge = self.jsBridge {
            let jsProbs = self.convertRNVideoJSPropsToRNDV()
            var jsPlayerView = RNDReactNativeDiceVideo.JSPlayerView(overlayBuilder: JSOverlayBuilder(bridge: jsBridge), jsProps: jsProbs)
            self.addSubview(jsPlayerView)
            jsPlayerView.setRCTBubblingEventBlock(onVideoProgress: self.onVideoProgress,
                                                  onBackButton: self.onBackButton,
                                                  onVideoError: self.onVideoError,
                                                  onRequestPlayNextSource: nil,
                                                  onFullScreenButton: nil,
                                                  onVideoStart: self.onVideoLoadStart,
                                                  onVideoEnded: self.onVideoEnd,
                                                  onVideoPaused: nil,
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
        jsPlayerView?.dorisGlue?.setupLimitedSeekableRange(with: range)
//        jsDoris?.setupLimitedSeekableRange(with: range)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        if jsPlayerView == nil {
            setupDoris()
        }
    }
}

