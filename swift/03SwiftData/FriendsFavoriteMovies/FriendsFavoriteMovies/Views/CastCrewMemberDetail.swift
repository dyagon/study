//
//  CastCrewMemberDetail.swift
//  FriendsFavoriteMovies
//
//  Created by dyagon on 2026/3/27.
//

import SwiftUI
import SwiftData

struct CastCrewMemberDetail: View {
    @Bindable var member: CastCrewMember

    private var sortedMovies: [Movie] {
        member.movies.sorted { $0.title.localizedStandardCompare($1.title) == .orderedAscending }
    }

    var body: some View {
        Form {
            TextField("Name", text: $member.name)
                .autocorrectionDisabled()

            if !member.movies.isEmpty {
                Section("Movies") {
                    ForEach(sortedMovies) { movie in
                        NavigationLink(movie.title) {
                            MovieDetail(movie: movie)
                        }
                    }
                }
            }
        }
        .navigationTitle(member.name.isEmpty ? "Cast / Crew" : member.name)
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview {
    NavigationStack {
        CastCrewMemberDetail(member: CastCrewMember.sampleData[0])
    }
    .modelContainer(SampleData.shared.modelContainer)
}
