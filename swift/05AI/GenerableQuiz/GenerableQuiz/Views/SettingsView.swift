//
//  SettingsView.swift
//  GenerableQuiz
//
//  Generator selection settings view.
//

import SwiftUI

struct SettingsView: View {
    @Environment(\.dismiss) private var dismiss
    @Bindable var quizManager: QuizManager

    var body: some View {
        NavigationStack {
            List {
                Section {
                    ForEach(quizManager.generators) { generator in
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(generator.name)
                                    .foregroundStyle(.primary)

                                if !generator.availability.isAvailable {
                                    Text(generator.availability.message ?? "Unavailable")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                            }

                            Spacer()

                            if generator.id == quizManager.selectedGeneratorId {
                                Image(systemName: "checkmark")
                                    .foregroundStyle(.blue)
                            }
                        }
                        .contentShape(Rectangle())
                        .onTapGesture {
                            if generator.availability.isAvailable {
                                quizManager.switchGenerator(to: generator.id)
                            }
                        }
                        .disabled(!generator.availability.isAvailable)
                    }
                } header: {
                    Text("Quiz Generator")
                } footer: {
                    Text("Select which AI service to use for generating quizzes.")
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
        }
    }
}

#Preview {
    SettingsView(quizManager: QuizManager(topic: "Marine Life"))
}
