//
//  ContentView.swift
//  GenerableQuiz
//
//  Main view with topic selection.
//

import SwiftUI

struct ContentView: View {
    @State private var showingSettings = false
    @State private var selectedTopic: Topic?

    private var currentGenerator: QuizGeneratorDescriptor? {
        let selectedId = UserDefaults.standard.string(forKey: "selectedGeneratorId") ?? NaiveQuizGenerator.info.id
        return QuizGeneratorDescriptor.all.first { $0.id == selectedId }
    }

    private func topicSelectionView(for topic: Topic) -> some View {
        Button {
            selectedTopic = topic
        } label: {
            HStack {
                Image(systemName: topic.imageName)
                Text(topic.name)
                Spacer()
                Image(systemName: "arrow.right")
            }
        }
        .buttonStyle(.borderedProminent)
    }

    var body: some View {
        NavigationStack {
            ZStack {
                Color.gray.opacity(0.1)
                    .edgesIgnoringSafeArea(.all)

                VStack(spacing: 16) {
                    Text("Pick a topic for your quiz")
                        .font(.title)

                    ForEach(Topic.topics) { topic in
                        topicSelectionView(for: topic)
                    }

                    Spacer()

                    if let descriptor = currentGenerator {
                        HStack {
                            Text("Generator:")
                                .foregroundStyle(.secondary)
                            Text(descriptor.name)
                                .foregroundStyle(.primary)
                            Spacer()
                        }
                        .font(.footnote)
                        .padding(.horizontal)
                    }
                }
                .padding()
            }
            .navigationTitle("Generable Quiz")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        showingSettings = true
                    } label: {
                        Image(systemName: "gear")
                    }
                }
            }
            .navigationDestination(item: $selectedTopic) { topic in
                QuizContentView(topic: topic.name)
            }
        }
        .sheet(isPresented: $showingSettings) {
            SettingsView(quizManager: QuizManager(topic: "General"))
        }
    }
}

#Preview {
    ContentView()
}
