package com.vortexa.ui.page.creator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.model.CreatorTask
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click

/**
 * 创作者任务激励区块（Figma 504-55094）：标题 + 可滚动任务列表。
 * 数据来自 GET /v/api/user/creator/tasks。
 *
 * @param tasks 任务列表，空时展示空状态
 * @param onTaskAction 任务按钮点击（领取奖励 / 去发布）
 * @param modifier 可选修饰符
 */
@Composable
fun CreatorCenterTaskSection(
    tasks: List<CreatorTask> = emptyList(),
    onTaskAction: (CreatorTask) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Colors.gray_F8F9FA)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "创作者任务激励",
            style = FontMedium(fontSize = 14, color = Colors.black_101828)
        )
        Column(
            modifier = Modifier
                .padding(top = 16.dp)
                .heightIn(max = 280.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (tasks.isEmpty()) {
                Text(
                    text = "暂无任务",
                    style = FontRegular(fontSize = 13, color = Colors.gray_6A7282)
                )
            } else {
                tasks.forEach { task ->
                    CreatorCenterTaskRow(
                        task = task,
                        onAction = { onTaskAction(task) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CreatorCenterTaskRow(
    task: CreatorTask,
    onAction: () -> Unit
) {
    val progressText = "${task.progress}/${task.target}"
    val (bg, textColor) = if (task.canClaim) {
        Colors.black_101828 to androidx.compose.ui.graphics.Color.White
    } else {
        androidx.compose.ui.graphics.Color(0x14277DFF) to Colors.blue_277DFF
    }
    val buttonText = if (task.canClaim) "领取奖励" else "去发布"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = task.title,
                style = FontRegular(fontSize = 13, color = Colors.black_101828)
            )
            Text(
                text = progressText,
                style = FontRegular(fontSize = 12, color = Colors.gray_6A7282),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Text(
            text = buttonText,
            style = FontMedium(fontSize = 12, color = textColor),
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(bg)
                .padding(horizontal = 12.dp, vertical = 3.dp)
                .click(onClickListener = onAction)
        )
    }
}

@Composable
@Preview
private fun CreatorCenterTaskSectionPreview() {
    CreatorCenterTaskSection(
        tasks = listOf(
            CreatorTask(1, "发布首篇内容", "成功发布 1 篇内容", "completed", 1, 1, "创作积分", 100, true, null, null),
            CreatorTask(2, "7 日连续创作", "连续 7 天发布内容", "unfinished", 4, 7, "创作积分", 300, false, null, null),
            CreatorTask(3, "单篇内容浏览破 1,000", "任意一篇内容浏览量达到 1000", "claimed", 1000, 1000, "现金券", 10, false, null, null),
            CreatorTask(4, "发布1篇帖子并获得200浏览", null, "completed", 200, 200, "创作积分", 50, true, null, null),
            CreatorTask(5, "收获5个点赞", null, "unfinished", 4, 5, "创作积分", 30, false, null, null)
        )
    )
}
