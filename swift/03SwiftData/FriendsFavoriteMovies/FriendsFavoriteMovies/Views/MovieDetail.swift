//
//  MovieDetail.swift
//  FriendsFavoriteMovies
//
//  Created by Dong YANG on 2026/3/26.
//

import SwiftUI
import SwiftData



struct MovieDetail: View {
    @Bindable var movie: Movie
    let isNew: Bool


    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var context


    init(movie: Movie, isNew: Bool = false) {
        self.movie = movie
        self.isNew = isNew
    }

    
    var sortedFriends: [Friend] {
        movie.favoritedBy.sorted { first, second in
            first.name < second.name
        }
    }

    private var sortedCastAndCrew: [CastCrewMember] {
        movie.castAndCrew.sorted { first, second in
            first.name < second.name
        }
    }

    private func removeFromFavoritedBy(at offsets: IndexSet) {
        for index in offsets {
            sortedFriends[index].favoriteMovie = nil
        }
    }

    var body: some View {
        Form {
            TextField("Movie title", text: $movie.title)


            DatePicker("Release date", selection: $movie.releaseDate, displayedComponents: .date)

            if !movie.castAndCrew.isEmpty {
                Section("Cast & crew") {
                    ForEach(sortedCastAndCrew) { member in
                        NavigationLink(member.name) {
                            CastCrewMemberDetail(member: member)
                        }
                    }
                }
            }
            
            if !movie.favoritedBy.isEmpty {
                Section("Favorited by") {
                    ForEach(sortedFriends) { friend in
                        Text(friend.name)
                    }
                    .onDelete(perform: removeFromFavoritedBy)
                }
            }
        }
        .navigationTitle(isNew ? "New Movie" : "Movie")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if isNew {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") {
                        context.delete(movie)
                        dismiss()
                    }
                }
            }
        }
    }
}




#Preview {
    NavigationStack {
        MovieDetail(movie: SampleData.shared.movie)
    }
}


#Preview("New Movie") {
    NavigationStack {
        MovieDetail(movie: SampleData.shared.movie, isNew: true)
    }
}

