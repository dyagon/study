//
//  ResponseView.swift
//  HikingSurvey
//
//  Created by dyagon on 2026/3/29.
//

import SwiftUI

struct ResponseView: View {
    var response: Response

    private var languageLabel: String {
        let code = response.languageCode
        let name = Locale.current.localizedString(forLanguageCode: code)
            ?? Locale.current.localizedString(forIdentifier: code)
        if let name, !name.isEmpty {
            return "\(name) · \(code)"
        }
        return code
    }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            VStack(alignment: .leading, spacing: 6) {
                Text(response.text)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Label(languageLabel, systemImage: "globe")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .accessibilityLabel("Detected language")
                    .accessibilityValue(languageLabel)
            }
            Image(systemName: response.sentiment.icon)
                .frame(width: 30, height: 30)
                .foregroundStyle(.white)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(response.sentiment.sentimentColor)
                )
                .accessibilityLabel("Sentiment")
                .accessibilityValue(response.sentiment.rawValue)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 8)
                .fill(.white)
        )
    }
}

#Preview {
    ResponseView(response: Response(text: "I enjoy hiking very much!", score: 1.0, languageCode: "en"))
}
