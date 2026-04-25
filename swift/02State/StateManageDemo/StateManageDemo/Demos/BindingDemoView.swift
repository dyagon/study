//
//  BindingDemoView.swift
//  StateManageDemo
//
//  Created by Dong YANG on 2026/3/25.
//

import SwiftUI

struct BindingDemoView: View {
    @Binding var counter: Int

    var body: some View {
        VStack(spacing: 16) {
            Text("@Binding")
                .font(.headline)

            Text("\(counter)")
                .font(.system(size: 48, weight: .bold, design: .rounded))
                .foregroundStyle(.orange)

            HStack {
                Button("−1") { counter -= 1 }
                    .buttonStyle(.bordered)
                Button("+1") { counter += 1 }
                    .buttonStyle(.bordered)
            }

            Text("(mirrors above)")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(Color.orange.opacity(0.1))
        .cornerRadius(12)
    }
}

#Preview {
    BindingDemoView(counter: .constant(0))
        .frame(width: 250, height: 200)
}
