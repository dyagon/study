//
//  FilteredMovieList.swift
//  FriendsFavoriteMovies
//
//  Created by dyagon on 2026/3/27.
//

import SwiftUI
import SwiftData




struct FilteredMovieList: View {
    @State private var searchText = ""
    @State private var sortByReleaseDate = false

    var body: some View {
        NavigationSplitView {
            MovieList(titleFilter: searchText, sortByReleaseDate: sortByReleaseDate)
                .searchable(
                    text: $searchText,
                    placement: .navigationBarDrawer(displayMode: .always)
                )
                .toolbar {
                    ToolbarItem(placement: .topBarTrailing) {
                        Toggle("Sort by release date", isOn: $sortByReleaseDate)
                    }
                }
        } detail: {
            Text("Select a movie")
                .navigationTitle("Movie")
                .navigationBarTitleDisplayMode(.inline)
        }
    }
}


#Preview {
    FilteredMovieList()
        .modelContainer(SampleData.shared.modelContainer)
}
