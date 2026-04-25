//
//  BoundsRect.swift
//  SignDecoder
//
//  Created by dyagon on 2026/3/30.
//

import SwiftUI
import Vision


struct BoundsRect: Shape {
    let normalizedRect: NormalizedRect


    func path(in rect: CGRect) -> Path {
        let imageCoordinatesRect = normalizedRect.toImageCoordinates(rect.size, origin: .upperLeft)
        return Path(imageCoordinatesRect)
    }
}
