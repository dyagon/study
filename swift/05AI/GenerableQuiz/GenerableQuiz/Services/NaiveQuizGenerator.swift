//
//  NaiveQuizGenerator.swift
//  GenerableQuiz
//
//  Naive implementation that returns fixed quiz data.
//

import Foundation

final class NaiveQuizGenerator: QuizGenerator, @unchecked Sendable {
    static let shared = NaiveQuizGenerator()

    static let info = QuizGeneratorInfo(
        id: "naive",
        name: "Naive Quiz Generator"
    )

    static let descriptor = QuizGeneratorDescriptor(
        id: info.id,
        name: info.name,
        availability: .available
    )

    static func checkAvailability() -> QuizGeneratorAvailability {
        return .available
    }

    func generateQuiz(topic: String) -> AsyncThrowingStream<Quiz, Error> {
        AsyncThrowingStream { continuation in
            Task {
                try? await Task.sleep(for: .seconds(0.5))

                let quiz = createQuiz(for: topic)
                continuation.yield(quiz)
                continuation.finish()
            }
        }
    }

    func regenerateQuestion(topic: String, existingQuestions: [String]) -> AsyncThrowingStream<Question, Error> {
        AsyncThrowingStream { continuation in
            Task {
                try? await Task.sleep(for: .seconds(0.5))

                let question = createQuestion(for: topic, excluding: existingQuestions)
                continuation.yield(question)
                continuation.finish()
            }
        }
    }

    private func createQuiz(for topic: String) -> Quiz {
        let questions: [Question]

        switch topic {
        case "Marine Life":
            questions = marineLifeQuestions
        case "Farm Animals":
            questions = farmAnimalQuestions
        case "Modern Reptiles":
            questions = reptileQuestions
        case "Birds of Prey":
            questions = birdQuestions
        default:
            questions = [
                Question(
                    text: "What is \(topic)?",
                    answers: [
                        Answer(text: "An interesting topic", isCorrect: true, explanation: "Yes!"),
                        Answer(text: "A boring topic", isCorrect: false, explanation: "No..."),
                        Answer(text: "An unknown topic", isCorrect: false, explanation: "No..."),
                        Answer(text: "A mystery topic", isCorrect: false, explanation: "No...")
                    ]
                )
            ]
        }

        return Quiz(questions: questions)
    }

    private func createQuestion(for topic: String, excluding: [String]) -> Question {
        let allQuestions = [
            ("Marine Life", marineLifeQuestions),
            ("Farm Animals", farmAnimalQuestions),
            ("Modern Reptiles", reptileQuestions),
            ("Birds of Prey", birdQuestions)
        ]

        for (topicName, questions) in allQuestions {
            if topic == topicName {
                let available = questions.filter { q in !excluding.contains(q.text) }
                if let q = available.randomElement() {
                    return q
                }
            }
        }

        return Question(
            text: "New question about \(topic)?",
            answers: [
                Answer(text: "Answer A", isCorrect: true, explanation: "Yes!"),
                Answer(text: "Answer B", isCorrect: false, explanation: "No..."),
                Answer(text: "Answer C", isCorrect: false, explanation: "No..."),
                Answer(text: "Answer D", isCorrect: false, explanation: "No...")
            ]
        )
    }

