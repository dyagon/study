//
//  GeneratorDescriptor.swift
//  ImageGenerator
//
//  Created by Dong YANG on 2026/3/31.
//

import Foundation

struct GeneratorDescriptor: Identifiable, Sendable {
    let id: String
    let name: String
    let availability: GeneratorAvailability
}

/// 按 `id` 返回懒加载单例（`static let shared` 首次访问时初始化，且全局各一处）。
enum ImageGeneratorProvider {
    static func shared(for id: String) -> ImageGenerator {
        switch id {
        case WanxiangImageGenerator.info.id:
            return WanxiangImageGenerator.shared
        case AppleIntelligenceImageGenerator.info.id:
            return AppleIntelligenceImageGenerator.shared
        default:
            return WanxiangImageGenerator.shared
        }
    }
}

extension GeneratorDescriptor {
    static let all: [GeneratorDescriptor] = [
        GeneratorDescriptor(
            id: WanxiangImageGenerator.info.id,
            name: WanxiangImageGenerator.info.name,
            availability: WanxiangImageGenerator.checkAvailability()
        ),
        GeneratorDescriptor(
            id: AppleIntelligenceImageGenerator.info.id,
            name: AppleIntelligenceImageGenerator.info.name,
            availability: AppleIntelligenceImageGenerator.checkAvailability()
        ),
    ]
}
