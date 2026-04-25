//
//  KitchenView.swift
//  ImageGenerator
//
//  Created by Dong YANG on 2026/3/30.
//

import SwiftUI

struct KitchenView: View {
    @Environment(AppManager.self) private var appManager

    var body: some View {
        VStack(spacing: 16) {
            Text("Refine Your Dish")
                .font(.largeTitle.weight(.semibold))
            imageArea
            Spacer()
            if let error = appManager.error {
                Text(error.localizedDescription)
                    .foregroundStyle(Color.red)
            }
        }
        .padding()
        .toolbar {
            ToolbarItem(placement: .navigation) {
                Button("Start Over", systemImage: "chevron.left") {
                    appManager.reset()
                }
            }
        }
    }

    private var imageArea: some View {
        Group {
            if let image = appManager.currentImage {
                Image(nsImage: image)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
            } else {
                Rectangle()
                    .fill(.gray.opacity(0.2))
            }
        }
        .frame(width: ImageGeneratorState.imageSize, height: ImageGeneratorState.imageSize)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

#Preview {
    KitchenView()
        .previewEnvironment()
}
