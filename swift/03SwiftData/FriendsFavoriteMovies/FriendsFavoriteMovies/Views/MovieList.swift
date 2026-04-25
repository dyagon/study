//
//  MovieList.swift
//  FriendsFavoriteMovies
//
//  Created by Dong YANG on 2026/3/26.
//

import SwiftUI
import SwiftData



struct MovieList: View {
    @Query private var movies: [Movie]
    @Environment(\.modelContext) private var context
    @State private var newMovie: Movie?


    init(titleFilter: String = "", sortByReleaseDate: Bool = false) {
        let filterValue = titleFilter
        let predicate = #Predicate<Movie> { movie in
            filterValue.isEmpty
                || movie.title.localizedStandardContains(filterValue)
                || movie.castAndCrew.contains { member in
                    member.name.localizedStandardContains(filterValue)
                }
        }
        if sortByReleaseDate {
            _movies = Query(filter: predicate, sort: \Movie.releaseDate)
        } else {
            _movies = Query(filter: predicate, sort: \Movie.title)
        }
    }


    var body: some View {
        Group {
            if !movies.isEmpty {
                List {
                    ForEach(movies) { movie in
                        NavigationLink(movie.title) {
                            MovieDetail(movie: movie)
                        }
                    }
                    .onDelete { indexSet in
                        deleteMovies(indexes: indexSet)
                    }
                }
            } else {
                ContentUnavailableView("Add Movies", systemImage: "film.stack")
            }
        }
        .navigationTitle("Movies")
        .toolbar {
            ToolbarItem {
                Button("Add movie", systemImage: "plus", action: addMovie)
            }
            ToolbarItem(placement: .topBarTrailing) {
                EditButton()
            }
        }
        .sheet(item: $newMovie) { movie in
            NavigationStack {
                MovieDetail(movie: movie, isNew: true)
            }
            .interactiveDismissDisabled()
        }
    }


    private func addMovie() {
        let newMovie = Movie(title: "", releaseDate: .now)
        context.insert(newMovie)
        self.newMovie = newMovie
    }


    private func deleteMovies(indexes: IndexSet) {
        for index in indexes {
            context.delete(movies[index])
        }
    }
}


#Preview {
    NavigationStack {
        MovieList()
            .modelContainer(SampleData.shared.modelContainer)
    }
}


#Preview("Filtered") {
    NavigationStack {
        MovieList(titleFilter: "tr")
            .modelContainer(SampleData.shared.modelContainer)
    }
}


#Preview("Empty List") {
    NavigationStack {
        MovieList()
            .modelContainer(for: Movie.self, inMemory: true)
    }
}
