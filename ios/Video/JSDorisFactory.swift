//
//  JSDorisFactory.swift
//  react-native-video
//
//  Created by Yaroslav Lvov on 11.03.2022.
//

import AVDoris
import AVKit

class JSDorisFactory {
    static func build(jsProps: JSProps, containerView: UIView, jsInput: JSInputProtocol, bridge: RCTBridge?) -> JSDoris? {
        guard let theme = jsProps.theme.value else { return nil }
        
        let controller = JSDoris()
        let player = AVPlayer()
        let translationsMapper: AVDorisTranslationsMapper = AVDorisTranslationsMapper()
        
        //
        let translations = translationsMapper.map(translations: jsProps.translations.value)
        let style = DorisUIStyle(colors: .init(primary: theme.colors.primary,
                                               secondary: theme.colors.secondary),
                                 fonts: .init(primary: theme.fonts.primary,
                                              secondary: theme.fonts.secondary))
        
        let styleViewModel = UIStyleViewModel(style: style)
        let translationsViewModel = UITranslationsViewModel(translations: translations)
        let initialViewModel = UIInitialViewModel(styleViewModel: styleViewModel,
                                                  translationsViewModel: translationsViewModel)
        
        let view = DorisViewBuilder.build(player: player, viewModel: initialViewModel)
        view.backgroundColor = .black
        
        let doris = DorisFactory.create(player: player,
                                        view: view,
                                        output: controller)
        
        controller.doris = doris
        controller.output = jsInput
        controller.jsBridge = bridge
        
        containerView.addSubview(doris.viewController.view)
        
        doris.viewController.view.translatesAutoresizingMaskIntoConstraints = false
        
        let leading = doris.viewController.view.leadingAnchor.constraint(equalTo: containerView.leadingAnchor)
        let trailing = doris.viewController.view.trailingAnchor.constraint(equalTo: containerView.trailingAnchor)
        let top = doris.viewController.view.topAnchor.constraint(equalTo: containerView.topAnchor)
        let bottom = doris.viewController.view.bottomAnchor.constraint(equalTo: containerView.bottomAnchor)
        containerView.addConstraints([leading, trailing, top, bottom])
        
        return controller
    }
}
