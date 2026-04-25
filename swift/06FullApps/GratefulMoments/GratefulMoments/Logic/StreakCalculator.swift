//
//  StreakCalculator.swift
//  GratefulMoments
//
//  Created by dyagon on 2026/3/29.
//

import Foundation


struct StreakCalculator {
    let calendar = Calendar.current


    /// Counts the number of days in a row a moment has been saved
    ///
    /// Days are measured from the end of the day, rather than whatever time of day it is currently
    /// - precondition: `moments` must be sorted by timestamp, from earliest to latest
    func calculateStreak(for moments: [Moment]) -> Int {
        let startOfToday = calendar.startOfDay(for: .now)
        let endOfToday = calendar.date(byAdding: DateComponents(day: 1, second: -1), to: startOfToday)!


        // Ex. [0, 0, 1, 2, 4, 5]
        let daysAgoArray = moments
            .reversed()
            .map(\.timestamp)
            .map { calendar.dateComponents([.day], from: $0, to: endOfToday) }
            .compactMap { $0.day }
        
        var streak = 0
        for daysAgo in daysAgoArray {
            if daysAgo == streak {
                // Streak already here; don't increase the streak.
                // 5 posts in 1 day is a 1 streak. 5 posts in 2 days is a 2 streak.
                continue
            } else if daysAgo == streak + 1 {
                // A moment exists the day after the current streak, add to the streak
                streak += 1
            } else {
                // The streak breaks if jumping more than one day
                break
            }
        }

        
        // Streak is calculated above starting from yesterday. Not yet saving a moment today shouldn't break the streak.
        // If a moment has been saved today, include it in the streak.
        if daysAgoArray.first == 0 {
            streak += 1
        }
        return streak
    }
}
