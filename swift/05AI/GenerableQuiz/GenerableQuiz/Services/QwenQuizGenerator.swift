//
//  QwenQuizGenerator.swift
//  GenerableQuiz
//
//  Quiz generator using Alibaba Cloud Qwen (Bailian) model.
//

import Foundation

// MARK: - JSON Response Models

private struct QuizResponse: Codable {
    let questions: [QuestionResponse]
}

private struct QuestionResponse: Codable {
    let text: String
    let answers: [AnswerResponse]
}

private struct AnswerResponse: Codable {
    let text: String
    let isCorrect: Bool
    let explanation: String
}

private struct SingleQuestionResponse: Codable {
    let text: String
    let answers: [AnswerResponse]
}

// MARK: - Generator

final class QwenQuizGenerator: QuizGenerator, @unchecked Sendable {
    static let shared = QwenQuizGenerator()

    static let info = QuizGeneratorInfo(
        id: "qwen",
        name: "通义千问 (Qwen)"
    )

    static let descriptor = QuizGeneratorDescriptor(
        id: info.id,
        name: info.name,
        availability: checkAvailability()
    )

    private let apiKey: String?
    private let modelName: String
    private let baseURL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation"

    private let baseSystemPrompt = "You are a quiz generator."
    private let quizJSONFormat = """
    {
      "questions": [
        {
          "text": "Question text here",
          "answers": [
            {"text": "Answer A", "isCorrect": true, "explanation": "Explanation for correct answer"},
            {"text": "Answer B", "isCorrect": false, "explanation": "Explanation for wrong answer"},
            {"text": "Answer C", "isCorrect": false, "explanation": "Explanation for wrong answer"},
            {"text": "Answer D", "isCorrect": false, "explanation": "Explanation for wrong answer"}
          ]
        }
      ]
    }
    """
    private let singleQuestionJSONFormat = """
    {
      "text": "Question text here",
      "answers": [
        {"text": "Answer A", "isCorrect": true, "explanation": "Explanation for correct answer"},
        {"text": "Answer B", "isCorrect": false, "explanation": "Explanation for wrong answer"},
        {"text": "Answer C", "isCorrect": false, "explanation": "Explanation for wrong answer"},
        {"text": "Answer D", "isCorrect": false, "explanation": "Explanation for wrong answer"}
      ]
    }
    """

    init(apiKey: String? = ProcessInfo.processInfo.environment["DASHSCOPE_API_KEY"],
         modelName: String = "qwen-plus") {
        self.apiKey = apiKey
        self.modelName = modelName
    }

    static func checkAvailability() -> QuizGeneratorAvailability {
        guard let apiKey = ProcessInfo.processInfo.environment["DASHSCOPE_API_KEY"],
              !apiKey.isEmpty else {
            return .unavailable("DASHSCOPE_API_KEY environment variable is not set. Please set it to use Qwen.")
        }
        return .available
    }

    func generateQuiz(topic: String) -> AsyncThrowingStream<Quiz, Error> {
        AsyncThrowingStream { continuation in
            Task {
                do {
                    let quiz = try await generateQuizSync(topic: topic)
                    continuation.yield(quiz)
                    continuation.finish()
                } catch {
                    continuation.finish(throwing: error)
                }
            }
        }
    }

    func regenerateQuestion(topic: String, existingQuestions: [String]) -> AsyncThrowingStream<Question, Error> {
        AsyncThrowingStream { continuation in
            Task {
                do {
                    let question = try await regenerateQuestionSync(topic: topic, existingQuestions: existingQuestions)
                    continuation.yield(question)
                    continuation.finish()
                } catch {
                    continuation.finish(throwing: error)
                }
            }
        }
    }

    private func generateQuizSync(topic: String) async throws -> Quiz {
        let systemPrompt = baseSystemPrompt + """

        Create a multiple-choice quiz with exactly 4 questions about the given topic.
        Each question must have exactly 4 answers with only one correct answer.
        Provide a brief explanation for each answer.

        Respond with valid JSON using this format:
        \(quizJSONFormat)
        """

        let userPrompt = "Create a quiz about: \(topic)"
        let text = try await performRequest(systemPrompt: systemPrompt, userPrompt: userPrompt)

        return try parseQuizFromJSON(text)
    }