    private var marineLifeQuestions: [Question] {
        [
            Question(
                text: "What is the largest animal on Earth?",
                answers: [
                    Answer(text: "Blue Whale", isCorrect: true, explanation: "The blue whale is the largest animal ever known to exist."),
                    Answer(text: "Elephant", isCorrect: false, explanation: "While large, elephants are not the largest animals."),
                    Answer(text: "Giraffe", isCorrect: false, explanation: "Giraffes are tall but not the largest by weight."),
                    Answer(text: "Hippopotamus", isCorrect: false, explanation: "Hippos are large but not the largest.")
                ]
            ),
            Question(
                text: "How do dolphins communicate?",
                answers: [
                    Answer(text: "Clicks and whistles", isCorrect: true, explanation: "Dolphins use echolocation clicks and signature whistles."),
                    Answer(text: "Hand signals", isCorrect: false, explanation: "Dolphins don't use hand signals."),
                    Answer(text: "Facial expressions only", isCorrect: false, explanation: "They use more than just facial expressions."),
                    Answer(text: "Body shaking", isCorrect: false, explanation: "That's not their primary communication method.")
                ]
            ),
            Question(
                text: "What do coral reefs provide for marine ecosystems?",
                answers: [
                    Answer(text: "Habitat and biodiversity", isCorrect: true, explanation: "Coral reefs are called the 'rainforests of the sea'."),
                    Answer(text: "Nothing important", isCorrect: false, explanation: "They are extremely important ecosystems."),
                    Answer(text: "Only fish", isCorrect: false, explanation: "They support thousands of species."),
                    Answer(text: "Warm water only", isCorrect: false, explanation: "They provide much more than warm water.")
                ]
            ),
            Question(
                text: "How long can a sea turtle live?",
                answers: [
                    Answer(text: "Over 100 years", isCorrect: true, explanation: "Sea turtles can live 100+ years."),
                    Answer(text: "About 10 years", isCorrect: false, explanation: "That's too short for sea turtles."),
                    Answer(text: "About 20 years", isCorrect: false, explanation: "They typically live much longer."),
                    Answer(text: "About 50 years", isCorrect: false, explanation: "Many live longer than 50 years.")
                ]
            )
        ]
    }

    private var farmAnimalQuestions: [Question] {
        [
            Question(
                text: "How many stomachs does a cow have?",
                answers: [
                    Answer(text: "Four", isCorrect: true, explanation: "Cows are ruminants with four stomach compartments."),
                    Answer(text: "One", isCorrect: false, explanation: "They actually have four compartments."),
                    Answer(text: "Two", isCorrect: false, explanation: "Not quite - they have four."),
                    Answer(text: "Three", isCorrect: false, explanation: "Close, but they have four.")
                ]
            ),
            Question(
                text: "What is a male duck called?",
                answers: [
                    Answer(text: "Drake", isCorrect: true, explanation: "A male duck is called a drake."),
                    Answer(text: "Rooster", isCorrect: false, explanation: "That's a male chicken."),
                    Answer(text: "Gander", isCorrect: false, explanation: "That's a male goose."),
                    Answer(text: "Stallion", isCorrect: false, explanation: "That's a male horse.")
                ]
            ),
            Question(
                text: "How long does it take for a chicken egg to hatch?",
                answers: [
                    Answer(text: "21 days", isCorrect: true, explanation: "Chicken eggs typically hatch in 21 days."),
                    Answer(text: "7 days", isCorrect: false, explanation: "That's far too short."),
                    Answer(text: "14 days", isCorrect: false, explanation: "Not quite - it's 21 days."),
                    Answer(text: "30 days", isCorrect: false, explanation: "That's too long for chicken eggs.")
                ]
            ),
            Question(
                text: "What do pigs use to cool down?",
                answers: [
                    Answer(text: "Mud wallows", isCorrect: true, explanation: "Pigs lack sweat glands and use mud to cool off."),
                    Answer(text: "Shade only", isCorrect: false, explanation: "They also use mud wallows."),
                    Answer(text: "Water bowls", isCorrect: false, explanation: "They prefer mud wallows."),
                    Answer(text: "Their ears", isCorrect: false, explanation: "Ears don't help with cooling.")
                ]
            )
        ]
    }

