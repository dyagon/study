//
//  WeekForecast.swift
//  WeatherForecast
//
//  Created by dyagon on 2026/3/27.
//

import SwiftUI

struct WeekForecast: View {
    let days: [ForecastDay]

    private var dateRangeText: String {
        guard let first = days.first?.date, let last = days.last?.date else {
            return "No forecast data"
        }

        let formatter = DateFormatter()
        formatter.dateFormat = "MMM d"
        return "\(formatter.string(from: first)) - \(formatter.string(from: last))"
    }

    private var averageHigh: Int {
        guard !days.isEmpty else { return 0 }
        return days.map(\.high).reduce(0, +) / days.count
    }

    private var averageLow: Int {
        guard !days.isEmpty else { return 0 }
        return days.map(\.low).reduce(0, +) / days.count
    }

    private var sunnyDays: Int {
        days.filter { !$0.isRainy }.count
    }

    private var rainyDays: Int {
        days.filter(\.isRainy).count
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Week Summary")
                .font(.title3.weight(.semibold))
            Text(dateRangeText)
                .font(.subheadline)
                .foregroundStyle(.secondary)

            HStack {
                Text("Avg High")
                    .foregroundStyle(.secondary)
                Spacer()
                Text("\(averageHigh)°")
                    .fontWeight(.semibold)
            }

            HStack {
                Text("Avg Low")
                    .foregroundStyle(.secondary)
                Spacer()
                Text("\(averageLow)°")
                    .fontWeight(.semibold)
            }

            HStack {
                Text("Sunny / Rainy")
                    .foregroundStyle(.secondary)
                Spacer()
                Text("\(sunnyDays) / \(rainyDays) days")
                    .fontWeight(.medium)
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.blue.opacity(0.1), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .padding(.horizontal)
    }
}
