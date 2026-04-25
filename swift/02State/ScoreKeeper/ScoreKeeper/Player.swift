//
//  Player.swift
//  ScoreKeeper
//
//  Created by Dong YANG on 2026/3/25.
//

import Foundation
import SwiftUI


enum PlayerColor: String, CaseIterable, Codable {
    case blue
    case green
    case orange
    case pink
    case purple
    case teal

    var uiColor: Color {
        switch self {
        case .blue: .blue
        case .green: .green
        case .orange: .orange
        case .pink: .pink
        case .purple: .purple
        case .teal: .teal
        }
    }
}

struct Player: Identifiable {
    let id: UUID
    var name: String
    var score: Int
    var color: PlayerColor

    init(id: UUID = UUID(), name: String, score: Int, color: PlayerColor = .blue) {
        self.id = id
        self.name = name
        self.score = score
        self.color = color
    }
}


extension Player: Equatable {
    static func == (lhs: Player, rhs: Player) -> Bool {
        lhs.name == rhs.name && lhs.score == rhs.score && lhs.color == rhs.color
     }
}


