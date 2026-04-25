//
//  GeneratorView.swift
//  ImageGenerator
//
//  Created by Dong YANG on 2026/3/31.
//

import SwiftUI

struct GeneratorView: View {
    let generator: GeneratorDescriptor
    let availability: GeneratorAvailability
    let isSelected: Bool
    let onSelect: () -> Void

    var body: some View {
        Button(action: onSelect) {
            HStack(spacing: 12) {
                availabilityIcon

                VStack(alignment: .leading, spacing: 2) {
                    Text(generator.name)
                        .font(.body)
                        .foregroundStyle(.primary)

                    if let message = availability.message {
                        Text(message)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }

                Spacer()

                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundStyle(.blue)
                }
            }
            .padding()
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                RoundedRectangle(cornerRadius: 10)
                    .fill(isSelected ? Color.accentColor.opacity(0.1) : Color(nsColor: .windowBackgroundColor))
            )
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(isSelected ? Color.accentColor : Color.clear, lineWidth: 1.5)
            )
        }
        .buttonStyle(.plain)
        .disabled(!availability.isAvailable)
        .opacity(availability.isAvailable ? 1.0 : 0.5)
    }

    @ViewBuilder
    private var availabilityIcon: some View {
        if availability.isAvailable {
            Image(systemName: "checkmark.circle.fill")
                .foregroundStyle(.green)
                .font(.title3)
        } else {
            Image(systemName: "xmark.circle.fill")
                .foregroundStyle(.red)
                .font(.title3)
        }
    }
}
