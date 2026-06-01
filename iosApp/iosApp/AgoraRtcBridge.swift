import AVFoundation
import Foundation
import UIKit
import ComposeApp

#if canImport(AgoraRtcKit)
import AgoraRtcKit
#endif

enum AgoraRtcBridgeBootstrap {
    private static let factory = AgoraRtcBridgeFactory()

    static func register() {
        RtcNativeBridgeRegistry.shared.registerFactory(factory: factory)
    }
}

private final class AgoraRtcBridgeFactory: NSObject, RtcNativeBridgeFactory {
    func create() -> RtcNativeBridge {
        AgoraRtcBridge()
    }
}

#if canImport(AgoraRtcKit)
private final class AgoraRtcBridge: NSObject, RtcNativeBridge, AgoraRtcEngineDelegate {
    private var engine: AgoraRtcEngineKit?
    private weak var eventSink: RtcNativeEventSink?
    private weak var currentSurface: UIView?
    private var currentChannelName: String = ""
    private var currentToken: String = ""
    private var currentLocalUid: UInt = 0
    private var currentTargetUid: UInt = 0
    private var micMuted = true
    private var speakerMuted = false

    func join(token: String, channelName: String, uid: Int32, eventSink: RtcNativeEventSink) {
        self.eventSink = eventSink
        currentToken = token
        currentChannelName = channelName
        currentLocalUid = UInt(max(uid, 0))

        guard currentLocalUid > 0 else {
            eventSink.onError(message: "RTC 用户 ID 异常")
            return
        }
        guard let appId = resolveAgoraAppId() else {
            eventSink.onError(message: "缺少 Agora AppId 配置")
            return
        }

        requestMediaPermissions { [weak self] granted, message in
            guard let self else { return }
            guard granted else {
                eventSink.onError(message: message ?? "需要相机和麦克风权限才能进入课堂")
                return
            }
            self.createEngineIfNeeded(appId: appId)
            self.joinAgoraChannel()
        }
    }

    func leave() {
        engine?.stopPreview()
        engine?.leaveChannel(nil)
        currentChannelName = ""
        currentToken = ""
        currentSurface = nil
        currentTargetUid = 0
    }

    func setMicMuted(muted: Bool) {
        micMuted = muted
        engine?.muteLocalAudioStream(muted)
    }

    func setSpeakerMuted(muted: Bool) {
        speakerMuted = muted
        engine?.adjustPlaybackSignalVolume(muted ? 0 : 100)
    }

    func bindVideoSurface(surface: Any?, localUid: Int32, targetUid: Int32, screenSharing: Bool) {
        guard let view = surface as? UIView else {
            currentSurface = nil
            return
        }
        currentSurface = view
        currentTargetUid = UInt(max(targetUid, 0))
        bindVideoCanvas(view: view, localUid: UInt(max(localUid, 0)), targetUid: currentTargetUid)
    }

    func dispose() {
        leave()
        AgoraRtcEngineKit.destroy()
        engine = nil
    }

    private func createEngineIfNeeded(appId: String) {
        guard engine == nil else { return }
        let config = AgoraRtcEngineConfig()
        config.appId = appId
        let rtc = AgoraRtcEngineKit.sharedEngine(with: config, delegate: self)
        rtc.setChannelProfile(.liveBroadcasting)
        rtc.setClientRole(.broadcaster)
        rtc.enableVideo()
        rtc.enableAudio()
        rtc.setDefaultAudioRouteToSpeakerphone(true)
        rtc.muteLocalAudioStream(micMuted)
        rtc.adjustPlaybackSignalVolume(speakerMuted ? 0 : 100)
        engine = rtc
    }

    private func joinAgoraChannel() {
        guard let engine, !currentToken.isEmpty, !currentChannelName.isEmpty else { return }
        engine.joinChannel(
            byToken: currentToken,
            channelId: currentChannelName,
            info: nil,
            uid: currentLocalUid
        ) { [weak self] _, uid, _ in
            DispatchQueue.main.async {
                self?.eventSink?.onJoinSuccess(localUid: Int32(uid))
                if let view = self?.currentSurface {
                    self?.bindVideoCanvas(view: view, localUid: uid, targetUid: self?.currentTargetUid ?? uid)
                }
            }
        }
    }

    private func bindVideoCanvas(view: UIView, localUid: UInt, targetUid: UInt) {
        guard let engine else { return }
        let uidToRender = targetUid == 0 ? localUid : targetUid
        let canvas = AgoraRtcVideoCanvas()
        canvas.uid = uidToRender
        canvas.view = view
        if uidToRender == localUid {
            engine.setupLocalVideo(canvas)
            engine.startPreview()
        } else {
            engine.setupRemoteVideo(canvas)
        }
    }

    private func resolveAgoraAppId() -> String? {
        let value = Bundle.main.object(forInfoDictionaryKey: "AgoraAppId") as? String
        let trimmed = value?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if trimmed.isEmpty || trimmed.contains("$(") {
            return nil
        }
        return trimmed
    }

    private func requestMediaPermissions(completion: @escaping (Bool, String?) -> Void) {
        AVCaptureDevice.requestAccess(for: .video) { videoGranted in
            AVAudioSession.sharedInstance().requestRecordPermission { audioGranted in
                DispatchQueue.main.async {
                    if !videoGranted {
                        completion(false, "需要相机权限才能进入课堂")
                    } else if !audioGranted {
                        completion(false, "需要麦克风权限才能进入课堂")
                    } else {
                        completion(true, nil)
                    }
                }
            }
        }
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        DispatchQueue.main.async { [weak self] in
            self?.eventSink?.onRemoteUserJoined(uid: Int32(uid))
            if let view = self?.currentSurface {
                self?.bindVideoCanvas(view: view, localUid: self?.currentLocalUid ?? 0, targetUid: uid)
            }
        }
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        DispatchQueue.main.async { [weak self] in
            self?.eventSink?.onRemoteUserLeft(uid: Int32(uid))
        }
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        DispatchQueue.main.async { [weak self] in
            self?.eventSink?.onError(message: "RTC 连接失败：\(errorCode.rawValue)")
        }
    }
}
#else
private final class AgoraRtcBridge: NSObject, RtcNativeBridge {
    func join(token: String, channelName: String, uid: Int32, eventSink: RtcNativeEventSink) {
        eventSink.onError(message: "Agora SDK 未安装，请运行 pod install 并使用 iosApp.xcworkspace 构建")
    }

    func leave() {}
    func setMicMuted(muted: Bool) {}
    func setSpeakerMuted(muted: Bool) {}
    func bindVideoSurface(surface: Any?, localUid: Int32, targetUid: Int32, screenSharing: Bool) {}
    func dispose() {}
}
#endif
