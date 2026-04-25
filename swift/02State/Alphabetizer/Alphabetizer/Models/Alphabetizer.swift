//
//  Alphabetizer.swift
//  Alphabetizer
//
//  Created by dyagon on 2026/3/27.
//

import Foundation

@Observable
class Alphabetizer {
    private let tileCount = 3

    var difficulty: DifficultyMode = .standard {
        didSet {
            celebrationNotice = nil
        }
    }

    var tiles = [Tile]()
    var score = 0
    var message: Message = .instructions

    var totalAttempts = 0
    var winCount = 0

    var currentPack: VocabularyPack
    var masteredPackIds: Set<String> = []
    var consecutiveWinsOnPack = 0
    var celebrationNotice: String?

    init(currentPack: VocabularyPack = .landTrail) {
        self.currentPack = currentPack
        startNewGame()
    }

    var winPercentageDisplay: String {
        guard totalAttempts > 0 else { return "—" }
        let pct = (Double(winCount) / Double(totalAttempts)) * 100.0
        return String(format: "%.0f%%", pct)
    }

    var isCurrentPackMastered: Bool {
        masteredPackIds.contains(currentPack.rawValue)
    }

    var masteryProgressDescription: String {
        if VocabularyPack.allCases.allSatisfy({ masteredPackIds.contains($0.rawValue) }) {
            return "You have mastered every vocabulary set. Keep playing to stay sharp!"
        }
        if isCurrentPackMastered {
            return "Practicing \(currentPack.rawValue) — pick another set from the list anytime."
        }
        let n = VocabularyPack.winsToMasterSet
        return "Three consecutive wins on \(currentPack.rawValue) complete this set (\(consecutiveWinsOnPack)/\(n) so far)."
    }

    var packsMasteredSummary: String {
        let total = VocabularyPack.allCases.count
        let done = masteredPackIds.count
        return "Sets mastered: \(done)/\(total)"
    }

    /// Checks if tiles are in alphabetical order
    func submit() {
        guard message == .instructions else { return }

        totalAttempts += 1

        let userSortedTiles = tiles.sorted {
            $0.position.x < $1.position.x
        }
        let alphabeticallySortedTiles = tiles.sorted {
            $0.word.lexicographicallyPrecedes($1.word)
        }
        let isAlphabetized = userSortedTiles == alphabeticallySortedTiles

        if isAlphabetized {
            score += 1
            winCount += 1
            message = .youWin
            if !isCurrentPackMastered {
                consecutiveWinsOnPack += 1
                if consecutiveWinsOnPack >= VocabularyPack.winsToMasterSet {
                    completeMasteryOfCurrentPack()
                }
            }
        } else {
            if difficulty.penalizesWrongSubmit {
                score = max(0, score - 1)
            }
            if !isCurrentPackMastered {
                consecutiveWinsOnPack = 0
            }
            message = .tryAgain
        }

        for (tile, correctTile) in zip(userSortedTiles, alphabeticallySortedTiles) {
            let tileIsAlphabetized = tile == correctTile
            tile.flipped = tileIsAlphabetized
        }

        Task { @MainActor in
            try await Task.sleep(for: .seconds(2))

            startNewGame()

            for tile in tiles {
                tile.flipped = false
            }

            message = .instructions
            celebrationNotice = nil
        }
    }

    func selectVocabularyPack(_ pack: VocabularyPack) {
        guard pack != currentPack else { return }
        currentPack = pack
        consecutiveWinsOnPack = 0
        celebrationNotice = nil
        startNewGame()
    }

    // MARK: private implementation

    private func completeMasteryOfCurrentPack() {
        let completed = currentPack
        masteredPackIds.insert(completed.rawValue)
        consecutiveWinsOnPack = 0

        if let next = VocabularyPack.allCases.first(where: { !masteredPackIds.contains($0.rawValue) }) {
            celebrationNotice = "You mastered \(completed.rawValue)! Next: \(next.rawValue)."
            currentPack = next
        } else {
            celebrationNotice = "You mastered \(completed.rawValue)! Every vocabulary set is complete."
            currentPack = VocabularyPack.allCases.randomElement() ?? completed
        }
    }

    private func startNewGame() {
        let newWords = currentPack.vocabulary.selectRandomWords(count: tileCount)
        if tiles.isEmpty {
            for word in newWords {
                tiles.append(Tile(word: word))
            }
        } else {
            for (tile, word) in zip(tiles, newWords) {
                tile.word = word
            }
        }
    }
}
