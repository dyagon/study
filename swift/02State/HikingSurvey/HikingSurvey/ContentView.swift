//
//  ContentView.swift
//  HikingSurvey
//
//  Created by dyagon on 2026/3/29.
//

import SwiftUI

struct ContentView: View {
    @FocusState private var textFieldIsFocused: Bool
    @State var responses: [Response] = []
    @State private var responseText = ""
    @State private var showChartsPage = false
    var scorer = Scorer()

    func saveResponse(text: String) {
        let result = scorer.analyze(text)
        let response = Response(text: text, score: result.score, languageCode: result.languageCode)
        responses.insert(response, at: 0)
    }

    var body: some View {
        NavigationStack {
            VStack {
                Text("Opinions on Hiking")
                    .frame(maxWidth: .infinity)
                    .font(.title)
                    .padding(.top, 24)
                ScrollView {
                    ForEach(responses) { response in
                        ResponseView(response: response)
                    }
                }
                HStack {
                    TextField("What do you think about hiking?", text: $responseText, axis: .vertical)
                        .textFieldStyle(.roundedBorder)
                        .lineLimit(5)
                    Button("Done") {
                        guard !responseText.isEmpty else { return }
                        saveResponse(text: responseText)
                        responseText = ""
                        textFieldIsFocused = false
                    }
                    .padding(.horizontal, 4)
                }
                .padding(.bottom, 8)
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showChartsPage = true
                    } label: {
                        Label("图表", systemImage: "chart.bar.doc.horizontal")
                    }
                }
            }
            .sheet(isPresented: $showChartsPage) {
                SurveyChartsPageView(responses: responses)
            }
        }
        .onAppear {
            for response in Response.sampleResponses {
                saveResponse(text: response)
            }
        }
        .padding(.horizontal)
        .background(Color(white: 0.94))
    }
}

#Preview {
    ContentView()
}
