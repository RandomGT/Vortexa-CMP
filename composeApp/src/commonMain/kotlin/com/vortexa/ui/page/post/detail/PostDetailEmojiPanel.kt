package com.vortexa.ui.page.post.detail

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.ui.theme.Colors

/**
 * 渲染帖子详情页的标准 Emoji 选择面板。
 *
 * @param modifier 面板外层修饰符
 * @param onEmojiClick 点击某个 Emoji 时触发，回传被点击的 Emoji 字符
 * @return Unit
 */
@Composable
fun PostDetailEmojiPanel(
    modifier: Modifier = Modifier,
    onEmojiClick: (String) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        modifier = modifier
            .background(Colors.gray_F3F5F7)
            .fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(STANDARD_OPEN_SOURCE_EMOJIS) { emoji ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White, CircleShape)
                    .clickable {
                        Log.d(TAG, "Emoji clicked in panel, value=$emoji")
                        onEmojiClick(emoji)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun PostDetailEmojiPanelPreview() {
    PostDetailEmojiPanel()
}

/**
 * 标准 Unicode Emoji 列表（基于开源 Unicode TR51/CLDR 数据）。
 * 来源示例：
 * - https://github.com/mathiasbynens/unicode-tr51
 * - https://github.com/datasets/emojis
 */
private val STANDARD_OPEN_SOURCE_EMOJIS = listOf(
    "😀", "😁", "😂", "🤣", "😃", "😄", "😅", "😆", "😉", "😊", "😋", "😎",
    "😍", "😘", "🥰", "😗", "😙", "😚", "🙂", "🤗", "🤩", "🤔", "🤨", "😐",
    "😑", "😶", "🙄", "😏", "😣", "😥", "😮", "🤐", "😯", "😪", "😫", "🥱",
    "😴", "😌", "😛", "😜", "😝", "🤤", "😒", "😓", "😔", "😕", "🙃", "🫠",
    "🤑", "😲", "☹️", "🙁", "😖", "😞", "😟", "😤", "😢", "😭", "😦", "😧",
    "😨", "😩", "🤯", "😬", "😮‍💨", "😰", "😱", "🥵", "🥶", "😳", "🤪", "😵",
    "🤠", "🥳", "😇", "🤓", "🧐", "🤥", "🤫", "🤭", "🫢", "🫣", "🫡", "🤝",
    "👍", "👎", "👏", "🙌", "👐", "🤲", "🙏", "💪", "🫶", "👋", "🤟", "👌",
    "🤌", "✌️", "🤞", "🤙", "👈", "👉", "👆", "👇", "☝️", "✍️", "💅", "👀",
    "🧠", "🫀", "🫁", "👂", "👃", "👄", "🫦", "👶", "🧒", "👦", "👧", "🧑",
    "👨", "👩", "🧓", "👴", "👵", "🤶", "🎅", "🧙", "🧚", "🧛", "🧜", "🧝",
    "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮",
    "🐷", "🐸", "🐵", "🦄", "🐔", "🐧", "🐦", "🐤", "🦉", "🦋", "🐢", "🐙",
    "🍎", "🍊", "🍋", "🍉", "🍇", "🍓", "🍒", "🍑", "🍍", "🥭", "🍅", "🥑",
    "🥕", "🌽", "🥦", "🍔", "🍟", "🍕", "🌮", "🍣", "🍜", "🍰", "🍪", "☕",
    "🍺", "🍻", "🍷", "🥂", "⚽", "🏀", "🏈", "⚾", "🎾", "🏐", "🏓", "🏸",
    "🎮", "🎲", "🎯", "🎨", "🎵", "🎤", "🎧", "🎬", "🚗", "🚕", "🚌", "🚑",
    "🚒", "🚓", "✈️", "🚀", "🛸", "🌍", "🌕", "⭐", "☀️", "⛅", "🌧️", "⛈️",
    "❄️", "🔥", "💧", "🌈", "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍",
    "🤎", "💔", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💯", "💢",
    "💥", "💫", "💦", "💨", "🕳️", "💬", "🗯️", "💭", "✅", "❌", "⚠️", "❓"
)

private const val TAG = "PostDetailEmojiPanel"
