package com.vortexa.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSItemProvider
import platform.Foundation.NSItemProviderReadingProtocol
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UILabel
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIView
import platform.UIKit.UIViewAnimationOptionCurveEaseInOut
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume

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

internal actual suspend fun platformPickImages(maxCount: Int): List<PickedMedia> {
    if (maxCount <= 0) return emptyList()
    return pickImagesFromPhotoLibrary(maxCount)
}

internal actual suspend fun platformTakePhoto(): PickedMedia? {
    val camera = platform.UIKit.UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
    return if (UIImagePickerController.isSourceTypeAvailable(camera)) {
        pickImageFromSource(camera)
    } else {
        platformToast("当前设备不支持拍照")
        null
    }
}

internal actual suspend fun platformPickVideo(): PickedMedia? = null

private val activeImagePickerDelegates = mutableMapOf<UIImagePickerController, ImagePickerDelegate>()
private val activePhotoPickerDelegates = mutableMapOf<PHPickerViewController, PhotoPickerDelegate>()

private suspend fun pickImagesFromPhotoLibrary(maxCount: Int): List<PickedMedia> =
    suspendCancellableCoroutine { continuation ->
        dispatch_async(dispatch_get_main_queue()) {
            val presenter = topViewController() ?: run {
                continuation.resume(emptyList())
                return@dispatch_async
            }
            val configuration = PHPickerConfiguration().apply {
                selectionLimit = maxCount.toLong()
                filter = PHPickerFilter.imagesFilter
            }
            val picker = PHPickerViewController(configuration = configuration)
            val delegate = PhotoPickerDelegate(
                picker = picker,
                onFinished = { media ->
                    activePhotoPickerDelegates.remove(picker)
                    if (continuation.isActive) continuation.resume(media)
                }
            )
            activePhotoPickerDelegates[picker] = delegate
            picker.delegate = delegate
            continuation.invokeOnCancellation {
                dispatch_async(dispatch_get_main_queue()) {
                    activePhotoPickerDelegates.remove(picker)
                    picker.dismissViewControllerAnimated(true, completion = null)
                }
            }
            presenter.presentViewController(picker, animated = true, completion = null)
        }
    }

private suspend fun pickImageFromSource(sourceType: platform.UIKit.UIImagePickerControllerSourceType): PickedMedia? =
    suspendCancellableCoroutine { continuation ->
        dispatch_async(dispatch_get_main_queue()) {
            val presenter = topViewController() ?: run {
                continuation.resume(null)
                return@dispatch_async
            }
            val picker = UIImagePickerController().apply {
                this.sourceType = sourceType
                allowsEditing = true
            }
            val delegate = ImagePickerDelegate(
                picker = picker,
                onFinished = { media ->
                    activeImagePickerDelegates.remove(picker)
                    if (continuation.isActive) continuation.resume(media)
                }
            )
            activeImagePickerDelegates[picker] = delegate
            picker.delegate = delegate
            continuation.invokeOnCancellation {
                dispatch_async(dispatch_get_main_queue()) {
                    activeImagePickerDelegates.remove(picker)
                    picker.dismissViewControllerAnimated(true, completion = null)
                }
            }
            presenter.presentViewController(picker, animated = true, completion = null)
        }
    }

