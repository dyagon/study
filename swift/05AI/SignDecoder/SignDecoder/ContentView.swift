//
//  ContentView.swift
//  SignDecoder
//
//  Created by dyagon on 2026/3/30.
//

import SwiftUI



struct ContentView: View {
    @State private var showRecognitionSettings = false

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 50) {
                Text("Tap to select a sign to translate")
                    .font(.headline)

                ImageGalleryView()
                Spacer()
            }
            .trailTheme()
            .navigationTitle("Sign Decoder")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showRecognitionSettings = true
                    } label: {
                        Image(systemName: "gearshape")
                    }
                    .accessibilityLabel("Recognition settings")
                }
            }
            .sheet(isPresented: $showRecognitionSettings) {
                NavigationStack {
                    RecognitionSettingsView()
                }
            }
        }
    }
}


#Preview {
    ContentView()
}
