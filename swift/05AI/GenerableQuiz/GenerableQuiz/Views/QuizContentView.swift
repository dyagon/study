//
//  QuizContentView.swift
//  GenerableQuiz
//
//  Wrapper view for quiz content that handles generator availability.
//

import SwiftUI

struct QuizContentView: View {
    let topic: String

    var body: some View {
        let quizManager = QuizManager(topic: topic)
        let descriptor = quizManager.selectedGenerator

        if descriptor?.availability.isAvailable == true {
            QuizView()
                .environment(\.quizManager, quizManager)
                .toolbar {
                    ToolbarItem(placement: .principal) {
                        Text(descriptor?.name ?? "Quiz")
                            .font(.headline)
                    }
                }
        } else {
            QuizUnavailableView(
                message: descriptor?.availability.message ?? "Selected generator is unavailable."
            )
        }
    }
}