private class ImagePickerDelegate(
    private val picker: UIImagePickerController,
    private val onFinished: (PickedMedia?) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerEditedImage] as? UIImage
            ?: didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val media = image?.writeJpegToTempFile()?.let { PickedMedia(uri = it, type = MediaType.Image) }
        picker.dismissViewControllerAnimated(true) {
            onFinished(media)
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true) {
            onFinished(null)
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class PhotoPickerDelegate(
    private val picker: PHPickerViewController,
    private val onFinished: (List<PickedMedia>) -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val results = didFinishPicking.filterIsInstance<PHPickerResult>()
        if (results.isEmpty()) {
            onFinished(emptyList())
            return
        }
        loadImages(results)
    }

    private fun loadImages(results: List<PHPickerResult>) {
        val imageClass = UIImage.`class`() as? NSItemProviderReadingProtocol ?: run {
            onFinished(emptyList())
            return
        }
        val picked = mutableListOf<PickedMedia>()
        var pending = results.size

        fun completeOne() {
            pending -= 1
            if (pending == 0) {
                if (picked.isEmpty()) {
                    platformToast("图片读取失败，请重试或换一张图片")
                }
                onFinished(picked)
            }
        }

        for (result in results) {
            val provider = result.itemProvider
            if (!provider.canLoadObjectOfClass(imageClass)) {
                loadImageData(provider) { path ->
                    if (path != null) {
                        picked.add(PickedMedia(uri = path, type = MediaType.Image))
                    }
                    completeOne()
                }
                continue
            }
            provider.loadObjectOfClass(imageClass) { item, error ->
                dispatch_async(dispatch_get_main_queue()) {
                    val image = item as? UIImage
                    val path = image?.writeJpegToTempFile(prefix = "post_image")
                    if (path != null) {
                        picked.add(PickedMedia(uri = path, type = MediaType.Image))
                        completeOne()
                    } else {
                        loadImageData(provider) { fallbackPath ->
                            if (fallbackPath != null) {
                                picked.add(PickedMedia(uri = fallbackPath, type = MediaType.Image))
                            }
                            completeOne()
                        }
                    }
                }
            }
        }
    }

    private fun loadImageData(provider: NSItemProvider, onLoaded: (String?) -> Unit) {
        val typeIdentifier = provider.registeredTypeIdentifiers
            .filterIsInstance<String>()
            .firstOrNull { it.isSupportedImageTypeIdentifier() }

        if (typeIdentifier == null) {
            onLoaded(null)
            return
        }

        provider.loadDataRepresentationForTypeIdentifier(typeIdentifier) { data, error ->
            dispatch_async(dispatch_get_main_queue()) {
                onLoaded(data?.writeImageDataToTempFile(typeIdentifier))
            }
        }
    }
}

private fun UIImage.writeJpegToTempFile(prefix: String = "avatar"): String? {
    val data: NSData = UIImageJPEGRepresentation(this, 0.9) ?: return null
    val fileName = "${prefix}_${NSUUID.UUID().UUIDString}.jpg"
    val path = NSTemporaryDirectory().trimEnd('/') + "/" + fileName
    val ok = NSFileManager.defaultManager.createFileAtPath(path, contents = data, attributes = null)
    return if (ok) path.toFileUriString() else null
}

private fun NSData.writeImageDataToTempFile(typeIdentifier: String, prefix: String = "post_image"): String? {
    val extension = typeIdentifier.imageFileExtension()
    val fileName = "${prefix}_${NSUUID.UUID().UUIDString}.$extension"
    val path = NSTemporaryDirectory().trimEnd('/') + "/" + fileName
    val ok = NSFileManager.defaultManager.createFileAtPath(path, contents = this, attributes = null)
    return if (ok) path.toFileUriString() else null
}

private fun String.toFileUriString(): String =
    NSURL.fileURLWithPath(this).absoluteString ?: this

private fun String.isSupportedImageTypeIdentifier(): Boolean {
    val value = lowercase()
    return value.contains("image") ||
        value.contains("jpeg") ||
        value.contains("jpg") ||
        value.contains("png") ||
        value.contains("gif") ||
        value.contains("heic") ||
        value.contains("heif") ||
        value.contains("webp")
}

private fun String.imageFileExtension(): String {
    val value = lowercase()
    return when {
        value.contains("png") -> "png"
        value.contains("gif") -> "gif"
        value.contains("heic") -> "heic"
        value.contains("heif") -> "heif"
        value.contains("webp") -> "webp"
        else -> "jpg"
    }
}

private fun topViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
        ?: firstWindow()?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

private fun firstWindow(): UIWindow? {
    return UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
}
