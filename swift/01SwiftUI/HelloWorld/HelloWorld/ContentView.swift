//
//  ContentView.swift
//  HelloWorld
//
//  Created by dyagon on 2026/3/23.
//

import SwiftUI

struct ContentView: View {
    
    @State private var tapCount = 0
    
    var body: some View {
        VStack(spacing: 20) {
            Text("you hit \(tapCount)")
                .font(.largeTitle)
            RatingBadgeView()
            Button(action: {
                tapCount += 1
            }) {
                Text("click me")
                    .foregroundColor(.white)
                    .padding()
                    .background(Color.blue)
                    .cornerRadius(10)
            }
            Image(systemName: "apple.logo")
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundStyle(.tint)
            Text("Hello, world!")
        }
        .padding()
    }
}


struct RatingBadgeView: View {
    var body: some View {
        HStack {
            Image(systemName: "star.fill").foregroundColor(.yellow)
            Text("五星好评")
                .font(.headline)
        }
        .padding()
        .background(Color.gray.opacity(0.2))
        .cornerRadius(10)
    }
}

#Preview {
    ContentView()
}
