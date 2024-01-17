//
//  RCTVideoManager.swift
//  RNDReactNativeDiceVideo
//
//  Created by Lukasz on 24/10/2019.
//  Copyright © 2019 Endeavor Streaming. All rights reserved.
//

import Foundation

#if SharedPlayer
typealias CurrentPlayerView = NewPlayerView
#else
typealias CurrentPlayerView = PlayerView
#endif

@objc(RCTVideoManager)
class RCTVideoManager: RCTViewManager {
    override func view() -> UIView! {
        let view =  CurrentPlayerView()
        view.jsBridge = bridge
        
        let weirdViewThatCausesFocusIssues = bridge.uiManager.view(forReactTag: NSNumber(integerLiteral: 25))
        weirdViewThatCausesFocusIssues?.isHidden = true
        
        return view
    }
    
    //MARK: Differs (ios only)
    @objc public func seekToNow(_ node: NSNumber) {
        DispatchQueue.main.async {
            let component = self.bridge.uiManager.view(forReactTag: node) as? CurrentPlayerView
            component?.seekToNow()
        }
    }
    
    //MARK: Differs (ios only)
    @objc public func seekToTimestamp(_ node: NSNumber, isoDate: String) {
        DispatchQueue.main.async {
            let component = self.bridge.uiManager.view(forReactTag: node) as? CurrentPlayerView
            component?.seekToTimestamp(isoDate: isoDate)
        }
    }
    
    @objc public func seekToPosition(_ node: NSNumber, position: Double) {
        DispatchQueue.main.async {
            let component = self.bridge.uiManager.view(forReactTag: node) as? CurrentPlayerView
            component?.seekToPosition(position: position)
        }
    }
    
    @objc public func replaceAdTagParameters(_ node: NSNumber, payload: NSDictionary) {
        DispatchQueue.main.async {
            let component = self.bridge.uiManager.view(forReactTag: node) as? CurrentPlayerView
            component?.replaceAdTagParameters(payload: payload)
        }
    }
    
    @objc public func seekToResumePosition(_ node: NSNumber, position: Double) {
        DispatchQueue.main.async {
            let component = self.bridge.uiManager.view(forReactTag: node) as? CurrentPlayerView
            component?.setInitialSeek(position: position)
        }
    }
    
    @objc public func limitSeekableRange(_ node: NSNumber, payload: NSDictionary) {
        DispatchQueue.main.async {
            let component = self.bridge.uiManager.view(forReactTag: node) as? CurrentPlayerView
            if let limitedSeekbleRange = try? Source.LimitedSeekableRange(dict: payload) {
                component?.setupLimitedSeekableRange(with: limitedSeekbleRange)
            }
        }
    }

    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
}
