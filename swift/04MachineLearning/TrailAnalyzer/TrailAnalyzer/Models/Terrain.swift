//
//  Terrain.swift
//  TrailAnalyzer
//
//  Created by Dong YANG on 2026/3/30.
//

import Foundation


enum Terrain: String, Identifiable, CaseIterable {
    case paved
    case dirt
    case rocky
    case sandy


    var id: String {
        rawValue
    }
}

