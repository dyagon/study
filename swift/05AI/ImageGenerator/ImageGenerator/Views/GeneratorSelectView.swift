//
//  SelectModelView.swift
//  ImageGenerator
//
//  Created by Dong YANG on 2026/3/30.
//

import SwiftUI

struct GeneratorSelectView: View {
    @Environment(AppManager.self) private var appManager
    @Environment(\.supportsImagePlayground) private var supportsImagePlayground

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Generate With")
                .font(.headline)

            Text("Choose which image generation method to use.")
                .font(.subheadline)
                .foregroundStyle(.secondary)

            VStack(spacing: 10) {
                ForEach(appManager.generators) { generator in
                    GeneratorView(
                        generator: generator,
                        availability: resolvedAvailability(for: generator),
                        isSelected: appManager.selectedGeneratorId == generator.id
                    ) {
                        appManager.switchGenerator(id: generator.id)
                    }
                }
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(Color(nsColor: .controlBackgroundColor))
        )
    }

    private func resolvedAvailability(for generator: GeneratorDescriptor) -> GeneratorAvailability {
        guard generator.id == AppleIntelligenceImageGenerator.info.id else {
            return generator.availability
        }

        guard supportsImagePlayground else {
            return GeneratorAvailability(
                isAvailable: false,
                message: "当前设备、系统版本或地区不支持 Image Playground"
            )
        }

        return generator.availability
    }
}

#Preview {
    GeneratorSelectView()
        .environment(AppManager())
}
