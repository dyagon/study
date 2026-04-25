//
//  DiceView.swift
//  DiceRoller
//
//  Created by dyagon on 2026/3/25.
//

import SwiftUI

struct DiceView: View {
    let dice: Dice
    let onTap: () -> Void

    private var d6SymbolName: String {
        "die.face.\(dice.value).fill"
    }

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .fill(.white)
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(.black.opacity(0.2), lineWidth: 2)
            if dice.sides == .d6 {
                Image(systemName: d6SymbolName)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 64, height: 64)
                    .foregroundStyle(.black, .white)
            } else {
                VStack(spacing: 4) {
                    Text("\(dice.value)")
                        .font(.system(size: 36, weight: .bold, design: .rounded))
                        .foregroundStyle(.black)
                    Text(dice.sides.label.uppercased())
                        .font(.caption.bold())
                        .foregroundStyle(.black.opacity(0.6))
                }
                .padding(8)
            }
        }
        .frame(width: 100, height: 100)
        .contentShape(Rectangle())
        .onTapGesture {
            onTap()
        }
    }
}

#Preview {
    DiceView(dice: Dice(sides: .d6)) { }
}
