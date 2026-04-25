//
//  WanxiangImageGenerator.swift
//  ImageGenerator
//
//  Created by Dong YANG on 2026/3/31.
//

import Foundation
import AppKit

final class WanxiangImageGenerator: ImageGenerator, @unchecked Sendable {

    static let info = GeneratorInfo(id: "wanxiang", name: "通义万象 (Wanxiang)")
    static let shared = WanxiangImageGenerator()

    static func checkAvailability() -> GeneratorAvailability {
        if let key = ProcessInfo.processInfo.environment["DASHSCOPE_API_KEY"], !key.isEmpty {
            return GeneratorAvailability(isAvailable: true, message: nil)
        }
        return GeneratorAvailability(
            isAvailable: false,
            message: "环境变量 DASHSCOPE_API_KEY 未设置"
        )
    }

    private let baseURL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation"
    private let session: URLSession

    private init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 120
        self.session = URLSession(configuration: config)
    }

    private var apiKey: String {
        get throws {
            guard let key = ProcessInfo.processInfo.environment["DASHSCOPE_API_KEY"] else {
                throw ImageGeneratorError.missingAPIKey
            }
            return key
        }
    }

    func generate(prompt: String, style: ImageStyle) async throws -> NSImage {
        let availability = Self.checkAvailability()
        guard availability.isAvailable else {
            throw ImageGeneratorError.unavailable(availability.message ?? "API key not configured")
        }

        let imageURL = try await requestImageURL(prompt: prompt, style: style)
        return try await downloadImage(from: imageURL)
    }

    private func requestImageURL(prompt: String, style: ImageStyle) async throws -> URL {
        guard let url = URL(string: baseURL) else {
            throw ImageGeneratorError.networkError
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Bearer \(try apiKey)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let fullPrompt = "\(prompt), \(style.promptSuffix)"

        let body = WanxiangRequest(
            model: "wan2.6-t2i",
            input: WanxiangInput(
                messages: [
                    WanxiangMessage(
                        role: "user",
                        content: [WanxiangContent(text: fullPrompt)]
                    )
                ]
            ),
            parameters: WanxiangParameters(
                promptExtend: true,
                watermark: false,
                n: 1,
                negativePrompt: "",
                size: "1280*1280"
            )
        )

        request.httpBody = try JSONEncoder().encode(body)

        let (data, response) = try await session.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            print("[WanxiangImageGenerator] requestImageURL: response is not HTTPURLResponse")
            throw ImageGeneratorError.networkError
        }

        let bodyPreview = Self.logBodyPreview(data)

        guard httpResponse.statusCode == 200 else {
            print("[WanxiangImageGenerator] HTTP \(httpResponse.statusCode) body: \(bodyPreview)")
            throw ImageGeneratorError.generationFailed
        }

        print("[WanxiangImageGenerator] HTTP 200, body bytes=\(data.count), preview: \(bodyPreview)")

        let result: WanxiangResponse
        do {
            result = try JSONDecoder().decode(WanxiangResponse.self, from: data)
        } catch {
            print("[WanxiangImageGenerator] JSON decode failed: \(error). Raw body: \(bodyPreview)")
            throw error
        }

        if let code = result.code, !code.isEmpty {
            print(
                "[WanxiangImageGenerator] API business error request_id=\(result.requestId ?? "nil") " +
                "code=\(code) message=\(result.message ?? "")"
            )
            throw ImageGeneratorError.generationFailed
        }

        let imageURLString = Self.extractImageURLString(from: result)
        let resultsCount = result.output?.results?.count ?? 0
        let choicesCount = result.output?.choices?.count ?? 0
        let contentSummary = result.output?.choices?.first?.message?.content?
            .map { "\($0.type ?? "?")" }
            .joined(separator: ",") ?? "nil"
        print(
            "[WanxiangImageGenerator] decoded request_id=\(result.requestId ?? "nil"), " +
            "output.results.count=\(resultsCount), output.choices.count=\(choicesCount), " +
            "first choice content types=[\(contentSummary)], extractedURL=\(imageURLString.map { "\"\($0.prefix(80))…\"" } ?? "nil")"
        )

        guard let imageURLString,
              let imageURL = URL(string: imageURLString),
              !imageURLString.isEmpty else {
            print(
                "[WanxiangImageGenerator] invalidResponse: no image URL in output.results[].image_url " +
                "nor output.choices[].message.content (type=image). See body preview above."
            )
            throw ImageGeneratorError.invalidResponse
        }

        print("[WanxiangImageGenerator] image download URL: \(imageURL.absoluteString)")
        return imageURL
    }

    private func downloadImage(from url: URL) async throws -> NSImage {
        let (data, response) = try await session.data(from: url)

        guard let httpResponse = response as? HTTPURLResponse else {
            print("[WanxiangImageGenerator] downloadImage: not HTTP response for \(url.absoluteString)")
            throw ImageGeneratorError.networkError
        }

        let mime = httpResponse.value(forHTTPHeaderField: "Content-Type") ?? "?"
        print(
            "[WanxiangImageGenerator] downloadImage HTTP \(httpResponse.statusCode) " +
            "Content-Type=\(mime) bytes=\(data.count) url=\(url.absoluteString)"
        )

        guard httpResponse.statusCode == 200 else {
            print("[WanxiangImageGenerator] downloadImage body preview: \(Self.logBodyPreview(data))")
            throw ImageGeneratorError.networkError
        }

        guard let image = NSImage(data: data) else {
            print("[WanxiangImageGenerator] invalidResponse: NSImage(data:) failed (not image bytes?). preview: \(Self.logBodyPreview(data))")
            throw ImageGeneratorError.invalidResponse
        }

        return image
    }

    /// 控制台日志用，避免一次打印超大 body。
    private static func logBodyPreview(_ data: Data, maxChars: Int = 4000) -> String {
        let s = String(data: data, encoding: .utf8) ?? "<non-utf8 \(data.count) bytes>"
        if s.count <= maxChars { return s }
        let end = s.index(s.startIndex, offsetBy: maxChars)
        return String(s[..<end]) + "…(truncated, total \(s.count) chars)"
    }

    /// Wan 2.6 同步接口文档：`output.choices[].message.content[]` 中 `type == "image"` 时 URL 在 `image`（部分实现也在 `text`）。
    /// 旧版/其他接口可能仍返回 `output.results[].image_url`。
    private static func extractImageURLString(from result: WanxiangResponse) -> String? {
        if let s = result.output?.results?.first?.imageUrl?.trimmingCharacters(in: .whitespacesAndNewlines),
           !s.isEmpty,
           URL(string: s) != nil {
            return s
        }
        for choice in result.output?.choices ?? [] {
            for item in choice.message?.content ?? [] where item.type == "image" {
                let s = (item.image ?? item.text)?
                    .trimmingCharacters(in: .whitespacesAndNewlines)
                if let s, !s.isEmpty, URL(string: s) != nil {
                    return s
                }
            }
        }
        return nil
    }
}

