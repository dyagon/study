//
//  Sentiment.swift
//  HikingSurvey
//
//  Created by dyagon on 2026/3/29.
//

import SwiftUI
import Charts

/// Buckets NL sentiment scores (typically in [-1, 1]) into readable categories.
enum Sentiment: String, Plottable, CaseIterable, Comparable {
    case veryNegative = "Very negative"
    case negative = "Negative"
    case slightlyNegative = "Slightly negative"
    case neutral = "Neutral"
    case slightlyPositive = "Slightly positive"
    case positive = "Positive"
    case veryPositive = "Very positive"

    init(_ score: Double) {
        switch score {
        case ...(-0.5):
            self = .veryNegative
        case (-0.5)..<(-0.2):
            self = .negative
        case (-0.2)..<(-0.05):
            self = .slightlyNegative
        case (-0.05)...0.05:
            self = .neutral
        case (0.05)..<0.2:
            self = .slightlyPositive
        case 0.2..<0.5:
            self = .positive
        default:
            self = .veryPositive
        }
    }

    var icon: String {
        switch self {
        case .veryNegative:
            return "chevron.down.2"
        case .negative:
            return "chevron.down"
        case .slightlyNegative:
            return "arrow.down.right"
        case .neutral:
            return "minus"
        case .slightlyPositive:
            return "arrow.up.right"
        case .positive:
            return "chevron.up"
        case .veryPositive:
            return "chevron.up.2"
        }
    }

    var sentimentColor: Color {
        switch self {
        case .veryNegative:
            return Color(red: 0.45, green: 0.05, blue: 0.12)
        case .negative:
            return Color(red: 0.65, green: 0.12, blue: 0.18)
        case .slightlyNegative:
            return Color(red: 0.85, green: 0.35, blue: 0.25)
        case .neutral:
            return Color(red: 0.45, green: 0.45, blue: 0.48)
        case .slightlyPositive:
            return Color(red: 0.45, green: 0.65, blue: 0.35)
        case .positive:
            return Color(red: 0.20, green: 0.62, blue: 0.35)
        case .veryPositive:
            return Color(red: 0.05, green: 0.48, blue: 0.28)
        }
    }

    /// Lower rank = earlier in UI when ordering **positive → negative** (e.g. chart top / ring start).
    private var displaySequenceRank: Int {
        switch self {
        case .veryPositive: return 0
        case .positive: return 1
        case .slightlyPositive: return 2
        case .neutral: return 3
        case .slightlyNegative: return 4
        case .negative: return 5
        case .veryNegative: return 6
        }
    }

    static func < (lhs: Sentiment, rhs: Sentiment) -> Bool {
        lhs.displaySequenceRank < rhs.displaySequenceRank
    }
}
