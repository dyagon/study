//
//  StateDemoView.swift
//  StateManageDemo
//
//  Created by Dong YANG on 2026/3/25.
//

import SwiftUI

struct StateDemoView: View {
    @State private var count = 0

    var body: some View {
        VStack(spacing: 16) {
            Text("@State")
                .font(.headline)

            Text("\(count)")
                .font(.system(size: 48, weight: .bold, design: .rounded))
                .foregroundStyle(.blue)

            HStack {
                Button("−1") { count -= 1 }
                    .buttonStyle(.bordered)
                Button("+1") { count += 1 }
                    .buttonStyle(.bordered)
            }
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(Color.blue.opacity(0.1))
        .cornerRadius(12)
    }
}

#Preview {
    StateDemoView()
        .frame(width: 250, height: 200)
}
