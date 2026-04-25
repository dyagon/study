//
//  RecognitionSettingsView.swift
//  SignDecoder
//

import SwiftUI
import Vision

enum RecognitionSettingsKeys {
    static let level = "signDecoder.recognitionLevel"
    static let minHeight = "signDecoder.minNormalizedBoxHeight"
}

enum RecognitionLevelPreference: String, CaseIterable, Identifiable {
    case fast
    case accurate

    var id: String { rawValue }

    var visionLevel: RecognizeTextRequest.RecognitionLevel {
        switch self {
        case .fast: return .fast
        case .accurate: return .accurate
        }
    }

    var title: LocalizedStringKey {
        switch self {
        case .fast: return "Fast"
        case .accurate: return "Accurate"
        }
    }

    var footnote: String {
        switch self {
        case .fast:
            return "Prioritizes speed. Good for quick previews; text may differ from Accurate mode."
        case .accurate:
            return "Slower, but usually improves character accuracy. Compare results after switching."
        }
    }
}

struct RecognitionSettingsView: View {
    @Environment(\.dismiss) private var dismiss
    @AppStorage(RecognitionSettingsKeys.level) private var levelRaw = RecognitionLevelPreference.accurate.rawValue
    @AppStorage(RecognitionSettingsKeys.minHeight) private var minHeight = 0.03

    private var levelBinding: Binding<String> {
        Binding(
            get: { levelRaw },
            set: { levelRaw = $0 }
        )
    }

    var body: some View {
        Form {
            Section {
                Picker("Recognition level", selection: levelBinding) {
                    ForEach(RecognitionLevelPreference.allCases) { pref in
                        Text(pref.title).tag(pref.rawValue)
                    }
                }
                .pickerStyle(.segmented)

                if let pref = RecognitionLevelPreference(rawValue: levelRaw) {
                    Text(pref.footnote)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
            } header: {
                Text("Speed vs accuracy")
            }

            Section {
                Slider(value: $minHeight, in: 0.01...0.12, step: 0.005)
                Text(minHeight, format: .number.precision(.fractionLength(3)))
                    .monospacedDigit()
                    .foregroundStyle(.secondary)
            } header: {
                Text("Minimum box height")
            } footer: {
                Text("Regions shorter than this (normalized 0–1) are skipped so the app focuses on main sign text, not small print.")
            }
        }
        .navigationTitle("Recognition")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .confirmationAction) {
                Button("Done") { dismiss() }
            }
        }
    }
}

#Preview {
    NavigationStack {
        RecognitionSettingsView()
    }
}
