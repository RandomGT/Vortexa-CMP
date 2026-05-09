package com.vortexa.ui.page.profile.other

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vortexa.model.Certification
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.page.creator.CreatorCenterHeaderTagItem
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.bg_other_profile
import vortexa.composeapp.generated.resources.icon_back
import vortexa.composeapp.generated.resources.icon_cert
import vortexa.composeapp.generated.resources.profile_default

private val certificationTagColors = listOf(
    Color(0xFF8DD3FF),
    Color(0xFFFFCFA3),
    Color(0xFFAEECC2),
    Color(0xFFFFC6D9),
    Color(0xFFD6C4FF)
)

/**
 * 他人主页头部（Figma 415-41955，与创作者中心头部 504-52175 同一套布局语言）。
 */
@Composable
fun OtherUserProfileHeader(
    onBackClick: () -> Unit,
    avatarUrl: String?,
    nickname: String,
    isVerified: Boolean,
    tags: List<CreatorCenterHeaderTagItem>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        Image(
            painter = painterResource(Res.drawable.bg_other_profile),
            contentScale = ContentScale.FillBounds,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Icon(
            painter = painterResource(Res.drawable.icon_back),
            contentDescription = "返回",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(18.dp)
                .size(24.dp)
                .click(onClickListener = onBackClick)
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarImage(
                avatarUrl = avatarUrl?.takeIf { it.isNotBlank() },
                contentDescription = nickname,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape),
                defaultResId = Res.drawable.profile_default
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nickname.ifBlank { "用户" },
                    style = FontMedium(fontSize = 18, color = Colors.black_101828)
                )
                if (isVerified) {
                    Icon(
                        painter = painterResource(Res.drawable.icon_cert),
                        contentDescription = "已认证",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(18.dp)
                    )
                }
            }
            if (tags.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth()
                        .clipToBounds(),
                    contentPadding = PaddingValues(horizontal = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(tags) { _, item ->
                        Text(
                            text = item.text,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            style = FontRegular(fontSize = 12, color = Color.White),
                            modifier = Modifier
                                .background(
                                    color = item.backgroundColor,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

internal fun certificationsToHeaderTags(
    certifications: List<Certification>?
): List<CreatorCenterHeaderTagItem> {
    return certifications.orEmpty()
        .mapNotNull { it.name.takeIf(String::isNotBlank) }
        .mapIndexed { index, name ->
            CreatorCenterHeaderTagItem(
                text = name,
                backgroundColor = certificationTagColors[index % certificationTagColors.size]
            )
        }
}

@Composable
private fun OtherUserProfileHeaderPreview() {
    OtherUserProfileHeader(
        onBackClick = {},
        avatarUrl = null,
        nickname = "Capper",
        isVerified = true,
        tags = listOf(
            CreatorCenterHeaderTagItem("导师认证", certificationTagColors[0]),
            CreatorCenterHeaderTagItem("大V", certificationTagColors[1])
        )
    )
}
