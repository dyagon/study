//
//  BadgeManagerTests.swift
//  GratefulMomentsTests
//

import SwiftData
import Testing
import UIKit
@testable import GratefulMoments


@MainActor
struct BadgeManagerTests {

    private static var tinyImageData: Data {
        let renderer = UIGraphicsImageRenderer(size: CGSize(width: 4, height: 4))
        let image = renderer.image { ctx in
            UIColor.systemBlue.setFill()
            ctx.fill(CGRect(x: 0, y: 0, width: 4, height: 4))
        }
        return image.pngData()!
    }


    private func makeManagerAndContext() throws -> (BadgeManager, ModelContext) {
        let schema = Schema([Moment.self, Badge.self])
        let configuration = ModelConfiguration(schema: schema, isStoredInMemoryOnly: true)
        let container = try ModelContainer(for: schema, configurations: [configuration])
        let manager = BadgeManager(modelContainer: container)
        try manager.loadBadgesIfNeeded()
        return (manager, container.mainContext)
    }


    private func badge(in context: ModelContext, _ details: BadgeDetails) throws -> Badge? {
        try context.fetch(FetchDescriptor<Badge>()).first { $0.details == details }
    }


    // MARK: - shouldUnlock (pure rules)


    @Test("firstEntry unlocks with at least one moment")
    func firstEntryRule() {
        let m = Moment(title: "A", note: "")
        let locked = BadgeDetails.allCases.map { Badge(details: $0) }
        #expect(BadgeManager.shouldUnlock(.firstEntry, moments: [m], lockedBadges: locked, streak: 0))
    }


    @Test("fiveStars unlocks with five moments")
    func fiveStarsRule() {
        let moments = (0..<5).map { Moment(title: "M\($0)", note: "") }
        let locked = BadgeDetails.allCases.map { Badge(details: $0) }
        #expect(BadgeManager.shouldUnlock(.fiveStars, moments: moments, lockedBadges: locked, streak: 0))
    }


    @Test("shutterbug unlocks with three photo moments")
    func shutterbugRule() {
        let data = Self.tinyImageData
        let moments = (0..<3).map { Moment(title: "P\($0)", note: "", imageData: data) }
        let locked = BadgeDetails.allCases.map { Badge(details: $0) }
        #expect(BadgeManager.shouldUnlock(.shutterbug, moments: moments, lockedBadges: locked, streak: 0))
    }


    @Test("expressive unlocks with five photo + note moments")
    func expressiveRule() {
        let data = Self.tinyImageData
        let moments = (0..<5).map {
            Moment(title: "E\($0)", note: "Note \($0)", imageData: data)
        }
        let locked = BadgeDetails.allCases.map { Badge(details: $0) }
        #expect(BadgeManager.shouldUnlock(.expressive, moments: moments, lockedBadges: locked, streak: 0))
    }


    @Test("perfectTen unlocks with 10 moments and prerequisite badges not in locked set")
    func perfectTenRule() {
        let data = Self.tinyImageData
        var moments: [Moment] = (0..<5).map {
            Moment(title: "X\($0)", note: "n", imageData: data)
        }
        moments += (0..<5).map { Moment(title: "Y\($0)", note: "") }
        let locked = [Badge(details: .perfectTen), Badge(details: .twentyMoments), Badge(details: .thirtyDayStreak)]
        #expect(BadgeManager.shouldUnlock(.perfectTen, moments: moments, lockedBadges: locked, streak: 0))
    }


    @Test("twentyMoments unlocks with 20 moments")
    func twentyMomentsRule() {
        let moments = (0..<20).map { Moment(title: "T\($0)", note: "") }
        let locked = BadgeDetails.allCases.map { Badge(details: $0) }
        #expect(BadgeManager.shouldUnlock(.twentyMoments, moments: moments, lockedBadges: locked, streak: 0))
    }


    @Test("thirtyDayStreak unlocks when streak is at least 30")
    func thirtyDayStreakRule() {
        let moments = [Moment(title: "Only", note: "")]
        let locked = BadgeDetails.allCases.map { Badge(details: $0) }
        #expect(BadgeManager.shouldUnlock(.thirtyDayStreak, moments: moments, lockedBadges: locked, streak: 30))
    }


    // MARK: - unlockBadges integration


    @Test("integration: first moment unlocks firstEntry in store")
    func integrationFirstEntry() throws {
        let (manager, ctx) = try makeManagerAndContext()
        let m = Moment(title: "Hello", note: "")
        ctx.insert(m)
        try manager.unlockBadges(newMoment: m)
        let b = try badge(in: ctx, .firstEntry)
        #expect(b?.timestamp != nil)
    }


    @Test("integration: twentyMoments unlocks on 20th insert")
    func integrationTwentyMoments() throws {
        let (manager, ctx) = try makeManagerAndContext()
        var last: Moment!
        for i in 0..<20 {
            let m = Moment(title: "R\(i)", note: "")
            ctx.insert(m)
            try manager.unlockBadges(newMoment: m)
            last = m
        }
        _ = last!
        let b = try badge(in: ctx, .twentyMoments)
        #expect(b?.timestamp != nil)
    }


    @Test("integration: thirty consecutive calendar days unlocks thirtyDayStreak")
    func integrationStreak30() throws {
        let (manager, ctx) = try makeManagerAndContext()
        let cal = Calendar.current
        let startOfToday = cal.startOfDay(for: .now)
        var last: Moment!
        for dayOffset in 0..<30 {
            let dayStart = cal.date(byAdding: .day, value: -dayOffset, to: startOfToday)!
            let ts = cal.date(byAdding: .hour, value: 12, to: dayStart)!
            let m = Moment(title: "Day \(dayOffset)", note: "", timestamp: ts)
            ctx.insert(m)
            try manager.unlockBadges(newMoment: m)
            last = m
        }
        _ = last!
        let b = try badge(in: ctx, .thirtyDayStreak)
        #expect(b?.timestamp != nil)
    }
}
