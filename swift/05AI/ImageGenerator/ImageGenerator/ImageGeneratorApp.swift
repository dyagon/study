//
//  ImageGeneratorApp.swift
//  ImageGenerator
//
//  Created by Dong YANG on 2026/3/30.
//

import SwiftUI


@main
struct ImageGeneratorApp: App {
    @State var appManager = AppManager()


    var body: some Scene {
        Window("ImageGenerator", id: "main") {
            ContentView()
                .environment(appManager)
        }
    }
}

