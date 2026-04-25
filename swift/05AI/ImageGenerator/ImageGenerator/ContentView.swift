//
//  ContentView.swift
//  ImageGenerator
//
//  Created by Dong YANG on 2026/3/30.
//

import SwiftUI

struct ContentView: View {
    @Environment(AppManager.self) private var appManager

    var body: some View {
        NavigationSplitView {
            GeneratorSelectView()
                .frame(minWidth: 220, idealWidth: 260)
        } detail: {
            VStack(spacing: 0) {
                // Top: Style Options
                StyleOptionsView()

                // Middle: Image Display
                ImageDisplayView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
            .overlay(alignment: .bottom) { generateButtonBar }
            .overlay { loadingOverlay }
        }
    }

    private var generateButtonBar: some View {
        HStack {
            Spacer()

            generateButton

            Spacer()
        }
        .padding(.vertical, 16)
        .background(.ultraThinMaterial)
    }

    private var generateButton: some View {
        Button {
            appManager.generateImage()
        } label: {
            ZStack {
                Circle()
                    .fill(
                        LinearGradient(
                            colors: [.blue, .purple],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .shadow(color: .blue.opacity(0.4), radius: 8, x: 0, y: 4)

                if appManager.isGenerating {
                    ProgressView()
                        .progressViewStyle(.circular)
                        .tint(.white)
                } else {
                    Image(systemName: "wand.and.stars")
                        .font(.system(size: 24, weight: .medium))
                        .foregroundStyle(.white)
                }
            }
        }
        .buttonStyle(.plain)
        .frame(width: 64, height: 64)
        .disabled(appManager.imageGeneratorState.style == nil || appManager.isGenerating)
    }

    @ViewBuilder
    private var loadingOverlay: some View {
        if appManager.isGenerating {
            ZStack {
                Color.black.opacity(0.3)
                    .ignoresSafeArea()

                HStack(spacing: 8) {
                    ProgressView()
                    Text("Generating image...")
                }
                .padding()
                .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 16))
            }
        }
    }
}

// MARK: - Style Options View (Top)

struct StyleOptionsView: View {
    @Environment(AppManager.self) private var appManager

    var body: some View {
        @Bindable var state = appManager.imageGeneratorState

        VStack(spacing: 16) {
            HStack(spacing: 24) {
                // Recipe Selection
                VStack(alignment: .leading, spacing: 6) {
                    Label("Dish", systemImage: "fork.knife")

                    Picker("", selection: $state.recipe) {
                        ForEach(ImageGeneratorState.recipes, id: \.description) { recipe in
                            Text(recipe)
                        }
                    }
                    .pickerStyle(.segmented)
                    .labelsHidden()
                }

                // Style Selection
                VStack(alignment: .leading, spacing: 6) {
                    Label("Style", systemImage: "paintpalette.fill")

                    HStack(spacing: 10) {
                        ForEach(ImageGeneratorState.styles) { style in
                            StyleCard(
                                style: style,
                                isSelected: state.style == style
                            ) {
                                state.style = style
                            }
                        }
                    }
                }

                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.top, 16)
        }
        .background(Color(nsColor: .controlBackgroundColor))
    }
}

// MARK: - Style Card

struct StyleCard: View {
    let style: ImageStyle
    let isSelected: Bool
    let onSelect: () -> Void

    var body: some View {
        Button(action: onSelect) {
            VStack(spacing: 6) {
                Image(systemName: style.iconName)
                    .font(.title2)

                Text(style.displayName)
                    .font(.caption)
                    .lineLimit(1)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
            .background(
                RoundedRectangle(cornerRadius: 10)
                    .fill(isSelected ? Color.accentColor.opacity(0.15) : Color(nsColor: .controlBackgroundColor))
            )
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(isSelected ? Color.accentColor : Color.clear, lineWidth: 2)
            )
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Image Display View (Middle)

struct ImageDisplayView: View {
    @Environment(AppManager.self) private var appManager

    var body: some View {
        VStack(spacing: 12) {
            if let image = appManager.currentImage {
                Image(nsImage: image)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
            } else {
                VStack(spacing: 16) {
                    Image(systemName: "photo")
                        .font(.system(size: 48))
                        .foregroundStyle(.tertiary)

                    Text("Generated image will appear here")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }

            if let error = appManager.error {
                Text(error.localizedDescription)
                    .foregroundStyle(Color.red)
                    .font(.caption)
            }
        }
        .padding()
    }
}

#Preview {
    ContentView()
        .previewEnvironment(generateImage: true)
}
