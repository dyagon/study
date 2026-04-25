//
//  ChartView.swift
//  HikingSurvey
//
//  Created by dyagon on 2026/3/29.
//

import SwiftUI
import Charts

/// Maps every `Sentiment` to its chart color so `foregroundStyle(by:)` legends resolve correctly.
private extension Sentiment {
    static var chartStyleScale: KeyValuePairs<Sentiment, Color> {
        KeyValuePairs(dictionaryLiteral:
            (.veryNegative, Sentiment.veryNegative.sentimentColor),
            (.negative, Sentiment.negative.sentimentColor),
            (.slightlyNegative, Sentiment.slightlyNegative.sentimentColor),
            (.neutral, Sentiment.neutral.sentimentColor),
            (.slightlyPositive, Sentiment.slightlyPositive.sentimentColor),
            (.positive, Sentiment.positive.sentimentColor),
            (.veryPositive, Sentiment.veryPositive.sentimentColor)
        )
    }
}

private struct SentimentCount: Identifiable {
    var id: Sentiment { sentiment }
    var sentiment: Sentiment
    var count: Int
}

// MARK: - Bar chart (aggregated by sentiment)

struct BarChartView: View {
    var responses: [Response]

    /// Top → bottom: very positive … very negative (`Sentiment` display order).
    private var sentimentCounts: [SentimentCount] {
        let grouped = Dictionary(grouping: responses, by: \.sentiment)
        return Sentiment.allCases
            .sorted()
            .map { sentiment in
                SentimentCount(sentiment: sentiment, count: grouped[sentiment]?.count ?? 0)
            }
    }

    private var sentimentYDomain: [String] {
        Sentiment.allCases.sorted().map(\.rawValue)
    }

    var body: some View {
        Group {
            if responses.isEmpty {
                ContentUnavailableView("暂无数据", systemImage: "chart.bar.xaxis")
            } else {
                Chart(sentimentCounts) { item in
                    BarMark(
                        x: .value("Responses", item.count),
                        y: .value("Sentiment", item.sentiment.rawValue)
                    )
                    .foregroundStyle(by: .value("情感档位", item.sentiment))
                }
                .chartForegroundStyleScale(Sentiment.chartStyleScale)
                .chartYScale(domain: sentimentYDomain)
                .chartXAxis {
                    AxisMarks(position: .bottom)
                }
                .chartYAxis {
                    AxisMarks { value in
                        AxisValueLabel {
                            if let raw = value.as(String.self) {
                                Text(raw)
                                    .font(.caption2)
                                    .lineLimit(2)
                                    .multilineTextAlignment(.trailing)
                            }
                        }
                    }
                }
                .chartLegend(position: .bottom, alignment: .center)
            }
        }
        .frame(minHeight: 380)
        .padding()
    }
}

// MARK: - Donut chart (one sector per response)

struct DonutChartView: View {
    var responses: [Response]

    /// Sector order around the ring: more positive first (matches bar chart top→down).
    private var sortedResponses: [Response] {
        responses.sorted {
            if $0.sentiment != $1.sentiment {
                return $0.sentiment < $1.sentiment
            }
            return $0.score > $1.score
        }
    }

    var body: some View {
        Group {
            if responses.isEmpty {
                ContentUnavailableView("暂无数据", systemImage: "chart.pie")
            } else {
                Chart(sortedResponses) { response in
                    SectorMark(
                        angle: .value("Count", 1),
                        innerRadius: .ratio(0.75)
                    )
                    .foregroundStyle(by: .value("情感档位", response.sentiment))
                }
                .chartForegroundStyleScale(Sentiment.chartStyleScale)
                .chartBackground { chartProxy in
                    GeometryReader { geometry in
                        if let anchor = chartProxy.plotFrame {
                            let frame = geometry[anchor]
                            Image(systemName: "figure.hiking")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .frame(height: frame.height * 0.4)
                                .foregroundStyle(Color(white: 0.59))
                                .position(x: frame.midX, y: frame.midY)
                        }
                    }
                }
                .chartLegend(position: .bottom, alignment: .center)
            }
        }
        .frame(minHeight: 260)
        .padding()
    }
}

// MARK: - Sheet: segmented bar / donut

private enum ChartPageTab: String, CaseIterable {
    case bar = "柱状图"
    case donut = "环形图"
}

struct SurveyChartsPageView: View {
    var responses: [Response]
    @Environment(\.dismiss) private var dismiss
    @State private var selectedTab: ChartPageTab = .bar

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                Picker("图表类型", selection: $selectedTab) {
                    ForEach(ChartPageTab.allCases, id: \.self) { tab in
                        Text(tab.rawValue).tag(tab)
                    }
                }
                .pickerStyle(.segmented)
                .padding(.horizontal)

                Group {
                    switch selectedTab {
                    case .bar:
                        BarChartView(responses: responses)
                    case .donut:
                        DonutChartView(responses: responses)
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)

                Text(chartFootnote)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
            }
            .navigationTitle("调查结果")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("关闭") { dismiss() }
                }
            }
        }
    }

    private var chartFootnote: String {
        switch selectedTab {
        case .bar:
            return "横向柱状图自上而下为积极→消极，各档条数（含 0）。"
        case .donut:
            return "环形图扇区顺序为积极→消极（与柱状图一致），每块对应一条回复。"
        }
    }
}

#Preview("Charts page") {
    SurveyChartsPageView(responses: [])
}
