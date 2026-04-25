//
//  Moment.swift
//  GratefulMoments
//
//  Created by dyagon on 2026/3/29.
//

import Foundation
import SwiftData
import UIKit



@Model
class Moment {
    var title: String
    var note: String
    var imageData: Data?
    var timestamp: Date

    var badges: [Badge]

    init(title: String, note: String, imageData: Data? = nil, timestamp: Date = .now) {
        self.title = title
        self.note = note
        self.imageData = imageData
        self.timestamp = timestamp
        self.badges = []
    }

    var image: UIImage? {
        imageData.flatMap {
            UIImage(data: $0)
        }
    }
}


extension Moment {
    /// 纯文字 moment，用于布局 / Preview。
    static let sample = sampleData[5]
    /// 长笔记，用于多行排版测试（`daysAgo: 19` 的「Rain on the window」）。
    static let longTextSample = sampleData[10]
    /// 带图 + 笔记，用于六边形有图样式（`daysAgo: 29` 的 Study）。
    static let imageSample = sampleData[0]


    /// 生成「几天前」某一时刻的时间戳（按当前日历、`daysAgo = 0` 表示今天）。
    /// - Parameters:
    ///   - daysAgo: 相对今天 0 点往回数的完整自然日。
    ///   - minuteOffset: 同一天内多条时错开排序用。
    private static func sampleDate(daysAgo: Int, minuteOffset: Int = 0) -> Date {
        let cal = Calendar.current
        let startOfToday = cal.startOfDay(for: .now)
        let day = cal.date(byAdding: .day, value: -daysAgo, to: startOfToday) ?? startOfToday
        let noon = cal.date(byAdding: .hour, value: 12, to: day) ?? day
        return cal.date(byAdding: .minute, value: minuteOffset, to: noon) ?? noon
    }


