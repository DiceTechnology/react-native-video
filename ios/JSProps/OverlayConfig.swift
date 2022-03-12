//
//  OverlayConfig.swift
//  react-native-video
//
//  Created by Yaroslav Lvov on 11.03.2022.
//

import AVDoris

struct OverlayConfig: SuperCodable {
    let type: OverlayConfigType
    let button: String
    let components: [Component]
    
    enum OverlayConfigType: String, Codable {
        case expanded, side, bottom, full
    }
    
    struct Component: Codable {
        let name: String
        let type: OverlayConfigType
        let initialProps: Dictionary<String, String>?
    }
    
    func create(bridge: RCTBridge?) -> OverlayType {
        guard let bridge = bridge else { return .none }
        
        switch type {
        case .expanded:
            guard
                let sideComponent = components.first(where: {$0.type == .side}),
                let bottomComponent = components.first(where: {$0.type == .bottom})
            else {
                return .none
            }
            
<<<<<<< HEAD
//            let sideJSComponentView = RCTRootView(bridge: bridge,
//                                                  moduleName: sideComponent.name,
//                                                  initialProperties: sideComponent.initialProps)
//
//            let bottomJSComponentView = RCTRootView(bridge: bridge,
//                                                    moduleName: bottomComponent.name,
//                                                    initialProperties: bottomComponent.initialProps)
            return .rightAndBottom(rightView: UIView(),
                                   bottomView: UIView())
=======
            let sideJSComponentView = RCTRootView(bridge: bridge,
                                                  moduleName: sideComponent.name,
                                                  initialProperties: sideComponent.initialProps)
            
            let bottomJSComponentView = RCTRootView(bridge: bridge,
                                                    moduleName: bottomComponent.name,
                                                    initialProperties: bottomComponent.initialProps)
            return .rightAndBottom(rightView: sideJSComponentView,
                                   bottomView: bottomJSComponentView)
>>>>>>> 2d45269ed40433f595241f280191cf9337436b0c
        case .side:
            guard
                let sideComponent = components.first(where: {$0.type == .side})
            else {
                return .none
            }
            
<<<<<<< HEAD
//            let sideJSComponentView = RCTRootView(bridge: bridge,
//                                                  moduleName: sideComponent.name,
//                                                  initialProperties: sideComponent.initialProps)
            return .right(rightView: UIView())
=======
            let sideJSComponentView = RCTRootView(bridge: bridge,
                                                  moduleName: sideComponent.name,
                                                  initialProperties: sideComponent.initialProps)
            return .right(rightView: sideJSComponentView)
>>>>>>> 2d45269ed40433f595241f280191cf9337436b0c
        default: return .none
        }
    }
}
