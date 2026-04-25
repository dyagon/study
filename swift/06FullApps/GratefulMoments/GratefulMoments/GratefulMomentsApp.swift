//
//  GratefulMomentsApp.swift
//  GratefulMoments
//
//  Created by dyagon on 2026/3/28.
//

import SwiftUI
import SwiftData


@main
struct GratefulMomentsApp: App {
    let dataContainer = DataContainer()


    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(dataContainer)
        }
        .modelContainer(dataContainer.modelContainer)
    }
}
