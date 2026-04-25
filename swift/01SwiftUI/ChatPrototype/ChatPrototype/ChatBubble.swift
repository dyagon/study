//
//  ChatBubble.swift
//  ChatPrototype
//
//  Created by dyagon on 2026/3/27.
//

import SwiftUI

struct ChatBubble: View {
    let text: String
    let color: Color
    let isFromCurrentUser: Bool

    var body: some View {
        HStack {
            if isFromCurrentUser {
                Spacer()
            }

            Text(text)
                .padding()
                .foregroundStyle(.white)
                .background(color, in: RoundedRectangle(cornerRadius: 8))
                .shadow(color: .black.opacity(0.25), radius: 4, x: 0, y: 3)

            if !isFromCurrentUser {
                Spacer()
            }
        }
    }
}

#Preview {
    VStack(spacing: 16) {
        ChatBubble(text: "Hi there!", color: .blue, isFromCurrentUser: true)
        ChatBubble(text: "Hello!", color: .teal, isFromCurrentUser: false)
    }
    .padding()
}
