import SwiftUI

struct ScoreView: View {
    @Environment(Alphabetizer.self) private var alphabetizer

    var body: some View {
        VStack(spacing: 6) {
            Text("Score: \(alphabetizer.score)")
                .font(.largeTitle)
                .foregroundStyle(Color.purple)
                .bold()
            Text("Record: \(alphabetizer.winCount) wins / \(alphabetizer.totalAttempts) attempts · \(alphabetizer.winPercentageDisplay)")
                .font(.title3)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
    }
}

#Preview {
    ScoreView()
        .environment(Alphabetizer())
}
