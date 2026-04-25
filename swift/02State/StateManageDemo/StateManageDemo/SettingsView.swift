//
//  SettingsView.swift
//  StateManageDemo
//
//  Created by Dong YANG on 2026/3/25.
//

import SwiftUI

struct SettingsView: View {
    @AppStorage("username") private var username = ""
    @AppStorage("notificationsEnabled") private var notificationsEnabled = true
    @AppStorage("theme") private var theme = "system"

    var body: some View {
        Form {
            Section("Account") {
                TextField("Username", text: $username)
            }

            Section("Preferences") {
                Toggle("Notifications", isOn: $notificationsEnabled)

                Picker("Theme", selection: $theme) {
                    Text("System").tag("system")
                    Text("Light").tag("light")
                    Text("Dark").tag("dark")
                }
            }

            Section {
                Text("@AppStorage persists data automatically")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .formStyle(.grouped)
        .frame(minWidth: 400, minHeight: 300)
    }
}

#Preview {
    SettingsView()
}
