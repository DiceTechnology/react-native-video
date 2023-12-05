//
//  Source.swift
//  RNDReactNativeDiceVideo
//
//  Created by Yaroslav Lvov on 05.03.2021.
//

import AVDoris

struct Source: SuperCodable {
    let id: String?
    let ima: Ima?
    let uri: URL
    let drm: Drm?
    let progressUpdateInterval: Int?
    let type: String
    let title: String?
    var live: Bool { config.muxData.videoIsLive }
    let partialVideoInformation: PartialVideoInformation?
    let isAudioOnly: Bool?
    let config: Config
    let titleInfo: TitleInfo?
    let imageUri: URL?
    let subtitles: [JSSubtitles]?
    let ads: JSAds?
    let thumbnailsPreview: URL?
    let limitedSeekableRange: LimitedSeekableRange?
    let nowPlaying: JSNowPlaying?
    let selectedSubtitleTrack: String?
    let preferredAudioTracks: [String]?
    var tracksPolicy: JSTracksPolicy?
}


extension Source {
    struct JSSubtitles: SuperCodable {
        let language: String
        let uri: URL
        var name: String {
            return NSLocale(localeIdentifier: NSLocale.current.identifier)
                .displayName(forKey: .identifier, value: language) ?? "Unknown"
        }
        var isVtt: Bool {
            uri.absoluteString.contains(".vtt")
        }
    }

    struct LimitedSeekableRange: SuperCodable {
        let start: Double?
        let end: Double?
        let seekToStart: Bool?
    }
    
    struct Ima: SuperCodable {
        let videoId: String?
        let adTagParameters: [String: String]?
        let endDate: Date?
        let startDate: Date?
        let assetKey: String?
        let contentSourceId: String?
        let authToken: String?
    }
    
    struct Drm: SuperCodable {
        let contentUrl: URL
        let drmScheme: String
        let croToken: String
        let licensingServerUrl: String
        let id: String
    }
    
    struct PartialVideoInformation: SuperCodable {
        let title: String
        let imageUri: URL
    }
    
    struct Config: SuperCodable {
        let muxData: MuxData
        let beacon: Beacon?
        let convivaData: JSConvivaData?
    }
    
    struct TitleInfo: SuperCodable {
        let external: Bool
        let title: String
        let description: String
    }
}


extension Source.Config {
    struct Beacon: SuperCodable {
        let authUrl: URL
        let url: URL
    }
    
    struct MuxData: SuperCodable {
        let envKey: String
        let videoTitle: String
        let viewerUserId: String
        let videoId: String
        let playerName: String
        let videoStreamType: String
        let subPropertyId: String
        let videoIsLive: Bool
        let experimentName: String?
    }
    
    struct JSConvivaData: SuperCodable {
        let url: String
        let customerKey: String?
        let title: String?
        let playerName: String?
        let playerVersion: String?
        let viewerId: String
        let isLive: Bool
        let debug: Bool
        let debugProxyUrl: String?
    }
}


extension DorisConvivaData {
    init?(data: Source.Config.JSConvivaData?) {
        guard let data = data, let customerKey = data.customerKey else { return nil }
        self.init(url: data.url,
                  customerKey: customerKey,
                  title: data.title.isEmptyOrNil ? "NA" : data.title ?? "NA",
                  playerName: data.playerName,
                  playerVersion: data.playerVersion,
                  viewerId: data.viewerId,
                  isLive: data.isLive,
                  debug: data.debug,
                  debugProxyUrl: data.debugProxyUrl ?? "")
    }
}