// MARK: - Wanxiang Request/Response Models

private struct WanxiangRequest: Encodable {
    let model: String
    let input: WanxiangInput
    let parameters: WanxiangParameters
}

private struct WanxiangInput: Encodable {
    let messages: [WanxiangMessage]
}

private struct WanxiangMessage: Encodable {
    let role: String
    let content: [WanxiangContent]
}

private struct WanxiangContent: Encodable {
    let text: String
}

private struct WanxiangParameters: Encodable {
    let promptExtend: Bool
    let watermark: Bool
    let n: Int
    let negativePrompt: String
    let size: String

    enum CodingKeys: String, CodingKey {
        case promptExtend = "prompt_extend"
        case watermark
        case n
        case negativePrompt = "negative_prompt"
        case size
    }
}

private struct WanxiangResponse: Decodable {
    let output: WanxiangOutput?
    let requestId: String?
    let code: String?
    let message: String?

    enum CodingKeys: String, CodingKey {
        case output
        case requestId = "request_id"
        case code
        case message
    }
}

private struct WanxiangOutput: Decodable {
    let results: [WanxiangResult]?
    let choices: [WanxiangOutputChoice]?
}

private struct WanxiangOutputChoice: Decodable {
    let message: WanxiangAssistantMessage?
}

private struct WanxiangAssistantMessage: Decodable {
    let content: [WanxiangOutputContentItem]?
}

private struct WanxiangOutputContentItem: Decodable {
    let type: String?
    let image: String?
    let text: String?
}

private struct WanxiangResult: Decodable {
    let imageUrl: String?

    enum CodingKeys: String, CodingKey {
        case imageUrl = "image_url"
    }
}

extension ImageStyle {
    fileprivate var promptSuffix: String {
        switch self {
        case .animation: return "animation style, vibrant colors"
        case .illustration: return "illustration style, detailed artwork"
        case .sketch: return "sketch style, hand-drawn"
        }
    }
}
