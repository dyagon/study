//
//  ContentView.swift
//  StateManageDemo
//
//  Created by Dong YANG on 2026/3/25.
//

import SwiftUI

struct ContentView: View {
    @State private var counterModel = CounterModel()
    @State private var showSettings = false

    var body: some View {
        VStack(spacing: 24) {
            Text("State Management")
                .font(.largeTitle.bold())

            HStack(spacing: 24) {
                StateDemoView()
                ObservableDemoView(counterModel: counterModel)
                BindingDemoView(counter: $counterModel.count)
            }

            HStack {
                Spacer()
                Button {
                    showSettings = true
                } label: {
                    Label("Settings", systemImage: "gear")
                }
                .buttonStyle(.bordered)
            }
            .padding(.horizontal)

            Spacer()
        }
        .padding()
        .frame(minWidth: 900, minHeight: 500)
        .sheet(isPresented: $showSettings) {
            SettingsView()
        }
    }
}

#Preview {
    ContentView()
}
