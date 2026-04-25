//
//  ObservableDemoView.swift
//  StateManageDemo
//
//  Created by Dong YANG on 2026/3/25.
//

import SwiftUI

struct ObservableDemoView: View {
    var counterModel: CounterModel

    var body: some View {
        VStack(spacing: 16) {
            Text("@Observable")
                .font(.headline)

            Text("\(counterModel.count)")
                .font(.system(size: 48, weight: .bold, design: .rounded))
                .foregroundStyle(.green)

            HStack {
                Button("−1") { counterModel.decrement() }
                    .buttonStyle(.bordered)
                Button("+1") { counterModel.increment() }
                    .buttonStyle(.bordered)
            }

            Text("History: \(counterModel.history.suffix(3).map(String.init).joined(separator: ", "))")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(Color.green.opacity(0.1))
        .cornerRadius(12)
    }
}

#Preview {
    ObservableDemoView(counterModel: CounterModel())
        .frame(width: 250, height: 200)
}
