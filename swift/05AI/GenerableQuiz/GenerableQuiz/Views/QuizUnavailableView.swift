//
//  QuizUnavailableView.swift
//  GenerableQuiz
//
//  View shown when the selected quiz generator is unavailable.
//

import SwiftUI

struct QuizUnavailableView: View {
    let message: String

    var body: some View {
        ContentUnavailableView(
            "Generator Unavailable",
            systemImage: "exclamationmark.triangle",
            description: Text(message)
        )
    }
}
