//
//  Dice.swift
//  DiceRoller
//

import Foundation

struct Dice: Identifiable {
    let id = UUID()
    var sides: DiceSides
    var value: Int

    init(sides: DiceSides = .d6) {
        self.sides = sides
        self.value = Int.random(in: 1...sides.rawValue)
    }

    mutating func roll() {
        value = Int.random(in: 1...sides.rawValue)
    }
}
