//
//  HexagonLayout.swift
//  GratefulMoments
//
//  Created by dyagon on 2026/3/29.
//

import SwiftUI



enum HexagonLayout: CaseIterable {
    /// Dense list: short titles, minimal copy.
    case compact
    /// Default card size.
    case standard
    /// Photo moments or longer text without dominating the path.
    case medium
    /// Newest / hero moment at the end of the timeline.
    case large


    var timestampBottomPadding: CGFloat {
        switch self {
        case .compact:
            return 0.06
        case .standard, .medium:
            return 0.08
        case .large:
            return 0.08
        }
    }


    var textBottomPadding: CGFloat {
        switch self {
        case .compact:
            return 0.20
        case .standard:
            return 0.25
        case .medium:
            return 0.26
        case .large:
            return 0.28
        }
    }


    var timestampHeight: CGFloat {
        size * (textBottomPadding - timestampBottomPadding)
    }

    var size: CGFloat {
        switch self {
        case .compact:
            return 148
        case .standard:
            return 200
        case .medium:
            return 275
        case .large:
            return 350
        }
    }


    var titleFont: Font {
        switch self {
        case .compact:
            return .subheadline.weight(.semibold)
        case .standard:
            return .headline
        case .medium:
            return .headline.weight(.semibold)
        case .large:
            return .title.bold()
        }
    }


    var bodyFont: Font {
        switch self {
        case .compact:
            return .caption2
        case .standard:
            return .caption2
        case .medium:
            return .caption
        case .large:
            return .body
        }
    }


    /// Max height for title + note when the hex shows a photo (material strip).
    var contentMaxHeightFractionImage: CGFloat {
        switch self {
        case .compact:
            return 0.12
        case .standard:
            return 0.15
        case .medium:
            return 0.14
        case .large:
            return 0.16
        }
    }


    /// Max height for text on solid-color hex (no photo).
    var contentMaxHeightFractionSolid: CGFloat {
        switch self {
        case .compact:
            return 0.42
        case .standard:
            return 0.50
        case .medium:
            return 0.52
        case .large:
            return 0.55
        }
    }


    var borderWidth: CGFloat {
        max(1.5, size * 0.0075)
    }


    /// Horizontal zig-zag offset scales with hex size so spacing stays proportional.
    var pathOffsetMultiplier: CGFloat {
        switch self {
        case .compact:
            return 0.38
        case .standard:
            return 0.35
        case .medium:
            return 0.34
        case .large:
            return 0.32
        }
    }
}
