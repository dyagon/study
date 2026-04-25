//
//  DiceRollerApp.swift
//  DiceRoller
//
//  Created by dyagon on 2026/3/25.
//

import SwiftUI

@main
struct DiceRollerApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .frame(minWidth: 400, idealWidth: 600, maxWidth: .infinity,
                    minHeight: 400, idealHeight: 600, maxHeight: .infinity)
        }
        .defaultSize(width: 400, height: 600)
        .windowResizability(.contentSize)
    }
}
