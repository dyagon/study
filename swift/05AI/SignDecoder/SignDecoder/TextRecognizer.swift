//
//  TextRecognizer.swift
//  SignDecoder
//
//  Created by dyagon on 2026/3/30.
//

import Foundation
import SwiftUI
import Vision

/// One text region after filtering, with Vision’s alternate strings (via `topCandidates`).
struct LineRecognition: Identifiable {
    let id: UUID
    let normalizedRect: NormalizedRect
    let candidates: [RecognizedText]

    init(observation: RecognizedTextObservation, candidates: [RecognizedText]) {
        id = observation.uuid
        normalizedRect = observation.boundingBox
        self.candidates = candidates
    }

    /// Confidence of the best candidate; used for box coloring (treat as “certainty” in UI copy).
    var primaryConfidence: Float {
        candidates.first?.confidence ?? 0
    }
}

struct RecognitionResult {
    let lines: [LineRecognition]
}

enum TextRecognitionRunner {

    static func recognize(
        imageResource: ImageResource,
        recognitionLevel: RecognizeTextRequest.RecognitionLevel,
        minimumNormalizedHeight: CGFloat,
        maxCandidateCount: Int = 5
    ) async -> RecognitionResult {
        var request = RecognizeTextRequest()
        request.recognitionLevel = recognitionLevel

        let image = UIImage(resource: imageResource)
        guard let imageData = image.pngData(),
              let observations = try? await request.perform(on: imageData) else {
            return RecognitionResult(lines: [])
        }

        let filtered = observations.filter { $0.boundingBox.height >= minimumNormalizedHeight }
        let sorted = filtered.sorted { readingOrderCompare($0, $1) }

        let lines: [LineRecognition] = sorted.compactMap { obs in
            let cands = obs.topCandidates(maxCandidateCount)
            guard !cands.isEmpty else { return nil }
            return LineRecognition(observation: obs, candidates: cands)
        }

        return RecognitionResult(lines: lines)
    }

    /// Top-to-then reading order in Vision’s normalized space (origin lower-left).
    private static func readingOrderCompare(_ o1: RecognizedTextObservation, _ o2: RecognizedTextObservation) -> Bool {
        let r1 = o1.boundingBox
        let r2 = o2.boundingBox
        let y1 = r1.origin.y + r1.height * 0.5
        let y2 = r2.origin.y + r2.height * 0.5
        let rowTolerance = CGFloat(0.04)
        if abs(y1 - y2) > rowTolerance {
            return y1 > y2
        }
        return r1.origin.x < r2.origin.x
    }
}
