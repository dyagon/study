//
//  BirthdaysApp.swift
//  Birthdays
//
//  Created by dyagon on 2026/3/26.
//

import SwiftUI
import SwiftData

@main
struct BirthdaysApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        // 注入模型容器，支持多个 Model 逗号分隔
        .modelContainer(for: Friend.self)

    }
}