    private func regenerateQuestionSync(topic: String, existingQuestions: [String]) async throws -> Question {
        let existingText = existingQuestions.joined(separator: ", ")
        let systemPrompt = baseSystemPrompt + """

        Create ONE multiple-choice question about the given topic.
        The question must have exactly 4 answers with only one correct answer.
        Provide a brief explanation for each answer.
        IMPORTANT: Do not create a question similar to these existing questions: \(existingText)

        Respond with valid JSON using this format:
        \(singleQuestionJSONFormat)
        """

        let userPrompt = "Create a new question about: \(topic)"
        let text = try await performRequest(systemPrompt: systemPrompt, userPrompt: userPrompt)

        return try parseQuestionFromJSON(text)
    }

    private func performRequest(systemPrompt: String, userPrompt: String) async throws -> String {
        guard let apiKey = apiKey else {
            throw QuizGeneratorError.unavailable("API key not available")
        }

        let messages: [[String: Any]] = [
            ["role": "system", "content": systemPrompt],
            ["role": "user", "content": userPrompt]
        ]

        let requestBody: [String: Any] = [
            "model": modelName,
            "input": ["messages": messages]
        ]

        guard let url = URL(string: baseURL) else {
            throw QuizGeneratorError.generationFailed(NSError(domain: "QwenQuizGenerator", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid URL"]))
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        request.httpBody = try JSONSerialization.data(withJSONObject: requestBody)

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw QuizGeneratorError.generationFailed(NSError(domain: "QwenQuizGenerator", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid response"]))
        }

        guard httpResponse.statusCode == 200 else {
            let errorMessage = String(data: data, encoding: .utf8) ?? "Unknown error"
            throw QuizGeneratorError.generationFailed(NSError(domain: "QwenQuizGenerator", code: httpResponse.statusCode, userInfo: [NSLocalizedDescriptionKey: "API error: \(errorMessage)"]))
        }

        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
              let output = json["output"] as? [String: Any],
              let text = output["text"] as? String else {
            throw QuizGeneratorError.generationFailed(NSError(domain: "QwenQuizGenerator", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid response format"]))
        }

        return text
    }

    private func parseQuizFromJSON(_ text: String) throws -> Quiz {
        let response = try decodeJSON(QuizResponse.self, from: text)
        let questions = response.questions.map { q in
            Question(
                text: q.text,
                answers: q.answers.map { a in
                    Answer(text: a.text, isCorrect: a.isCorrect, explanation: a.explanation)
                }
            )
        }
        return Quiz(questions: questions)
    }

    private func parseQuestionFromJSON(_ text: String) throws -> Question {
        let response = try decodeJSON(SingleQuestionResponse.self, from: text)
        return Question(
            text: response.text,
            answers: response.answers.map { a in
                Answer(text: a.text, isCorrect: a.isCorrect, explanation: a.explanation)
            }
        )
    }

    private func decodeJSON<T: Decodable>(_ type: T.Type, from text: String) throws -> T {
        let cleanedText = extractJSON(from: text)

        guard let data = cleanedText.data(using: .utf8) else {
            throw QuizGeneratorError.generationFailed(NSError(domain: "QwenQuizGenerator", code: -1, userInfo: [NSLocalizedDescriptionKey: "Failed to convert text to data"]))
        }

        let decoder = JSONDecoder()
        return try decoder.decode(type, from: data)
    }

    private func extractJSON(from text: String) -> String {
        var result = text.trimmingCharacters(in: .whitespacesAndNewlines)

        // Strip markdown code fences
        if result.hasPrefix("```json") {
            result = String(result.dropFirst(7))
        } else if result.hasPrefix("```") {
            result = String(result.dropFirst(3))
        }

        if result.hasSuffix("```") {
            result = String(result.dropLast(3))
        }

        result = result.trimmingCharacters(in: .whitespacesAndNewlines)

        // Extract JSON object
        if let firstBrace = result.firstIndex(of: "{"),
           let lastBrace = result.lastIndex(of: "}") {
            result = String(result[firstBrace...lastBrace])
        }

        return result
    }
}
