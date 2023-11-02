import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        KoinHelper().startKoin()
    }
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
