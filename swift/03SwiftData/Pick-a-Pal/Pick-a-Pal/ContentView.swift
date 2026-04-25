//
//  ContentView.swift
//  Pick-a-Pal
//
//  Created by dyagon on 2026/3/25.
//

import SwiftUI
import SwiftData

struct ContentView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \Pal.name) private var pals: [Pal]
    @State private var savedNames: [String] = []
    @State private var nameToAdd = ""
    @State private var pickedName = ""
    @State private var shouldRemovePickedName = false


    var body: some View {
        VStack {
            VStack(spacing: 8) {
                Image(systemName: "person.3.sequence.fill")
                    .foregroundStyle(.tint)
                    .symbolRenderingMode(.hierarchical)
                Text("Pick-a-Pal")
            }
            .font(.title)
            .bold()


            Text(pickedName.isEmpty ? " " : pickedName)
                .font(.title2)
                .bold()
                .foregroundStyle(.tint)


            List {
                ForEach(pals) { pal in
                    Text(pal.name)
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: 8))


            TextField("Add Name", text: $nameToAdd)
                .autocorrectionDisabled()
                .onSubmit {
                    let trimmedName = nameToAdd.trimmingCharacters(in: .whitespacesAndNewlines)
                    let alreadyExists = pals.contains { $0.name == trimmedName }
                    if !trimmedName.isEmpty && !alreadyExists {
                        modelContext.insert(Pal(name: trimmedName))
                        nameToAdd = ""
                    }
                }


            Divider()

            HStack {
                Button("Save List") {
                    savedNames = pals.map(\.name)
                }
                .disabled(pals.isEmpty)

                Button("Load List") {
                    for pal in pals {
                        modelContext.delete(pal)
                    }
                    for name in savedNames {
                        modelContext.insert(Pal(name: name))
                    }
                }
                .disabled(savedNames.isEmpty)
            }


            Toggle("Remove when picked", isOn: $shouldRemovePickedName)


            Button {
                if let randomPal = pals.randomElement() {
                    pickedName = randomPal.name
                    if shouldRemovePickedName {
                        modelContext.delete(randomPal)
                    }
                } else {
                    pickedName = ""
                }
            } label: {
                Text("Pick Random Name")
                    .padding(.vertical, 8)
                    .padding(.horizontal, 16)
            }
            .buttonStyle(.borderedProminent)
            .font(.title2)
        }
        .padding()
    }
}
#Preview {
    ContentView()
        .modelContainer(for: Pal.self, inMemory: true)
}
