package com.vortexa.ui.page.post.detail

/**
 * 帖子详情页底部输入区状态机。
 *
 * - [Collapsed]：未展开，仅显示单行占位
 * - [Keyboard]：键盘输入状态，输入框获焦、弹出键盘，收起表情/媒体面板
 * - [Emoji]：表情输入状态，收起键盘，展开表情面板
 * - [Media]：图片/视频输入状态，收起键盘与表情面板（选图/选视频流程）
 */
sealed class ComposerState {
    /** 未展开 */
    data object Collapsed : ComposerState()

    /** 键盘输入状态：弹键盘，收其他 panel */
    data object Keyboard : ComposerState()

    /** 表情输入状态：收键盘，展开表情 panel */
    data object Emoji : ComposerState()

    /** 图片/视频输入状态：收键盘、收表情 panel */
    data object Media : ComposerState()
}
