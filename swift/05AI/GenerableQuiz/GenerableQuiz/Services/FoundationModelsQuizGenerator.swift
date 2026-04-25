//
//  FoundationModelsQuizGenerator.swift
//  GenerableQuiz
//
//  Concrete FoundationModels implementation of QuizGeneratorProtocol.
//

// import Foundation
// import FoundationModels
//
// final class FoundationModelsQuizGenerator: QuizGenerator, @unchecked Sendable {
//     static let shared = FoundationModelsQuizGenerator()
//
//     static let info = QuizGeneratorInfo(
//         id: "foundationModels",
//         name: "Apple Foundation Models"
//     )
//
//     static let descriptor = QuizGeneratorDescriptor(
//         id: info.id,
//         name: info.name,
//         availability: checkAvailability()
//     )
//
//     static func checkAvailability() -> QuizGeneratorAvailability {
//         let availability = SystemLanguageModel.default.availability
//         switch availability {
//         case .available:
//             return .available
//         case .unavailable(let reason):
//             let message: String
//             switch reason {
//             case .appleIntelligenceNotEnabled:
//                 message = "Apple Intelligence is not enabled. Please enable it in Settings."
//             case .deviceNotEligible:
//                 message = "This device is not eligible for Apple Intelligence."
//             case .modelNotReady:
//                 message = "The language model is not ready."
//             @unknown default:
//                 message = "Apple Intelligence is unavailable."
//             }
//             return .unavailable(message)
//         }
//     }
//
//     func generateQuiz(topic: String) -> AsyncThrowingStream<Quiz.PartiallyGenerated, Error> {
//         AsyncThrowingStream { continuation in
//             let session = LanguageModelSession(
//                 instructions: "Create a quiz with the provided topic as the focus."
//             )
//             let stream = session.streamResponse(to: topic, generating: Quiz.self)
//
//             Task {
//                 do {
//                     for try await partial in stream {
//                         continuation.yield(partial.content)
//                     }
//                     continuation.finish()
//                 } catch {
//                     continuation.finish(throwing: QuizGeneratorError.streamingFailed(error))
//                 }
//             }
//         }
//     }
//
//     func regenerateQuestion(
//         topic: String,
//         existingQuestions: [String]
//     ) -> AsyncThrowingStream<Question.PartiallyGenerated, Error> {
//         AsyncThrowingStream { continuation in
//             let session = LanguageModelSession(
//                 instructions: "Create a question focused on \(topic)"
//             )
//             let existingQuestionsText = existingQuestions.joined(separator: ", ")
//             let prompt = "Avoid asking questions similar to these: \(existingQuestionsText)"
//             let stream = session.streamResponse(to: prompt, generating: Question.self)
//
//             Task {
//                 do {
//                     for try await partial in stream {
//                         continuation.yield(partial.content)
//                     }
//                     continuation.finish()
//                 } catch {
//                     continuation.finish(throwing: QuizGeneratorError.streamingFailed(error))
//                 }
//             }
//         }
//     }
// }
