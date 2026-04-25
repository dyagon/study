//
//  LockedBadgeView.swift
//  GratefulMoments
//
//  Created by dyagon on 2026/3/29.
//

import SwiftUI


struct LockedBadgeView: View {
    var badge: Badge


    var body: some View {
        HStack {
            Image(badge.details.lockedImage)
                .resizable()
                .frame(width: 70, height: 70)
                .padding(.trailing, 16)
            VStack(alignment: .leading, spacing: 8) {
                Text(badge.details.title)
                    .font(.subheadline.bold())
                Text(badge.details.requirements)
                    .font(.caption)
            }
            Spacer()
        }
        .padding()
        .background(Color.secondary.opacity(0.2))
        .clipShape(RoundedRectangle(cornerRadius: 16.0))
    }
}


#Preview {
    LockedBadgeView(badge: .sample)
}
