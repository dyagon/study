//
//  CounterModel.swift
//  StateManageDemo
//
//  Created by Dong YANG on 2026/3/25.
//

import Foundation
import Observation

@Observable
@MainActor
class CounterModel {
    var count: Int = 0
    var history: [Int] = []

    func increment() {
        count += 1
        history.append(count)
        trimHistory()
    }

    func decrement() {
        count -= 1
        history.append(count)
        trimHistory()
    }

    private func trimHistory() {
        if history.count > 10 {
            history = Array(history.suffix(10))
        }
    }
}
