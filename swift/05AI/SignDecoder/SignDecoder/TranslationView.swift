//
//  TranslationView.swift
//  SignDecoder
//
//  Created by dyagon on 2026/3/30.
//

import SwiftUI
import Translation

struct TranslationView: View {
    @Binding var text: String
    var isProcessing: Bool
    @State private var showingTranslation = false

    var body: some View {
        VStack {
            Text("Identified Text")
                .font(.subheadline.bold())
                .textCase(.uppercase)
                .foregroundStyle(.gray)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.leading)

            TextField("Tap to edit recognized text", text: $text, axis: .vertical)
                .lineLimit(3...10)
                .textFieldStyle(.roundedBorder)
                .frame(maxWidth: .infinity, minHeight: 50, alignment: .topLeading)
                .padding()
                .background(Color(white: 0.9))
                .overlay {
                    if isProcessing {
                        ProgressView()
                    }
                }
                .disabled(isProcessing)
                .translationPresentation(isPresented: $showingTranslation, text: text)

            Button {
                showingTranslation = true
            } label: {
                Text("Translate")
                    .frame(height: 50)
                    .frame(maxWidth: .infinity)
                    .font(.title2.bold())
                    .foregroundColor(.white)
                    .background(RoundedRectangle(cornerRadius: 8))
            }
            .disabled(text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
            .padding(.top)
        }
    }
}

#Preview {
    struct PreviewWrapper: View {
        @State private var t = "Caution, falling rocks"
        var body: some View {
            TranslationView(text: $t, isProcessing: false)
        }
    }
    return PreviewWrapper()
}

#Preview("Processing") {
    struct PreviewWrapper: View {
        @State private var t = ""
        var body: some View {
            TranslationView(text: $t, isProcessing: true)
        }
    }
    return PreviewWrapper()
}
