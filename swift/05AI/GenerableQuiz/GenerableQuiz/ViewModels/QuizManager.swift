//
//  QuizManager.swift
//  GenerableQuiz
//
//  @Observable state manager for quiz generation.
//

import Foundation
import SwiftUI

private let kSelectedGeneratorIdKey = "selectedGeneratorId"

@Observable
final class QuizManager {
    let topic: String

    var quiz: Quiz?
    var isGenerating = false
    var error: Error?
    var selectedGeneratorId: String {
        didSet {
            UserDefaults.standard.set(selectedGeneratorId, forKey: kSelectedGeneratorIdKey)
        }
    }

    private var generationTask: Task<Void, Never>?

    init(topic: String, selectedGeneratorId: String? = nil) {
        self.topic = topic
        self.selectedGeneratorId = selectedGeneratorId
            ?? UserDefaults.standard.string(forKey: kSelectedGeneratorIdKey)
            ?? NaiveQuizGenerator.info.id
    }

    var generators: [QuizGeneratorDescriptor] {
        QuizGeneratorDescriptor.all
    }

    var selectedGeneratorImpl: any QuizGenerator {
        QuizGeneratorProvider.shared(for: selectedGeneratorId)
    }

    var selectedGenerator: QuizGeneratorDescriptor? {
        generators.first { $0.id == selectedGeneratorId }
    }

    func switchGenerator(to id: String) {
        guard generators.contains(where: { $0.id == id }) else { return }
        selectedGeneratorId = id
    }

    func generateQuiz() {
        cancelGeneration()

        generationTask = Task { @MainActor in
            isGenerating = true
            error = nil

            do {
                let stream = selectedGeneratorImpl.generateQuiz(topic: topic)
                for try await partial in stream {
                    self.quiz = partial
                }
            } catch {
                self.error = error
            }

            isGenerating = false
        }
    }

    func regenerate(question: Question) {
        guard !isGenerating,
              var currentQuiz = self.quiz,
              let index = currentQuiz.questions.firstIndex(where: { $0.id == question.id }) else {
            return
        }

        let existingQuestions = currentQuiz.questions.map { $0.text }

        generationTask = Task { @MainActor in
            isGenerating = true
            error = nil

            do {
                let stream = selectedGeneratorImpl.regenerateQuestion(
                    topic: topic,
                    existingQuestions: existingQuestions
                )
                for try await partial in stream {
                    currentQuiz.questions[index] = partial
                    self.quiz = currentQuiz
                }
            } catch {
                self.error = error
            }

            isGenerating = false
        }
    }

    func cancelGeneration() {
        generationTask?.cancel()
        generationTask = nil
    }
}

struct QuizManagerKey: EnvironmentKey {
    static let defaultValue: QuizManager? = nil
}

extension EnvironmentValues {
    var quizManager: QuizManager? {
        get { self[QuizManagerKey.self] }
        set { self[QuizManagerKey.self] = newValue }
    }
}
