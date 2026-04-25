//
//  SettingsView.swift
//  ScoreKeeper
//
//  Created by Dong YANG on 2026/3/25.
//

import SwiftUI

struct SettingsView: View {
    @Binding var doesHighestScoreWin: Bool
    @Binding var startingPoints: Int
    @Binding var totalRounds: Int
    @Binding var winningPointTotal: Int


    var body: some View {
        VStack(alignment: .leading) {
            Text("Game Rules")
                .font(.headline)
            Divider()
            Picker("Win condition", selection: $doesHighestScoreWin) {
                Text("Highest Score Wins")
                    .tag(true)
                Text("Lowest Score Wins")
                    .tag(false)
            }
            Picker("Starting points", selection: $startingPoints) {
                Text("0 starting points")
                    .tag(0)
                Text("10 starting points")
                    .tag(10)
                Text("20 starting points")
                    .tag(20)
            }
            Stepper("Rounds: \(totalRounds)", value: $totalRounds, in: 1...20)
            Stepper("Winning points: \(winningPointTotal)", value: $winningPointTotal, in: 1...100)
        }
        .padding()
        .background(.thinMaterial, in: .rect(cornerRadius: 10.0))
    }
}


#Preview {
    @Previewable @State var doesHighestScoreWin = true
    @Previewable @State var startingPoints = 10
    @Previewable @State var totalRounds = 3
    @Previewable @State var winningPointTotal = 20
    SettingsView(
        doesHighestScoreWin: $doesHighestScoreWin,
        startingPoints: $startingPoints,
        totalRounds: $totalRounds,
        winningPointTotal: $winningPointTotal
    )
}
