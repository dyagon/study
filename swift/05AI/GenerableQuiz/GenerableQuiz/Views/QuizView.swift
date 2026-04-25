//
//  QuizView.swift
//  GenerableQuiz
//
//  Main quiz view showing generated questions.
//

import SwiftUI

struct QuizView: View {
    @Environment(\.quizManager) private var quizManager

    @ViewBuilder
    private var content: some View {
        let lastQuestion = quizManager?.quiz?.questions.last

        Color.gray.opacity(0.1)
            .edgesIgnoringSafeArea(.all)

        ScrollViewReader { value in
            ScrollView {
                quizStack
            }
            .onChange(of: lastQuestion?.answers.count) {
                withAnimation {
                    value.scrollTo(lastQuestion?.id)
                }
            }
            .onChange(of: quizManager?.isGenerating) {
                withAnimation {
                    value.scrollTo(quizManager?.quiz?.questions.first?.id)
                }
            }
        }

        if quizManager?.isGenerating == true {
            HStack {
                ProgressView()
                Text("Generating...")
            }
            .frame(width: 200, height: 75)
            .background(.ultraThinMaterial)
            .clipShape(RoundedRectangle(cornerRadius: 16.0))
        }
    }

    private var quizStack: some View {
        VStack(spacing: 16) {
            if let error = quizManager?.error {
                Label(error.localizedDescription, systemImage: "xmark.circle")
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .foregroundStyle(Color.red)
                    .padding(.horizontal)
            }

            if let questions = quizManager?.quiz?.questions {
                ForEach(questions) { question in
                    QuestionView(question: question)
                        .disabled(quizManager?.isGenerating == true)
                        .padding(.vertical, 8)
                        .id(question.id)
                }
            }

            Button {
                quizManager?.generateQuiz()
            } label: {
                Text("Start a new quiz")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .disabled(quizManager?.isGenerating == true)
        }
        .navigationTitle(quizManager?.topic ?? "Quiz")
        .padding()
    }

    var body: some View {
        NavigationStack {
            ZStack {
                content
            }
            .onAppear {
                quizManager?.generateQuiz()
            }
        }
    }
}

#Preview {
    QuizView()
        .environment(\.quizManager, QuizManager(topic: "Marine Life"))
}
