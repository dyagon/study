import SwiftUI
import SwiftData


@main
struct AlphabetizerApp: App {
    @State private var alphabetizer = Alphabetizer()


    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(alphabetizer)
                .frame(minWidth: 600, idealWidth: 834, minHeight: 800, idealHeight: 1194)

        }
        .windowResizability(.contentSize)

    }
}
