//
//  ImageGeneratorState.swift
//  ImageGenerator
//
//  Created by Dong YANG on 2026/3/30.
//

import Foundation
import AppKit

@Observable
class ImageGeneratorState {
    var recipe = ImageGeneratorState.defaultRecipe
    var style: ImageStyle?
    var ingredients: [String] = []

    private let generator: ImageGenerator

    init(generator: ImageGenerator? = nil) {
        self.generator = generator ?? WanxiangImageGenerator.shared
    }

    func generate() async throws -> NSImage {
        guard let style else {
            throw ImageGeneratorError.invalidStyle
        }

        let prompt = buildPrompt()
        print(
            "[ImageGeneratorState] generate() generator=\(type(of: generator)) " +
            "prompt=\(prompt), style=\(style), ingredients=\(ingredients)"
        )
        return try await generator.generate(prompt: prompt, style: style)
    }

    private func buildPrompt() -> String {
        var parts = [recipe]
        parts.append(contentsOf: ingredients)
        return parts.joined(separator: ", ")
    }

    func reset() {
        recipe = ImageGeneratorState.defaultRecipe
        style = nil
        ingredients.removeAll()
    }
}

extension ImageGeneratorState {
    static let recipes = ["Salad", "Sandwich", "Ice Cream"]
    static let styles: [ImageStyle] = ImageStyle.allCases

    static let imageSize: CGFloat = 256
    private static let defaultRecipe = recipes[0]
}
