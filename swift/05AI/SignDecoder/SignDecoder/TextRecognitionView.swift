//
//  TextRecognitionView.swift
//  SignDecoder
//
//  Created by dyagon on 2026/3/30.
//

import SwiftUI
import Vision

struct TextRecognitionView: View {
    let imageResource: ImageResource

    private let confidentBoxColor = Color(red: 1.00, green: 0.00, blue: 0.85)
    private let uncertainBoxColor = Color.orange

    @AppStorage(RecognitionSettingsKeys.level) private var levelRaw = RecognitionLevelPreference.accurate.rawValue
    @AppStorage(RecognitionSettingsKeys.minHeight) private var minHeight = 0.03

    @State private var lineItems: [LineRecognition] = []
    @State private var selectedCandidateIndex: [UUID: Int] = [:]
    @State private var identifiedText: String = ""
    @State private var isLoading = true

    private var taskSignature: String {
        "\(levelRaw)|\(minHeight)"
    }

    private var recognitionLevel: RecognizeTextRequest.RecognitionLevel {
        (RecognitionLevelPreference(rawValue: levelRaw) ?? .accurate).visionLevel
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Image(imageResource)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .clipShape(RoundedRectangle(cornerRadius: 8))
                .overlay {
                    if !lineItems.isEmpty {
                        ForEach(lineItems) { line in
                            BoundsRect(normalizedRect: line.normalizedRect)
                                .stroke(
                                    line.primaryConfidence < 1 ? uncertainBoxColor : confidentBoxColor,
                                    lineWidth: 3
                                )
                        }
                    }
                }
                .task(id: taskSignature) {
                    isLoading = true
                    lineItems = []
                    selectedCandidateIndex = [:]
                    identifiedText = ""

                    let minH = CGFloat(minHeight)
                    let result = await TextRecognitionRunner.recognize(
                        imageResource: imageResource,
                        recognitionLevel: recognitionLevel,
                        minimumNormalizedHeight: minH
                    )
                    lineItems = result.lines
                    selectedCandidateIndex = Dictionary(uniqueKeysWithValues: result.lines.map { ($0.id, 0) })
                    rebuildCombinedText()
                    isLoading = false
                }

            if !lineItems.isEmpty {
                DisclosureGroup("Alternate readings") {
                    ForEach(Array(lineItems.enumerated()), id: \.element.id) { index, line in
                        lineAlternateRow(index: index + 1, line: line)
                    }
                }
                .font(.subheadline)
            }

            Spacer(minLength: 8)

            TranslationView(text: $identifiedText, isProcessing: isLoading)
        }
        .padding()
        .trailTheme()
        .navigationTitle("Sign Info")
    }

    @ViewBuilder
    private func lineAlternateRow(index: Int, line: LineRecognition) -> some View {
        if line.candidates.count > 1 {
            Picker("Region \(index)", selection: candidateBinding(for: line.id, count: line.candidates.count)) {
                ForEach(Array(line.candidates.enumerated()), id: \.offset) { offset, cand in
                    Text(candidateLabel(cand)).tag(offset)
                }
            }
        } else if let only = line.candidates.first {
            LabeledContent("Region \(index)") {
                Text(only.string)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.trailing)
            }
        }
    }

    private func candidateBinding(for lineId: UUID, count: Int) -> Binding<Int> {
        Binding(
            get: { min(selectedCandidateIndex[lineId, default: 0], max(0, count - 1)) },
            set: { newValue in
                selectedCandidateIndex[lineId] = min(max(0, newValue), count - 1)
                rebuildCombinedText()
            }
        )
    }

    private func candidateLabel(_ cand: RecognizedText) -> String {
        let pct = Int((cand.confidence * 100).rounded())
        return "\(cand.string)  (\(pct)%)"
    }

    private func rebuildCombinedText() {
        identifiedText = lineItems.map { line in
            let i = min(selectedCandidateIndex[line.id, default: 0], max(0, line.candidates.count - 1))
            return line.candidates[i].string
        }.joined(separator: " ")
    }
}

#Preview {
    NavigationStack {
        TextRecognitionView(imageResource: .sign1)
            .navigationBarTitleDisplayMode(.inline)
    }
}
