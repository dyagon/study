//
//  Quiz.swift
//  GenerableQuiz
//
//  Created by dyagon on 2026/3/31.
//

import Foundation

struct Answer: Identifiable, Equatable {
    let id = UUID()

    let text: String
    let isCorrect: Bool
    let explanation: String
}

extension Answer {
    static let correctAnswer = Answer(
        text: "This answer is correct.",
        isCorrect: true,
        explanation: "Explanation that this is right."
    )

    static let incorrectAnswer = Answer(
        text: "This answer is incorrect.",
        isCorrect: false,
        explanation: "Explanation that this is wrong."
    )
}

struct Question: Identifiable, Equatable {
    let id = UUID()

    let text: String
    let answers: [Answer]
}

extension Question {
    static let sampleAnswers = [
        Answer.correctAnswer,
        Answer.incorrectAnswer,
        Answer.incorrectAnswer,
        Answer.incorrectAnswer
    ]

    static let sample = Question(
        text: "Which answer is correct?",
        answers: sampleAnswers
    )
}

struct Quiz: Equatable {
    var questions: [Question]
}
