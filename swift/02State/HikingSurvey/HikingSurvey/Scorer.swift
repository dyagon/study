//
//  Scorer.swift
//  HikingSurvey
//
//  Created by dyagon on 2026/3/29.
//

import Foundation
import NaturalLanguage

struct AnalysisResult {
    var score: Double
    /// BCP-47 language code (e.g. "en", "zh-Hans"); `"und"` if unknown.
    var languageCode: String
}

class Scorer {
    private let tagger = NLTagger(tagSchemes: [.sentimentScore, .language])

    func analyze(_ text: String) -> AnalysisResult {
        let score = sentimentScore(for: text)
        tagger.string = text
        let languageCode = tagger.dominantLanguage?.rawValue ?? "und"
        return AnalysisResult(score: score, languageCode: languageCode)
    }

    func score(_ text: String) -> Double {
        sentimentScore(for: text)
    }

    private func sentimentScore(for text: String) -> Double {
        var sentimentScore = 0.0
        tagger.string = text
        tagger.enumerateTags(
            in: text.startIndex..<text.endIndex,
            unit: .paragraph,
            scheme: .sentimentScore,
            options: []
        ) { sentimentTag, _ in
            if let sentimentString = sentimentTag?.rawValue,
               let score = Double(sentimentString) {
                sentimentScore = score
                return true
            }
            return false
        }
        return sentimentScore
    }
}
