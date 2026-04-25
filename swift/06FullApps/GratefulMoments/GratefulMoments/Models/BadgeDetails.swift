//
//  BadgeDetails.swift
//  GratefulMoments
//
//  Created by dyagon on 2026/3/29.
//

import Foundation
import SwiftUI


enum BadgeDetails: Int, Codable, CaseIterable {
    case firstEntry = 0
    case fiveStars = 1
    case shutterbug = 2
    case expressive = 3
    case perfectTen = 4
    case twentyMoments = 5
    case thirtyDayStreak = 6


    /// Badges that must already be unlocked before Perfect 10 can award.
    static let perfectTenPrerequisites: Set<BadgeDetails> = [
        .firstEntry, .fiveStars, .shutterbug, .expressive,
    ]


    var requirements: String {
        switch self {
        case .firstEntry:
            return "Log a moment to start your journey."
        case .fiveStars:
            return "Record five moments."
        case .shutterbug:
            return "Add three entries with photos."
        case .expressive:
            return "Add five moments with a photo and text."
        case .perfectTen:
            return "Record at least 10 moments after earning Start the Journey, 5 Stars, Shutterbug, and Expressive."
        case .twentyMoments:
            return "Record 20 moments."
        case .thirtyDayStreak:
            return "Log a moment on 30 different days in a row (including today)."
        }
    }


    var title: String {
        switch self {
        case .firstEntry:
            return "Start the Journey"
        case .fiveStars:
            return "5 Stars"
        case .shutterbug:
            return "Shutterbug"
        case .expressive:
            return "Expressive"
        case .perfectTen:
            return "Perfect 10"
        case .twentyMoments:
            return "20 Moments"
        case .thirtyDayStreak:
            return "30-Day Flame"
        }
    }


    var image: ImageResource {
        switch self {
        case .firstEntry:
            return .firstEntryUnlocked
        case .fiveStars:
            return .fiveStarsUnlocked
        case .shutterbug:
            return .shutterbugUnlocked
        case .expressive:
            return .expressiveUnlocked
        case .perfectTen:
            return .perfectTenUnlocked
        case .twentyMoments:
            return .twentyUnlocked
        case .thirtyDayStreak:
            return .thirtyDayStreakUnlocked
        }
    }


    var lockedImage: ImageResource {
        switch self {
        case .firstEntry:
            return .firstEntryLocked
        case .fiveStars:
            return .fiveStarsLocked
        case .shutterbug:
            return .shutterbugLocked
        case .expressive:
            return .expressiveLocked
        case .perfectTen:
            return .perfectTenLocked
        case .twentyMoments:
            return .twentyLocked
        case .thirtyDayStreak:
            return .thirtyDayStreakLocked
        }
    }


    var color: Color {
        switch self {
        case .firstEntry:
            return .ember
        case .fiveStars:
            return .ruby
        case .shutterbug:
            return .sapphire
        case .expressive:
            return .ocean
        case .perfectTen:
            return .ember
        case .twentyMoments:
            return .ruby
        case .thirtyDayStreak:
            return .ocean
        }
    }


    var congratulatoryMessage: String {
        switch self {
        case .firstEntry:
            return "Every journey begins with a single step. Congratulations — you’re on your way!"
        case .fiveStars:
            return "You’re building momentum! The more you focus on regular practice, the better you get at choosing to keep up your intentioned habits."
        case .shutterbug:
            return "Photos connect us to our past, and looking at them can take us right back to the grateful feeling we had when we snapped them."
        case .expressive:
            return "Look at you, giving yourself all the ways to savor your happy memories!"
        case .perfectTen:
            return "You're getting the hang of your new habit! Keep it up and see how far it can take you."
        case .twentyMoments:
            return "Twenty grateful moments — that’s a real collection. Your future self will love looking back at these."
        case .thirtyDayStreak:
            return "A full month of showing up. Consistency like that is rare — celebrate it."
        }
    }
}