    /// 预置数据：`DataContainer(includeSampleMoments: true)` 会按数组顺序插入并逐条 `unlockBadges`。
    ///
    /// 徽章对应关系（条数达标且满足条件时解锁，详见 `BadgeManager.shouldUnlock`）：
    /// - **firstEntry**：≥1 条 moment。
    /// - **fiveStars**：≥5 条。
    /// - **shutterbug**：≥3 条带图。
    /// - **expressive**：≥5 条「有图且笔记非空」。
    /// - **perfectTen**：≥10 条，且上述四类已解锁后仍可解 Perfect 10。
    /// - **twentyMoments**：≥20 条。
    /// - **thirtyDayStreak**：连续日历日 streak ≥30（此处用 `daysAgo` 29…0 各一天共 30 条）。
    ///
    /// 时间顺序：按 `timestamp` 升序 = `daysAgo` 从大到小（越「几天前」越早）。
    static let sampleData: [Moment] = [
        // MARK: 连续 30 天各 1 条 → 测 Streak + 最终条数 30（twenty / thirty 等）

        // daysAgo 29…25：5 条「图 + 笔记」→ 凑 expressive（5）与 shutterbug（≥3）
        Moment(
            title: "Study streak",
            note: "First photo note for badge tests.",
            imageData: UIImage(named: "Study")?.pngData(),
            timestamp: sampleDate(daysAgo: 29, minuteOffset: 0)
        ),
        Moment(
            title: "Relax streak",
            note: "Second photo note.",
            imageData: UIImage(named: "Relax")?.pngData(),
            timestamp: sampleDate(daysAgo: 28, minuteOffset: 0)
        ),
        Moment(
            title: "Concert 1",
            note: "Third photo note.",
            imageData: UIImage(named: "Concert")?.pngData(),
            timestamp: sampleDate(daysAgo: 27, minuteOffset: 0)
        ),
        Moment(
            title: "Study again",
            note: "Fourth photo note.",
            imageData: UIImage(named: "Study")?.pngData(),
            timestamp: sampleDate(daysAgo: 26, minuteOffset: 0)
        ),
        Moment(
            title: "Concert 2",
            note: "Fifth photo note — expressive 条件凑满。",
            imageData: UIImage(named: "Concert")?.pngData(),
            timestamp: sampleDate(daysAgo: 25, minuteOffset: 0)
        ),

        // daysAgo 24…20：纯文字，继续累加条数 toward 10 / 20
        Moment(
            title: "Tomato",
            note: "Picked my first homegrown tomato!",
            timestamp: sampleDate(daysAgo: 24)
        ),
        Moment(
            title: "Family love",
            note: "",
            timestamp: sampleDate(daysAgo: 23)
        ),
        Moment(
            title: "Tea",
            note: "Quiet cup before the day started.",
            timestamp: sampleDate(daysAgo: 22)
        ),
        Moment(
            title: "A+",
            note: "",
            timestamp: sampleDate(daysAgo: 21)
        ),
        Moment(
            title: "Language exchange",
            note: "Learned 12 new phrases in Korean",
            timestamp: sampleDate(daysAgo: 20)
        ),

        // 第 10 条前后：fiveStars / shutterbug / expressive 已满足 → 可解锁 perfectTen（≥10 条）
        Moment(
            title: "Rain on the window",
            note: "Soundtracked an afternoon of reading. Nothing urgent — just grateful to be still for a little while.",
            timestamp: sampleDate(daysAgo: 19)
        ),

        // daysAgo 18…10：继续加到 20 条
        Moment(
            title: "Water · Sun · Soil",
            note: "Three small things that kept the basil alive this week.",
            timestamp: sampleDate(daysAgo: 18)
        ),
        Moment(
            title: "Walk",
            note: "Short walk after dinner.",
            timestamp: sampleDate(daysAgo: 17)
        ),
        Moment(
            title: "Call home",
            note: "Caught up with parents.",
            timestamp: sampleDate(daysAgo: 16)
        ),
        Moment(
            title: "Coffee",
            note: "Good beans this week.",
            timestamp: sampleDate(daysAgo: 15)
        ),
        Moment(
            title: "Sleep",
            note: "Actually rested.",
            timestamp: sampleDate(daysAgo: 14)
        ),
        Moment(
            title: "Book",
            note: "Finished a chapter.",
            timestamp: sampleDate(daysAgo: 13)
        ),
        Moment(
            title: "Music",
            note: "Found a new album.",
            timestamp: sampleDate(daysAgo: 12)
        ),
        Moment(
            title: "Friends",
            note: "Quick lunch.",
            timestamp: sampleDate(daysAgo: 11)
        ),
        // 按时间第 20 条 → 插入并解锁时满足 twentyMoments（≥20）
        Moment(
            title: "Sky",
            note: "Clear blue — chronological #20 for twentyMoments.",
            timestamp: sampleDate(daysAgo: 10)
        ),

        // daysAgo 9…1：继续连续日直至满 30 天
        Moment(title: "Day −9", note: "", timestamp: sampleDate(daysAgo: 9)),
        Moment(title: "Day −8", note: "", timestamp: sampleDate(daysAgo: 8)),
        Moment(title: "Day −7", note: "", timestamp: sampleDate(daysAgo: 7)),
        Moment(title: "Day −6", note: "", timestamp: sampleDate(daysAgo: 6)),
        Moment(title: "Day −5", note: "", timestamp: sampleDate(daysAgo: 5)),
        Moment(title: "Day −4", note: "", timestamp: sampleDate(daysAgo: 4)),
        Moment(title: "Day −3", note: "", timestamp: sampleDate(daysAgo: 3)),
        Moment(title: "Day −2", note: "", timestamp: sampleDate(daysAgo: 2)),
        Moment(title: "Day −1", note: "", timestamp: sampleDate(daysAgo: 1)),

        // daysAgo 0：今天，时间线最后一条常用 large 布局
        Moment(
            title: "Today",
            note: "Most recent sample — thirtyDayStreak 需含今天连续 30 日。",
            timestamp: sampleDate(daysAgo: 0, minuteOffset: 30)
        ),
    ]


    /// Picks a hex size from content: short blurbs stay compact; photos and long copy scale up.
    func hexagonLayout(isLastInTimeline: Bool) -> HexagonLayout {
        if isLastInTimeline {
            return .large
        }
        let hasImage = image != nil
        if hasImage, note.count > 55 {
            return .medium
        }
        if hasImage {
            return .standard
        }
        if note.count > 95 || title.count > 26 {
            return .medium
        }
        if title.count <= 10, note.isEmpty, !hasImage {
            return .compact
        }
        return .standard
    }
}
