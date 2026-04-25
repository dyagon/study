//
//  ImageGenerator.swift
//  ImageGenerator
//
//  Created by Dong YANG on 2026/3/31.
//

import Foundation
import AppKit

protocol ImageGenerator: Sendable {
    static var info: GeneratorInfo { get }
    static func checkAvailability() -> GeneratorAvailability

    func generate(prompt: String, style: ImageStyle) async throws -> NSImage
}

struct GeneratorInfo: Sendable {
    let id: String
    let name: String
}

struct GeneratorAvailability: Sendable {
    let isAvailable: Bool
    let message: String?
}

enum ImageGeneratorError: Error, LocalizedError {
    case invalidStyle
    case generationFailed
    case networkError
    case invalidResponse
    case missingAPIKey
    case unavailable(String)

    var errorDescription: String? {
        switch self {
        case .invalidStyle: return "Invalid image style"
        case .generationFailed: return "Image generation failed"
        case .networkError: return "Network error"
        case .invalidResponse: return "Invalid response from server"
        case .missingAPIKey: return "Missing DASHSCOPE_API_KEY environment variable"
        case .unavailable(let msg): return msg
        }
    }
}
