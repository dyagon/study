//
//  CastCrewMember.swift
//  FriendsFavoriteMovies
//
//  Created by dyagon on 2026/3/27.
//

import Foundation
import SwiftData

@Model
final class CastCrewMember {
    var name: String

    @Relationship(inverse: \Movie.castAndCrew)
    var movies: [Movie] = []

    init(name: String) {
        self.name = name
    }

    static let sampleData = [
        CastCrewMember(name: "Alex Rivera"),
        CastCrewMember(name: "Jordan Kim"),
        CastCrewMember(name: "Sam Patel"),
        CastCrewMember(name: "Taylor Nguyen"),
        CastCrewMember(name: "Morgan Lee"),
    ]
}