    private var reptileQuestions: [Question] {
        [
            Question(
                text: "What is the largest reptile?",
                answers: [
                    Answer(text: "Saltwater crocodile", isCorrect: true, explanation: "Saltwater crocodiles are the largest living reptiles."),
                    Answer(text: "Green anaconda", isCorrect: false, explanation: "While large, they're not the largest reptiles."),
                    Answer(text: "Komodo dragon", isCorrect: false, explanation: "Komodos are large but not the largest."),
                    Answer(text: "Leatherback turtle", isCorrect: false, explanation: "Turtles are reptiles but crocs are larger.")
                ]
            ),
            Question(
                text: "How do snakes smell?",
                answers: [
                    Answer(text: "With their tongue", isCorrect: true, explanation: "Snakes use their forked tongue to collect scent particles."),
                    Answer(text: "With their nose", isCorrect: false, explanation: "They do have a nose but primarily use their tongue."),
                    Answer(text: "With their eyes", isCorrect: false, explanation: "Snakes don't smell with their eyes."),
                    Answer(text: "They can't smell", isCorrect: false, explanation: "Snakes do have a sense of smell.")
                ]
            ),
            Question(
                text: "What is unique about chameleons?",
                answers: [
                    Answer(text: "They change color", isCorrect: true, explanation: "Chameleons can change color for communication and camouflage."),
                    Answer(text: "They fly", isCorrect: false, explanation: "Chameleons don't fly."),
                    Answer(text: "They sing", isCorrect: false, explanation: "Chameleons don't sing."),
                    Answer(text: "They glow", isCorrect: false, explanation: "They change color, not glow.")
                ]
            ),
            Question(
                text: "How do turtles breathe?",
                answers: [
                    Answer(text: "With lungs", isCorrect: true, explanation: "Turtles breathe air with lungs, even sea turtles."),
                    Answer(text: "With gills", isCorrect: false, explanation: "Adult turtles have lungs, not gills."),
                    Answer(text: "Through their shell", isCorrect: false, explanation: "Shells don't help with breathing."),
                    Answer(text: "They don't breathe", isCorrect: false, explanation: "Turtles definitely breathe.")
                ]
            )
        ]
    }

    private var birdQuestions: [Question] {
        [
            Question(
                text: "What is the fastest bird in the world?",
                answers: [
                    Answer(text: "Peregrine falcon", isCorrect: true, explanation: "Peregrine falcons can dive at 240+ mph."),
                    Answer(text: "Golden eagle", isCorrect: false, explanation: "Fast but not the fastest."),
                    Answer(text: "Pigeon", isCorrect: false, explanation: "Pigeons are not the fastest."),
                    Answer(text: "Hummingbird", isCorrect: false, explanation: "Hummingbirds are fast but not the fastest.")
                ]
            ),
            Question(
                text: "How do eagles see?",
                answers: [
                    Answer(text: "4-8 times better than humans", isCorrect: true, explanation: "Eagles have much sharper vision than humans."),
                    Answer(text: "Same as humans", isCorrect: false, explanation: "Eagles have much better vision."),
                    Answer(text: "Worse than humans", isCorrect: false, explanation: "Eagles actually see much better."),
                    Answer(text: "Only at night", isCorrect: false, explanation: "Eagles are daytime hunters.")
                ]
            ),
            Question(
                text: "What do ospreys primarily eat?",
                answers: [
                    Answer(text: "Fish", isCorrect: true, explanation: "Ospreys are fish-eating birds of prey."),
                    Answer(text: "Rabbits", isCorrect: false, explanation: "They specialize in fish."),
                    Answer(text: "Snakes", isCorrect: false, explanation: "Ospreys primarily eat fish."),
                    Answer(text: "Insects", isCorrect: false, explanation: "Insects are not their primary food.")
                ]
            ),
            Question(
                text: "What is special about owl feathers?",
                answers: [
                    Answer(text: "Silent flight", isCorrect: true, explanation: "Owl feathers are specially designed for silent flight."),
                    Answer(text: "Waterproof", isCorrect: false, explanation: "That's not their special feature."),
                    Answer(text: "Glow in dark", isCorrect: false, explanation: "Owl feathers don't glow."),
                    Answer(text: "Color changing", isCorrect: false, explanation: "Owls don't change feather color.")
                ]
            )
        ]
    }
}
