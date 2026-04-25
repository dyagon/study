//
//  ContentView.swift
//  ScoreKeeper
//
//  Created by Dong YANG on 2026/3/25.
//

import SwiftUI




struct ContentView: View {
    @State private var scoreboard = Scoreboard()
    @State private var startingPoints = 0
    @State private var totalRounds = 3
    @State private var winningPointTotal = 20

    private var nextPlayerColor: PlayerColor {
        PlayerColor.allCases[scoreboard.players.count % PlayerColor.allCases.count]
    }

    var body: some View {
        VStack(alignment: .leading) {
            Text("Score Keeper")
                .font(.title)
                .bold()
                .padding(.bottom)
            SettingsView(
                doesHighestScoreWin: $scoreboard.doesHighestScoreWin,
                startingPoints: $startingPoints,
                totalRounds: $totalRounds,
                winningPointTotal: $winningPointTotal
            )

            if scoreboard.state == .playing {
                Text("Round \(scoreboard.currentRound) / \(scoreboard.totalRounds)")
                    .font(.headline)
                    .foregroundStyle(.secondary)
            }


            HStack {
                Text("Players")
                    .font(.headline)
                Spacer()
                EditButton()
                    .disabled(scoreboard.state != .setup)
            }

            List {
                ForEach(scoreboard.players.indices, id: \.self) { index in
                    HStack(spacing: 12) {
                        Circle()
                            .fill(scoreboard.players[index].color.uiColor)
                            .frame(width: 12, height: 12)

                        if scoreboard.winners.contains(scoreboard.players[index]) {
                            Image(systemName: "crown.fill")
                                .foregroundStyle(Color.yellow)
                        }

                        TextField("Name", text: $scoreboard.players[index].name)
                            .disabled(scoreboard.state != .setup)

                        Spacer()

                        Text("\(scoreboard.players[index].score)")
                            .opacity(scoreboard.state == .setup ? 0 : 1.0)

                        Stepper {
                            Text("\(scoreboard.players[index].score)")
                        } onIncrement: {
                            scoreboard.adjustScore(forPlayerAt: index, by: 1)
                        } onDecrement: {
                            scoreboard.adjustScore(forPlayerAt: index, by: -1)
                        }
                            .labelsHidden()
                            .opacity(scoreboard.state == .setup ? 0 : 1.0)
                    }
                }
                .onMove { indices, newOffset in
                    scoreboard.players.move(fromOffsets: indices, toOffset: newOffset)
                }
            }
            .padding(.vertical)


            Button("Add Player", systemImage: "plus") {
                scoreboard.players.append(Player(name: "", score: 0, color: nextPlayerColor))
            }
            .opacity(scoreboard.state == .setup ? 1.0 : 0)



            Spacer()


            
            HStack {
                Spacer()
                switch scoreboard.state {
                case .setup:
                    Button("Start Game", systemImage: "play.fill") {
                        scoreboard.startGame(
                            startingPoints: startingPoints,
                            totalRounds: totalRounds,
                            winningPointTotal: winningPointTotal
                        )
                    }
                case .playing:
                    Button(
                        scoreboard.currentRound >= scoreboard.totalRounds ? "End Game" : "Next Round",
                        systemImage: scoreboard.currentRound >= scoreboard.totalRounds ? "stop.fill" : "forward.fill"
                    ) {
                        scoreboard.advanceRoundOrEndGame()
                    }
                case .gameOver:
                    Button("Reset Game", systemImage: "arrow.counterclockwise") {
                        scoreboard.state = .setup
                    }
                }
                Spacer()
            }
            .buttonStyle(.bordered)
            .buttonBorderShape(.capsule)
            .controlSize(.large)
            .tint(.blue)
        }
        .padding()
    }
}

#Preview {
    ContentView()
}
