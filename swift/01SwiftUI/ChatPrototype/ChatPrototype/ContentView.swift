//
//  ContentView.swift
//  ChatPrototype
//
//  Created by dyagon on 2026/3/27.
//

import SwiftUI


struct ContentView: View {
    var body: some View {
        VStack(spacing: 25) {
            ChatBubble(text: "Knock, knock!", color: .teal, isFromCurrentUser: false)
            ChatBubble(text: "Who's there?", color: .indigo, isFromCurrentUser: true)
            ChatBubble(text: "Lettuce.", color: .teal, isFromCurrentUser: false)
            ChatBubble(text: "Lettuce who?", color: .indigo, isFromCurrentUser: true)
            ChatBubble(text: "Lettuce in, it's cold out here!", color: .teal, isFromCurrentUser: false)
        }
        .padding()
    }
}

#Preview {
    ContentView()
}
