//
//  BadgeManager.swift
//  GratefulMoments
//
//  Created by dyagon on 2026/3/29.
//

import Foundation
import SwiftData


final class BadgeManager {
    private let modelContainer: ModelContainer


    init(modelContainer: ModelContainer) {
        self.modelContainer = modelContainer
    }


    func unlockBadges(newMoment: Moment) throws {
        let context = modelContainer.mainContext
        let moments = try context.fetch(FetchDescriptor<Moment>())
        let lockedBadges = try context.fetch(
            FetchDescriptor<Badge>(predicate: #Predicate { $0.timestamp == nil })
        )
        let momentsSorted = moments.sorted { $0.timestamp < $1.timestamp }
        let streak = StreakCalculator().calculateStreak(for: momentsSorted)

        var newlyUnlocked: [Badge] = []
        for badge in lockedBadges {
            if Self.shouldUnlock(
                badge.details,
                moments: moments,
                lockedBadges: lockedBadges,
                streak: streak
            ) {
                newlyUnlocked.append(badge)
            }
        }

        for badge in newlyUnlocked {
            badge.moment = newMoment
            badge.timestamp = newMoment.timestamp
        }
    }


    /// Pure unlock rules; used by `unlockBadges` and unit tests.
    static func shouldUnlock(
        _ details: BadgeDetails,
        moments: [Moment],
        lockedBadges: [Badge],
        streak: Int
    ) -> Bool {
        let lockedDetailSet = Set(lockedBadges.map(\.details))

        switch details {
        case .firstEntry:
            return moments.count >= 1
        case .fiveStars:
            return moments.count >= 5
        case .shutterbug:
            return moments.count(where: { $0.image != nil }) >= 3
        case .expressive:
            return moments.count(where: { $0.image != nil && !$0.note.isEmpty }) >= 5
        case .perfectTen:
            guard moments.count >= 10 else { return false }
            let prereqStillLocked = !BadgeDetails.perfectTenPrerequisites.isDisjoint(with: lockedDetailSet)
            return !prereqStillLocked && lockedDetailSet.contains(.perfectTen)
        case .twentyMoments:
            return moments.count >= 20
        case .thirtyDayStreak:
            return streak >= 30
        }
    }


    /// Inserts rows for any `BadgeDetails` case missing from the store (handles app upgrades).
    func loadBadgesIfNeeded() throws {
        let context = modelContainer.mainContext
        let existing = try context.fetch(FetchDescriptor<Badge>())
        let existingDetails = Set(existing.map(\.details))
        for details in BadgeDetails.allCases where !existingDetails.contains(details) {
            context.insert(Badge(details: details))
        }
    }
}
