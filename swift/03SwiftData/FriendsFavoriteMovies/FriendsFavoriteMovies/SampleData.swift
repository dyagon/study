//
//  SampleData.swift
//  FriendsFavoriteMovies
//
//  Created by Dong YANG on 2026/3/26.
//

import Foundation
import SwiftData

@MainActor
class SampleData {
    static let shared = SampleData()

    let modelContainer: ModelContainer

    var context: ModelContext {
        modelContainer.mainContext
    }

    var friend: Friend {
        Friend.sampleData.first!
    }
    
    var movie: Movie {
        Movie.sampleData.first!
    }


    private init() {
        let schema = Schema([
            Friend.self,
            Movie.self,
            CastCrewMember.self,
        ])
        let modelConfiguration = ModelConfiguration(schema: schema, isStoredInMemoryOnly: true)

        do {
            modelContainer = try ModelContainer(for: schema, configurations: [modelConfiguration])

            insertSampleData()

            try context.save()
        } catch {
            fatalError("Could not create ModelContainer: \(error)")
        }
    }


    private func insertSampleData() {
        for friend in Friend.sampleData {
            context.insert(friend)
        }
        for movie in Movie.sampleData {
            context.insert(movie)
        }
        for member in CastCrewMember.sampleData {
            context.insert(member)
        }

        Movie.sampleData[0].castAndCrew = [CastCrewMember.sampleData[0], CastCrewMember.sampleData[2]]
        Movie.sampleData[1].castAndCrew = [CastCrewMember.sampleData[1], CastCrewMember.sampleData[3]]
        Movie.sampleData[2].castAndCrew = [CastCrewMember.sampleData[0], CastCrewMember.sampleData[4]]
        Movie.sampleData[3].castAndCrew = [CastCrewMember.sampleData[2]]
        Movie.sampleData[4].castAndCrew = [CastCrewMember.sampleData[3], CastCrewMember.sampleData[4]]
        Movie.sampleData[5].castAndCrew = [CastCrewMember.sampleData[1]]

        
        Friend.sampleData[0].favoriteMovie = Movie.sampleData[1]
        Friend.sampleData[2].favoriteMovie = Movie.sampleData[0]
        Friend.sampleData[3].favoriteMovie = Movie.sampleData[4]
        Friend.sampleData[4].favoriteMovie = Movie.sampleData[0]
        
        
    }
}
