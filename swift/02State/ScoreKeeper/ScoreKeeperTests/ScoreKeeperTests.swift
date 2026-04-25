//
//  ScoreKeeperTests.swift
//  ScoreKeeperTests
//
//  Created by Dong YANG on 2026/3/25.
//

import Testing
@testable import ScoreKeeper

@MainActor
struct ScoreKeeperTests {

    
    @Test("Reset player scores", arguments: [0, 10, 20])
    func resetScores(to newValue: Int)  {
        var scoreboard = Scoreboard(players: [
            Player(name: "Elisha", score: 0),
            Player(name: "Andre", score: 5),
        ])
        scoreboard.resetScores(to: newValue)


        for player in scoreboard.players {
            #expect(player.score == newValue)
        }
    }
   
    
    @Test("Highest score wins")
    func highestScoreWins() {
        let scoreboard = Scoreboard(
            players: [
                Player(name: "Elisha", score: 0),
                Player(name: "Andre", score: 4),
            ],
            state: .gameOver,
            doesHighestScoreWin: true
        )
        let winners = scoreboard.winners
        #expect(winners == [Player(name: "Andre", score: 4)])
    }
    
    
    @Test("Lowest score wins")
    func lowestScoreWins() {
        let scoreboard = Scoreboard(
            players: [
                Player(name: "Elisha", score: 0),
                Player(name: "Andre", score: 4),
            ],
            state: .gameOver,
            doesHighestScoreWin: false
        )
        let winners = scoreboard.winners
        #expect(winners == [Player(name: "Elisha", score: 0)])
    }

    @Test("Tied highest scores both win")
    func tiedHighestScoresBothWin() {
        let scoreboard = Scoreboard(
            players: [
                Player(name: "Elisha", score: 7),
                Player(name: "Andre", score: 7),
                Player(name: "Jasmine", score: 3),
            ],
            state: .gameOver,
            doesHighestScoreWin: true
        )
        let winners = scoreboard.winners
        #expect(winners == [
            Player(name: "Elisha", score: 7),
            Player(name: "Andre", score: 7),
        ])
    }

    @Test("Game ends after configured rounds")
    func gameEndsAfterConfiguredRounds() {
        var scoreboard = Scoreboard()
        scoreboard.startGame(startingPoints: 0, totalRounds: 2, winningPointTotal: 50)

        #expect(scoreboard.state == .playing)
        #expect(scoreboard.currentRound == 1)

        scoreboard.advanceRoundOrEndGame()
        #expect(scoreboard.state == .playing)
        #expect(scoreboard.currentRound == 2)

        scoreboard.advanceRoundOrEndGame()
        #expect(scoreboard.state == .gameOver)
    }

    @Test("Game ends when player reaches winning points")
    func gameEndsWhenPlayerReachesWinningPoints() {
        var scoreboard = Scoreboard(players: [
            Player(name: "Elisha", score: 0),
            Player(name: "Andre", score: 0),
        ])
        scoreboard.startGame(startingPoints: 0, totalRounds: 10, winningPointTotal: 3)

        scoreboard.adjustScore(forPlayerAt: 0, by: 1)
        #expect(scoreboard.state == .playing)

        scoreboard.adjustScore(forPlayerAt: 0, by: 2)
        #expect(scoreboard.players[0].score == 3)
        #expect(scoreboard.state == .gameOver)
    }

}
