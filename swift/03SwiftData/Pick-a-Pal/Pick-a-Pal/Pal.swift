//
//  Pal.swift
//  Pick-a-Pal
//

import Foundation
import SwiftData

@Model
final class Pal {
    var name: String

    init(name: String) {
        self.name = name
    }
}
