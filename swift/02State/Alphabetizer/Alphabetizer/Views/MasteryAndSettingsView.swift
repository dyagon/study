import SwiftUI

struct MasteryAndSettingsView: View {
    @Environment(Alphabetizer.self) private var alphabetizer

    private var settingsLocked: Bool {
        alphabetizer.message != .instructions
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Menu {
                    ForEach(DifficultyMode.allCases) { mode in
                        Button {
                            alphabetizer.difficulty = mode
                        } label: {
                            HStack {
                                Text(mode.rawValue)
                                if mode == alphabetizer.difficulty {
                                    Image(systemName: "checkmark")
                                }
                            }
                        }
                    }
                } label: {
                    Label(alphabetizer.difficulty.rawValue, systemImage: "slider.horizontal.3")
                        .font(.headline)
                }
                .disabled(settingsLocked)

                Spacer()

                Menu {
                    ForEach(VocabularyPack.allCases) { pack in
                        Button {
                            alphabetizer.selectVocabularyPack(pack)
                        } label: {
                            packLabel(pack)
                        }
                    }
                } label: {
                    Label(alphabetizer.currentPack.rawValue, systemImage: "books.vertical")
                        .font(.headline)
                        .lineLimit(1)
                }
                .disabled(settingsLocked)
                .frame(maxWidth: 220, alignment: .trailing)
            }

            Text(alphabetizer.difficulty.subtitle)
                .font(.caption)
                .foregroundStyle(.tertiary)
                .fixedSize(horizontal: false, vertical: true)

            Text(alphabetizer.packsMasteredSummary)
                .font(.subheadline.weight(.semibold))

            Text(alphabetizer.masteryProgressDescription)
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .fixedSize(horizontal: false, vertical: true)

            if let note = alphabetizer.celebrationNotice {
                Text(note)
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(Color.green)
                    .padding(10)
                    .frame(maxWidth: .infinity)
                    .background(RoundedRectangle(cornerRadius: 10).fill(Color.green.opacity(0.15)))
            }
        }
        .padding(.horizontal, 8)
    }

    @ViewBuilder
    private func packLabel(_ pack: VocabularyPack) -> some View {
        HStack {
            Text(pack.rawValue)
            Spacer(minLength: 12)
            if alphabetizer.masteredPackIds.contains(pack.rawValue) {
                Image(systemName: "medal.fill")
                    .foregroundStyle(.yellow)
            }
            if pack == alphabetizer.currentPack {
                Image(systemName: "checkmark.circle.fill")
                    .foregroundStyle(.purple)
            }
        }
    }
}

#Preview {
    MasteryAndSettingsView()
        .environment(Alphabetizer())
        .padding()
}
