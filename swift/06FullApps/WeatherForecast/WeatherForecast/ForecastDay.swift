//
//  ForecastDay.swift
//  WeatherForecast
//
//  Created by dyagon on 2026/3/27.
//

import Foundation

struct ForecastDay: Identifiable {
    let id = UUID()
    let day: String
    let date: Date
    let isRainy: Bool
    let high: Int
    let low: Int
}

extension ForecastDay {
    static let sampleWeek: [ForecastDay] = [
        ForecastDay(day: "Mon", date: .now, isRainy: false, high: 70, low: 50),
        ForecastDay(day: "Tue", date: .now.addingTimeInterval(86_400), isRainy: true, high: 60, low: 40),
        ForecastDay(day: "Wed", date: .now.addingTimeInterval(172_800), isRainy: false, high: 84, low: 63),
        ForecastDay(day: "Thu", date: .now.addingTimeInterval(259_200), isRainy: false, high: 88, low: 67),
        ForecastDay(day: "Fri", date: .now.addingTimeInterval(345_600), isRainy: true, high: 74, low: 58),
        ForecastDay(day: "Sat", date: .now.addingTimeInterval(432_000), isRainy: false, high: 91, low: 72),
        ForecastDay(day: "Sun", date: .now.addingTimeInterval(518_400), isRainy: false, high: 79, low: 61)
    ]
}
