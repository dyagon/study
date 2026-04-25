//
//  ContentView.swift
//  GratefulMoments
//
//  Created by dyagon on 2026/3/28.
//

import SwiftUI


struct ContentView: View {
    var body: some View {
        TabView {
            Tab("Moments", image: "MomentsTab") {
                MomentsView()
            }
            Tab("Achievements", systemImage: "medal.fill") {
                AchievementsView()
            }
        }
    }
}


#Preview {
    ContentView()
        .sampleDataContainer()
}


#Preview("Dark") {
    ContentView()
        .sampleDataContainer()
        .preferredColorScheme(.dark)
}
