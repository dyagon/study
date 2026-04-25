//
//  Scoreboard.swift
//  ScoreKeeper
//
//  Created by Dong YANG on 2026/3/25.
//

import Foundation

struct Scoreboard {
    var players: [Player] = [
        Player(name: "Elisha", score: 0, color: .blue),
        Player(name: "Andre", score: 0, color: .green),
        Player(name: "Jasmine", score: 0, color: .orange),
    ]
 
    var state = GameState.setup
    var doesHighestScoreWin = true
    var totalRounds = 3
    var currentRound = 1
    var winningPointTotal = 20


    
    var winners: [Player] {
        guard state == .gameOver else { return [] }


        var winningScore = 0
        if doesHighestScoreWin {
            winningScore = Int.min
            for player in players {
                winningScore = max(player.score, winningScore)
            }
        } else {
            winningScore = Int.max
            for player in players {
                winningScore = min(player.score, winningScore)
            }
        }


        return players.filter { player in
            player.score == winningScore
        }
    }
    
    mutating func resetScores(to newValue: Int) {
        for index in 0..<players.count {
            players[index].score = newValue
        }
    }

    mutating func startGame(startingPoints: Int, totalRounds: Int, winningPointTotal: Int) {
        state = .playing
        self.totalRounds = max(1, totalRounds)
        self.currentRound = 1
        self.winningPointTotal = max(1, winningPointTotal)
        resetScores(to: startingPoints)
    }

    mutating func advanceRoundOrEndGame() {
        guard state == .playing else { return }
        if currentRound >= totalRounds {
            state = .gameOver
        } else {
            currentRound += 1
        }
    }

    mutating func adjustScore(forPlayerAt index: Int, by delta: Int) {
        guard players.indices.contains(index) else { return }
        let newScore = players[index].score + delta
        players[index].score = min(max(newScore, 0), 20)

        if state == .playing && players[index].score >= winningPointTotal {
            state = .gameOver
        }
    }

}

