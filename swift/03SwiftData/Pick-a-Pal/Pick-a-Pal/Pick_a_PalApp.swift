//
//  Pick_a_PalApp.swift
//  Pick-a-Pal
//
//  Created by dyagon on 2026/3/25.
//

import SwiftUI
import SwiftData

@main
struct Pick_a_PalApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .modelContainer(for: Pal.self)
    }
}
