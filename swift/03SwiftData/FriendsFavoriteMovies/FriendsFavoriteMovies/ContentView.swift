//
//  ContentView.swift
//  FriendsFavoriteMovies
//
//  Created by Dong YANG on 2026/3/26.
//

import SwiftUI
import SwiftData

struct ContentView: View {
    var body: some View {
        TabView {
            Tab("Friends", systemImage: "person.and.person") {
                FilteredFriendList()
            }
            
            Tab("Movies", systemImage: "film.stack") {
                FilteredMovieList()
            }

        }
    }
}



#Preview {
    ContentView()
        .modelContainer(SampleData.shared.modelContainer)
}
