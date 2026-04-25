//
//  ContentView.swift
//  DiceRoller
//
//  Created by dyagon on 2026/3/25.
//

import SwiftUI

struct ContentView: View {
    @State private var selectedSides: DiceSides = .d6
    @State private var dice: [Dice] = [Dice(sides: .d6)]

    var body: some View {
        VStack(spacing: 20) {
            Text("Dice Roller")
                .font(.largeTitle.lowercaseSmallCaps())

            Picker("Dice Type", selection: $selectedSides) {
                ForEach(DiceSides.allCases) { sides in
                    Text(sides.label).tag(sides)
                }
            }
            .pickerStyle(.segmented)

            HStack {
                ForEach(Array(dice.indices), id: \.self) { index in
                    DiceView(dice: dice[index]) {
                        withAnimation {
                            dice[index].roll()
                        }
                    }
                }
            }

            HStack {
                Button("Remove Dice", systemImage: "minus.circle.fill") {
                    withAnimation {
                        guard dice.count > 1 else { return }
                        dice.removeLast()
                    }
                }
                .disabled(dice.count == 1)

                Button("Add Dice", systemImage: "plus.circle.fill") {
                    withAnimation {
                        guard dice.count < 5 else { return }
                        dice.append(Dice(sides: selectedSides))
                    }
                }
                .disabled(dice.count == 5)
            }
            .labelStyle(.iconOnly)
            .font(.title)
            .symbolRenderingMode(.palette)
            .foregroundStyle(.white, .black.opacity(0.3))

            Button("Roll") {
                withAnimation {
                    for index in dice.indices {
                        dice[index].sides = selectedSides
                        dice[index].roll()
                    }
                }
            }
            .buttonStyle(.borderedProminent)
            .buttonBorderShape(.capsule)
        }
        .padding()
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(.appBackground)
        .tint(.white)
        .onChange(of: selectedSides) { _, newSides in
            withAnimation {
                for index in dice.indices {
                    dice[index].sides = newSides
                    dice[index].roll()
                }
            }
        }
    }
}

#Preview {
    ContentView()
}
