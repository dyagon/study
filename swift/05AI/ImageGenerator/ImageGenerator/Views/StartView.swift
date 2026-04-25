//
//  StartView.swift
//  ImageGenerator
//
//  Created by Dong YANG on 2026/3/30.
//

import SwiftUI

struct StartView: View {
    @Environment(AppManager.self) private var appManager

    var body: some View {
        @Bindable var imageGeneratorState = appManager.imageGeneratorState

        VStack(alignment: .leading, spacing: 16) {
            Text("Create a Unique Dish")
                .font(.largeTitle.weight(.semibold))
                .frame(maxWidth: .infinity, alignment: .center)

            Label("Choose a dish", systemImage: "fork.knife")
                .padding(.top, 8)
            Picker("Recipes", selection: $imageGeneratorState.recipe) {
                ForEach(ImageGeneratorState.recipes, id: \.description) { recipe in
                    Text(recipe)
                }
            }

            Label("Choose an image style", systemImage: "paintpalette.fill")
                .padding(.top, 8)
            Picker("Styles", selection: $imageGeneratorState.style) {
                ForEach(ImageGeneratorState.styles) { style in
                    Text(style.displayName)
                        .tag(style as ImageStyle?)
                }
            }

            Spacer()
        }
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("Generate Image") {
                    appManager.generateImage()
                }
                .buttonStyle(.glassProminent)
                .disabled(imageGeneratorState.style == nil)
            }
        }
        .pickerStyle(.segmented)
        .labelsHidden()
        .frame(width: ImageGeneratorState.imageSize)
        .padding()
    }
}

#Preview {
    StartView()
        .previewEnvironment()
}
