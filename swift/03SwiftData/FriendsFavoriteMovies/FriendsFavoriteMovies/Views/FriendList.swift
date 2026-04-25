//
//  FriendList.swift
//  FriendsFavoriteMovies
//
//  Created by Dong YANG on 2026/3/26.
//

import SwiftUI
import SwiftData

struct FriendList: View {
    @Query private var friends: [Friend]
    @State private var newFriend: Friend?

    @Environment(\.modelContext) private var context

    init(nameFilter: String = "") {
        let predicate = #Predicate<Friend> { friend in
            nameFilter.isEmpty || friend.name.localizedStandardContains(nameFilter)
        }
        _friends = Query(filter: predicate, sort: \Friend.name)
    }

    var body: some View {
        Group {
            if !friends.isEmpty {
                List {
                    ForEach(friends) { friend in
                        NavigationLink(friend.name) {
                            FriendDetail(friend: friend)
                        }
                    }
                    .onDelete(perform: deleteFriends(indexes:))
                }
            } else {
                ContentUnavailableView("Add Friends", systemImage: "person.and.person")
            }
        }
        .navigationTitle("Friends")
        .toolbar {
            ToolbarItem {
                Button("Add friend", systemImage: "plus", action: addFriend)
            }
            ToolbarItem(placement: .topBarTrailing) {
                EditButton()
            }
        }
        .sheet(item: $newFriend) { friend in
            NavigationStack {
                FriendDetail(friend: friend, isNew: true)
            }
            .interactiveDismissDisabled()
        }
    }

    private func addFriend() {
        let newFriend = Friend(name: "")
        context.insert(newFriend)
        self.newFriend = newFriend
    }

    private func deleteFriends(indexes: IndexSet) {
        for index in indexes {
            context.delete(friends[index])
        }
    }
}


#Preview {
    NavigationStack {
        FriendList()
    }
        .modelContainer(SampleData.shared.modelContainer)
}


#Preview("Empty List") {
    NavigationStack {
        FriendList()
    }
        .modelContainer(for: Friend.self, inMemory: true)
}
