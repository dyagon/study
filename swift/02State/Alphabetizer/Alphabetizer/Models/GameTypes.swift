import Foundation

enum DifficultyMode: String, CaseIterable, Identifiable {
    case standard = "Standard"
    case strict = "Strict"
    case iconsOnly = "Icons only"
    case strictIcons = "Strict + icons"

    var id: String { rawValue }

    var subtitle: String {
        switch self {
        case .standard:
            return "Wrong answers do not cost points; new words each round."
        case .strict:
            return "Wrong submit loses 1 point; new words each round."
        case .iconsOnly:
            return "Tiles show icons only; same scoring as Standard."
        case .strictIcons:
            return "Icons only with point loss on mistakes."
        }
    }

    var penalizesWrongSubmit: Bool {
        switch self {
        case .strict, .strictIcons: return true
        default: return false
        }
    }

    var hidesWordsOnTiles: Bool {
        switch self {
        case .iconsOnly, .strictIcons: return true
        default: return false
        }
    }
}

enum VocabularyPack: String, CaseIterable, Identifiable {
    case landTrail = "Land & trail"
    case ocean = "Ocean life"
    case barnyard = "Barnyard"

    var id: String { rawValue }

    var vocabulary: Vocabulary {
        switch self {
        case .landTrail:
            return Vocabulary(words: ["Bear", "Fox", "Frog", "Lizard", "Panda", "Rabbit"])
        case .ocean:
            return Vocabulary(words: ["Crab", "Jellyfish", "Octopus", "Whale"])
        case .barnyard:
            return Vocabulary(words: ["Duck", "Goose", "Horse", "Sheep"])
        }
    }

    static let winsToMasterSet = 3
}
