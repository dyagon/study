import SwiftUI



struct ContentView: View {
    var body: some View {
        VStack(spacing: 16) {
            MasteryAndSettingsView()
            ScoreView()
            MessageView()
            Spacer()
            WordCanvas()
            Spacer()
            SubmitButton()
        }
        .padding(.top, 50)
    }
}


#Preview {
    ContentView()
        .environment(Alphabetizer())
        .frame(width: 834, height: 1000)
}
