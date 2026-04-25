//
//  AppManager.swift
//  ImageGenerator
//
//  Created by Dong YANG on 2026/3/30.
//

import SwiftUI

@Observable
class AppManager {
    let generators: [GeneratorDescriptor] = GeneratorDescriptor.all
    var selectedGeneratorId: String = GeneratorDescriptor.all.first?.id ?? ""
    var imageGeneratorState: ImageGeneratorState
    var currentImage: NSImage?
    private(set) var error: Error?
    private(set) var isGenerating = false
    init() {
        let defaultGenerator = GeneratorDescriptor.all.first { $0.availability.isAvailable } ?? GeneratorDescriptor.all[0]
        self.selectedGeneratorId = defaultGenerator.id
        self.imageGeneratorState = ImageGeneratorState(generator: ImageGeneratorProvider.shared(for: defaultGenerator.id))
    }

    var selectedGenerator: GeneratorDescriptor? {
        generators.first { $0.id == selectedGeneratorId }
    }

    func switchGenerator(id: String) {
        guard let descriptor = generators.first(where: { $0.id == id }) else { return }
        selectedGeneratorId = id
        imageGeneratorState = ImageGeneratorState(generator: ImageGeneratorProvider.shared(for: descriptor.id))
        currentImage = nil
        error = nil
    }

    func generateImage() {
        error = nil
        isGenerating = true

        Task {
            do {
                print(
                    "[AppManager] generateImage() start generatorId=\(selectedGeneratorId) " +
                    "recipe=\(imageGeneratorState.recipe) style=\(String(describing: imageGeneratorState.style))"
                )
                let generatedImage = try await imageGeneratorState.generate()
                await MainActor.run {
                    currentImage = generatedImage
                    isGenerating = false
                }
            } catch {
                print("[AppManager] generateImage() failed: \(error) (localized: \(error.localizedDescription))")
                await MainActor.run {
                    self.error = error
                    isGenerating = false
                }
            }
        }
    }

    func reset() {
        imageGeneratorState.reset()
        currentImage = nil
        error = nil
        isGenerating = false
    }

    var showKitchen: Bool {
        currentImage != nil || isGenerating
    }
}

extension View {
    func previewEnvironment(generateImage: Bool = true) -> some View {
        let appManager = AppManager()
        return environment(appManager)
            .onAppear {
                if generateImage {
                    appManager.imageGeneratorState.style = .illustration
                    appManager.generateImage()
                }
            }
    }
}
