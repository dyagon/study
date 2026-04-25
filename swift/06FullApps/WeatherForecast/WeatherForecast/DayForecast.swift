//
//  DayForecast.swift
//  WeatherForecast
//
//  Created by dyagon on 2026/3/27.
//

import SwiftUI

struct DayForecast: View {
    let day: String
    let isRainy: Bool
    let high: Int
    let low: Int

    var iconName: String {
        if isRainy {
            return "cloud.rain.fill"
        } else {
            return "sun.max.fill"
        }
    }

    var iconColor: Color {
        if isRainy {
            return .blue
        } else {
            return .yellow
        }
    }

    var highTemperatureColor: Color {
        if high > 80 {
            return .red
        } else {
            return .primary
        }
    }

    var body: some View {
        VStack(alignment: .center, spacing: 8) {
            Text(day)
                .font(.headline)
            Spacer(minLength: 0)
            Image(systemName: iconName)
                .foregroundStyle(iconColor)
                .font(.system(size: 36))
                .padding(5)
            Spacer(minLength: 0)
            Text("High: \(high)")
                .fontWeight(.semibold)
                .foregroundStyle(highTemperatureColor)
            Text("Low: \(low)")
                .fontWeight(.medium)
                .foregroundStyle(.secondary)
        }
        .frame(width: 128, height: 172, alignment: .center)
        .padding(.vertical, 14)
        .padding(.horizontal, 10)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 14, style: .continuous))
    }
}
