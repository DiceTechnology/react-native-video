//
//  RNVideoManager.swift
//  RNDReactNativeDiceVideo
//
//  Created by Lukasz on 24/10/2019.
//  Copyright © 2019 Endeavor Streaming. All rights reserved.
//

import Foundation
import AVKit

@objc(RNVideoManager)
class RNVideoManager: RCTViewManager {
    override func view() -> UIView! {
        let controller = PlayerViewController()
        let view = PlayerView(controller: controller)
        controller.view = view
        return view
    }
    
    @objc public func seekToNow(_ node: NSNumber) {
        DispatchQueue.main.async {
            let component = self.bridge.uiManager.view(forReactTag: node) as? PlayerView
//            component?.seekToNow()
        }
    }
    
    @objc public func seekToTimestamp(_ node: NSNumber, isoDate: String) {
        DispatchQueue.main.async {
            let component = self.bridge.uiManager.view(forReactTag: node) as? PlayerView
//            component?.seekToTimestamp(isoDate: isoDate)
        }
    }
    
    @objc public func seekToPosition(_ node: NSNumber, position: Double) {
        DispatchQueue.main.async {
            let component = self.bridge.uiManager.view(forReactTag: node) as? PlayerView
//            component?.seek(toSeconds: position)
        }
    }
    
    @objc public func replaceAdTagParameters(_ node: NSNumber, payload: NSDictionary) {
        DispatchQueue.main.async {
            let component = self.bridge.uiManager.view(forReactTag: node) as? PlayerView
            
            var adTagParameters = [String: Any]()
            var startDate: Date?
            var endDate: Date?
            
            if let adTagParametersPayload = payload.value(forKey: "adTagParameters") as? [String: Any] {
                adTagParameters = adTagParametersPayload
            }
            
            if let startDateInterval = payload.value(forKey: "startDate") as? Double {
                startDate = Date(timeIntervalSince1970: startDateInterval)
            }
            
            if let endDateInterval = payload.value(forKey: "endDate") as? Double {
                endDate = Date(timeIntervalSince1970: endDateInterval)
            }
            
//            component?.prepareAdTagParameters(adTagParameters: adTagParameters) { newAdTagParameters in
//                component?.doris?.replaceAdTagParameters(adTagParameters: newAdTagParameters ?? adTagParameters,
//                                                        validFrom: startDate,
//                                                        validUntil: endDate)
//            }
        }
    }
    
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
}
