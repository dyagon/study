//
//  QuizGenerator.swift
//  GenerableQuiz
//
//  Protocol + supporting types for quiz generation backends.
//

import Foundation

// MARK: - Info

struct QuizGeneratorInfo: Equatable {
    let id: String
    let name: String
}

// MARK: - Availability

struct QuizGeneratorAvailability: Equatable {
    let isAvailable: Bool
    let message: String?

    static let available = QuizGeneratorAvailability(isAvailable: true, message: nil)

    static func unavailable(_ message: String) -> QuizGeneratorAvailability {
        QuizGeneratorAvailability(isAvailable: false, message: message)
    }
}

// MARK: - Descriptor

struct QuizGeneratorDescriptor: Identifiable, Equatable {
    let id: String
    let name: String
    let availability: QuizGeneratorAvailability

    static let all: [QuizGeneratorDescriptor] = [
        NaiveQuizGenerator.descriptor,
        QwenQuizGenerator.descriptor
    ]
}

// MARK: - Error

enum QuizGeneratorError: LocalizedError {
    case unavailable(String)
    case generationFailed(Error)
    case streamingFailed(Error)
    case topicRequired

    var errorDescription: String? {
        switch self {
        case .unavailable(let message):
            return message
        case .generationFailed(let error):
            return "Generation failed: \(error.localizedDescription)"
        case .streamingFailed(let error):
            return "Streaming failed: \(error.localizedDescription)"
        case .topicRequired:
            return "A topic is required to generate a quiz."
        }
    }
}

// MARK: - Protocol

protocol QuizGenerator: Sendable {
    static var info: QuizGeneratorInfo { get }
    static func checkAvailability() -> QuizGeneratorAvailability

    func generateQuiz(topic: String) -> AsyncThrowingStream<Quiz, Error>
    func regenerateQuestion(topic: String, existingQuestions: [String]) -> AsyncThrowingStream<Question, Error>
}

// MARK: - Provider

enum QuizGeneratorProvider {
    static let defaultGeneratorID = NaiveQuizGenerator.info.id

    static func shared(for id: String) -> any QuizGenerator {
        switch id {
        case NaiveQuizGenerator.info.id:
            return NaiveQuizGenerator.shared
        case QwenQuizGenerator.info.id:
            return QwenQuizGenerator.shared
        default:
            return NaiveQuizGenerator.shared
        }
    }
}
