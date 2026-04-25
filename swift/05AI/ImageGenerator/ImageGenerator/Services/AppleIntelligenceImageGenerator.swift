//
//  AppleIntelligenceImageGenerator.swift
//  ImageGenerator
//
//  Created by Dong YANG on 2026/3/31.
//

import Foundation
import AppKit
import ImagePlayground

final class AppleIntelligenceImageGenerator: ImageGenerator {

    static let info = GeneratorInfo(id: "apple", name: "Apple Intelligence (Image Playground)")
    static let shared = AppleIntelligenceImageGenerator()

    static func checkAvailability() -> GeneratorAvailability {
        if #available(macOS 26.0, *) {
            return GeneratorAvailability(isAvailable: true, message: "macOS 26.0+ required")
        }
        return GeneratorAvailability(
            isAvailable: false,
            message: "需要 macOS 26.0 或更高版本"
        )
    }

    nonisolated func generate(prompt: String, style: ImageStyle) async throws -> NSImage {
        let playgroundStyle = toPlaygroundStyle(style)
        let concepts = [ImagePlaygroundConcept.text(prompt)]

        let imageCreator = try await ImageCreator()
        let images = imageCreator.images(for: concepts, style: playgroundStyle, limit: 1)

        for try await image in images {
            let cgImage = image.cgImage
            let nsSize = NSSize(width: CGFloat(cgImage.width), height: CGFloat(cgImage.height))
            return NSImage(cgImage: cgImage, size: nsSize)
        }

        throw ImageGeneratorError.generationFailed
    }

    private init() {}

    private nonisolated func toPlaygroundStyle(_ style: ImageStyle) -> ImagePlaygroundStyle {
        switch style {
        case .animation: return .animation
        case .illustration: return .illustration
        case .sketch: return .sketch
        }
    }
}
