//
//  DorisController.swift
//  react-native-video
//
//  Created by Yaroslav Lvov on 11.03.2022.
//

import AVDoris

class DorisController {
    private let muxDataMapper: AVDorisMuxDataMapper = AVDorisMuxDataMapper()
    private let sourceMapper: AVDorisSourceMapper = AVDorisSourceMapper()
    private var adTagParametersModifier = AdTagParametersModifier()
    
    private var currentPlayerState: DorisPlayerState = .initialization
    private var currentPlayingItemDuration: Double?
        
    weak var output: JSInputProtocol?
    var doris: Doris<TVOSAlphaPlayerView>?
    var jsBridge: RCTBridge?
    
    func setup(with props: JSProps) {
        props.source.bindAndFire { [weak self] source in
            guard let self = self else { return }
            guard let source = source else { return }
            
            self.setupMux(from: source)
            //FIXME: startAt
            self.setupPlayer(from: source, at: props.startAt.value)
        }
        
        props.relatedVideos.bindAndFire { [weak self] relatedVideos in
            guard let self = self else { return }
            guard let relatedVideos = relatedVideos else { return }
            self.doris?.viewController.viewModel.uiVisibilityViewModel.value.hasRelatedVideos = true
            
            let episodes = relatedVideos.items
                .dropFirst(relatedVideos.headIndex + 1)
                .prefix(3)
                .map { EpisodeViewModel(url: URL(string: $0.thumbnailUrl),
                                        title: $0.title,
                                        id: $0.id,
                                        type: StreamType(stringValue: $0.type)) }
            self.doris?.viewController.viewModel.moreEpisodesViewModel.value.episodes = episodes
        }
        
        props.buttons.bindAndFire { [weak self] buttons in
            guard let self = self else { return }
            guard let buttons = buttons else { return }
            
            self.doris?.viewController.viewModel.uiVisibilityViewModel.value.isFavouritable = buttons.favourite
            self.doris?.viewController.viewModel.uiVisibilityViewModel.value.hasSchedule = buttons.epg ?? false
            self.doris?.viewController.viewModel.uiVisibilityViewModel.value.hasStats = buttons.stats
        }
        
        props.metadata.bindAndFire { [weak self] metadata in
            guard let self = self else { return }
            self.doris?.viewController.viewModel.imagesViewModel.value.cropAlphaForLogo = true
            self.doris?.viewController.viewModel.imagesViewModel.value.logoURL = URL(string: metadata?.channelLogoUrl ?? "")
            self.doris?.viewController.viewModel.metadataViewModel.value.title = metadata?.title
            self.doris?.viewController.viewModel.metadataViewModel.value.description = metadata?.description
        }
        
        props.controls.bindAndFire { [weak self] isEnabled in
            guard let self = self else { return }
            self.doris?.viewController.toggleUI(isEnabled: isEnabled)
        }
        
        props.isFavourite.bindAndFire { [weak self] isFavourite in
            guard let self = self else { return }
            self.doris?.viewController.viewModel.buttonsViewModel.value.isFavourite = isFavourite
        }
        
        props.overlayConfig.bindAndFire { [weak self] config in
            guard let self = self else { return }
            guard let config = config else { return }
             
            self.doris?.viewController.viewModel.uiVisibilityViewModel.value.hasExternalOverlay = true
            self.doris?.viewController.viewModel.overlayViewModel.value.overlayType = config.create(bridge: self.jsBridge)
        }
    }
    
    func replaceAdTagParameters(parameters: AdTagParameters, extraInfo: AdTagParametersModifierInfo) {
        adTagParametersModifier.prepareAdTagParameters(adTagParameters: parameters.adTagParameters,
                                                       info: extraInfo) { [weak self] newAdTagParameters in
            guard let self = self else { return }
            guard let newAdTagParameters = newAdTagParameters else { return }
            self.doris?.player.replaceAdTagParameters(adTagParameters: newAdTagParameters, validFrom: parameters.startDate, validUntil: parameters.endDate)
        }
    }
    
    private func setupPlayer(from source: Source, at position: Double?) {
        sourceMapper.map(source: source, view: doris?.viewController.view) { [weak self] avDorisSource in
            guard let self = self else { return }
            
            switch avDorisSource {
            case .ima(let source):
                self.doris?.player.load(source: source, startAt: position)
            case .regular(let source):
                self.doris?.player.load(source: source, startAt: position)
            case .unknown:
                return
            }
        }
    }
    
    private func setupMux(from source: Source) {
        guard let avDorisMuxData = muxDataMapper.map(muxData: source.config.muxData) else { return }
        doris?.player.configureMux(playerData: avDorisMuxData.playerData,
                                   videoData: avDorisMuxData.videoData)
    }
}


extension DorisController: DorisOutputProtocol {
    func onPlayerEvent(_ event: DorisPlayerEvent) {
        switch event {
        
        case .stateChanged(state: let state):
            onPlayerStateChanged(state)
        case .finishedPlaying(endTime: _):
            output?.onVideoEnd?(nil)
        case .currentTimeChanged(time: let time):
            if time > 0 {
                output?.onVideoProgress?(["currentTime": time])
            }
            
            if let duration = currentPlayingItemDuration {
                let isAboutToEnd = time >= duration - 10
                output?.onVideoAboutToEnd?(["isAboutToEnd": isAboutToEnd]);
            }
        case .itemDurationChanged(duration: let duration):
            currentPlayingItemDuration = duration
        default: break
        }
    }
    
    func onAdvertisementEvent(_ event: AdvertisementEvent) {
        switch event {
        case .REQUIRE_AD_TAG_PARAMETERS(let data):
            output?.onRequireAdParameters?(["date": data.date.timeIntervalSince1970,
                                            "isBlocking": data.isBlocking])
        default: break
        }
    }
    
    func onViewEvent(_ event: DorisViewEvent) {
        switch event {
        case .favouritesButtonTap:
            output?.onFavouriteButton?(nil)
        case .statsButtonTap:
            output?.onStatsIconClick?(nil)
        case .scheduleButtonTap:
            output?.onEpgIconClick?(nil)
        case .relatedVideoSelected(id: let id, type: let type):
            output?.onRelatedVideoClicked?(["id": id, "type": type.stringValue])
        case .moreRelatedVideosTap:
            output?.onRelatedVideosIconClicked?(nil)
        case .backButtonTap:
            output?.onBackButton?(nil)
        default: break
        }
    }
    
    func onError(_ error: Error) {
        output?.onVideoError?(nil)
    }
    
    func onPlayerStateChanged(_ state: DorisPlayerState) {
        if currentPlayerState == .buffering {
            output?.onVideoBuffer?(["isBuffering": false])
        }
        
        currentPlayerState = state
        
        switch state {
        case .failed:
            output?.onVideoError?(nil)
        case .loaded:
            output?.onVideoLoad?(nil)
        case .loading,
             .buffering,
             .waitingForNetwork:
            output?.onVideoBuffer?(["isBuffering": true])
        case .paused,
             .stopped:
            output?.onPlaybackRateChange?(["playbackRate": 0.0])
        case .playing:
            output?.onPlaybackRateChange?(["playbackRate": 1.0])
        default: break
        }
    }
}
