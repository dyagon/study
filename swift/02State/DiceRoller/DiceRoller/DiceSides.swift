//
//  DiceSides.swift
//  DiceRoller
//

import Foundation

enum DiceSides: Int, CaseIterable, Identifiable {
    case d4 = 4
    case d6 = 6
    case d8 = 8
    case d12 = 12
    case d20 = 20

    var id: Int { rawValue }
    var label: String { "d\(rawValue)" }
}
