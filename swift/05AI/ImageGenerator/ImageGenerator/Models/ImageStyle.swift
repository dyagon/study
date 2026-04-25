//
//  ImageStyle.swift
//  ImageGenerator
//
//  Created by Dong YANG on 2026/3/31.
//

import Foundation

enum ImageStyle: String, CaseIterable, Identifiable {
    case animation
    case illustration
    case sketch

    var id: String { rawValue }

    var displayName: String {
        rawValue.capitalized
    }

    var iconName: String {
        switch self {
        case .animation: return "film.fill"
        case .illustration: return "paintbrush.fill"
        case .sketch: return "pencil.tip"
        }
    }
}
