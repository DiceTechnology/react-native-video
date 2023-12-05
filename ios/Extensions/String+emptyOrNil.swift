//
//  String+emptyOrNil.swift
//  react-native-video
//
//  Created by Yaroslav Lvov on 05.12.2023.
//

import Foundation

extension String? {
    var isEmptyOrNil: Bool {
        if let self = self {
            return self.isEmpty
        } else {
            return true
        }
    }
}
