//
//  FilteredFriendList.swift
//  FriendsFavoriteMovies
//
//  Created by dyagon on 2026/3/27.
//

import SwiftUI
import SwiftData

struct FilteredFriendList: View {
    @State private var searchText = ""

    var body: some View {
        NavigationSplitView {
            FriendList(nameFilter: searchText)
                .searchable(
                    text: $searchText,
                    placement: .navigationBarDrawer(displayMode: .always)
                )
        } detail: {
            Text("Select a friend")
                .navigationTitle("Friend")
                .navigationBarTitleDisplayMode(.inline)
        }
    }
}

#Preview {
    FilteredFriendList()
        .modelContainer(SampleData.shared.modelContainer)
}
