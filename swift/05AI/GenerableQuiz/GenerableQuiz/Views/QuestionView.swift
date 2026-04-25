//
//  QuestionView.swift
//  GenerableQuiz
//
//  Individual question display with regenerate capability.
//

import SwiftUI

struct QuestionView: View {
    var question: Question
    @State var selectedAnswer: Answer?
    @State private var showConfirmation = false
    @Environment(\.quizManager) private var quizManager

    var body: some View {
        VStack(alignment: .leading) {
            HStack {
                Text(question.text)

                Spacer()

                Button {
                    showConfirmation = true
                } label: {
                    Image(systemName: "arrow.counterclockwise")
                }
                .confirmationDialog("Regenerate question", isPresented: $showConfirmation) {
                    Button("Regenerate", role: .destructive) {
                        quizManager?.regenerate(question: question)
                    }
                } message: {
                    Text("Replace this question with a new one?")
                }
            }
            .padding(.bottom, 8)

            ForEach(question.answers) { answer in
                AnswerView(displayAnswer: answer, selectedAnswer: $selectedAnswer)
            }

            if let selectedAnswer = selectedAnswer {
                Text(selectedAnswer.explanation)
                    .font(.body.italic())
                    .padding(.top, 8)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(24)
        .background(.background, in: RoundedRectangle(cornerRadius: 24.0))
    }
}

#Preview {
    QuestionView(question: Question.sample)
        .environment(\.quizManager, QuizManager(topic: "Marine life"))
}
