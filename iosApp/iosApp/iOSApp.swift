import SwiftUI

@main
struct iOSApp: App {
    init() {
        AgoraRtcBridgeBootstrap.register()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
