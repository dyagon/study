//
//  ContentView.swift
//  WeatherForecast
//
//  Created by dyagon on 2026/3/27.
//

import SwiftUI

struct ContentView: View {
    private let weekForecast = ForecastDay.sampleWeek

    var body: some View {
        VStack(spacing: 16) {
            HStack {
                Text("Weekly Forecast")
                    .font(.largeTitle.weight(.bold))
                Spacer()
            }
            .frame(maxWidth: .infinity)
            .padding(.horizontal)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 14) {
                    ForEach(weekForecast) { forecast in
                        DayForecast(day: forecast.day, isRainy: forecast.isRainy, high: forecast.high, low: forecast.low)
                    }
                }
                .padding(.horizontal)
            }
            .frame(maxWidth: .infinity, minHeight: 190)

            WeekForecast(days: weekForecast)

            Spacer(minLength: 0)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .padding(.top, 18)
        .padding(.bottom, 12)
    }
}

#Preview {
    ContentView()
}
