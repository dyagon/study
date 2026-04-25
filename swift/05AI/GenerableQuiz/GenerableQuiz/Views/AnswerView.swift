//
//  AnswerView.swift
//  GenerableQuiz
//
//  Individual answer display with selection capability.
//

import SwiftUI

struct AnswerView: View {
    var displayAnswer: Answer
    @Binding var selectedAnswer: Answer?

    var body: some View {
        Button {
            selectedAnswer = displayAnswer
        } label: {
            HStack {
                Image(systemName: imageName)
                Text(displayAnswer.text)
                    .multilineTextAlignment(.leading)
                    .foregroundStyle(.primary)
                Spacer()
            }
        }
        .buttonStyle(.bordered)
        .foregroundStyle(tintColor)
        .tint(tintColor)
    }

    private var tintColor: Color {
        if selectedAnswer?.id == displayAnswer.id {
            return displayAnswer.isCorrect ? .green : .red
        } else {
            return .secondary
        }
    }

    private var imageName: String {
        if selectedAnswer?.id == displayAnswer.id {
            return displayAnswer.isCorrect ? "checkmark.circle.fill" : "xmark.circle.fill"
        } else {
            return "circle"
        }
    }
}

#Preview {
    @Previewable @State var correctAnswer: Answer?
    @Previewable @State var incorrectAnswer: Answer?

    VStack {
        AnswerView(displayAnswer: Answer.correctAnswer, selectedAnswer: $incorrectAnswer)
        AnswerView(displayAnswer: Answer.correctAnswer, selectedAnswer: $correctAnswer)
        AnswerView(displayAnswer: Answer.incorrectAnswer, selectedAnswer: $incorrectAnswer)
    }
}
