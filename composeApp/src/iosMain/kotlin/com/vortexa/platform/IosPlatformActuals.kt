package com.vortexa.platform

import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UILabel
import platform.UIKit.UIView
import platform.UIKit.UIViewAnimationOptionCurveEaseInOut
import platform.UIKit.UIWindow

internal actual fun platformToast(message: String) {
    val text = message.trim()
    if (text.isEmpty()) return

    dispatch_async(dispatch_get_main_queue()) {
        val window = UIApplication.sharedApplication.keyWindow ?: firstWindow() ?: return@dispatch_async
        val toastView = UIView().apply {
            alpha = 0.0
            backgroundColor = UIColor.blackColor.colorWithAlphaComponent(0.82)
            layer.cornerRadius = 14.0
            layer.masksToBounds = true
            translatesAutoresizingMaskIntoConstraints = false
        }
        val label = UILabel().apply {
            this.text = text
            textColor = UIColor.whiteColor
            font = UIFont.systemFontOfSize(15.0)
            textAlignment = NSTextAlignmentCenter
            numberOfLines = 0
            translatesAutoresizingMaskIntoConstraints = false
        }

        toastView.addSubview(label)
        window.addSubview(toastView)

        NSLayoutConstraint.activateConstraints(
            listOf(
                label.topAnchor.constraintEqualToAnchor(toastView.topAnchor, constant = 12.0),
                label.bottomAnchor.constraintEqualToAnchor(toastView.bottomAnchor, constant = -12.0),
                label.leadingAnchor.constraintEqualToAnchor(toastView.leadingAnchor, constant = 16.0),
                label.trailingAnchor.constraintEqualToAnchor(toastView.trailingAnchor, constant = -16.0),
                toastView.centerXAnchor.constraintEqualToAnchor(window.centerXAnchor),
                toastView.bottomAnchor.constraintEqualToAnchor(window.safeAreaLayoutGuide.bottomAnchor, constant = -72.0),
                toastView.widthAnchor.constraintLessThanOrEqualToAnchor(window.widthAnchor, multiplier = 0.82),
                toastView.widthAnchor.constraintGreaterThanOrEqualToConstant(120.0)
            )
        )

        UIView.animateWithDuration(0.18, animations = {
            toastView.alpha = 1.0
        })
        UIView.animateWithDuration(
            duration = 0.22,
            delay = 2.0,
            options = UIViewAnimationOptionCurveEaseInOut,
            animations = {
                toastView.alpha = 0.0
            },
            completion = {
                toastView.removeFromSuperview()
            }
        )
    }
}

internal actual fun platformOpenUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

internal actual suspend fun platformPickImages(maxCount: Int): List<PickedMedia> = emptyList()

internal actual suspend fun platformPickVideo(): PickedMedia? = null

private fun firstWindow(): UIWindow? {
    return UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
}
