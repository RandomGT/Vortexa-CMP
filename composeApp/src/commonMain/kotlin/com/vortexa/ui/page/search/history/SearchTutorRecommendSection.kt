package com.vortexa.ui.page.search.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vortexa.model.TeacherItem
import com.vortexa.ui.component.DefaultColorLabel
import com.vortexa.ui.page.home.pager.home.communicate.TutorRecommendHorizontal

/**
 * 导师推荐区域：标题「导师推荐」+ 横向滚动的导师卡片列表。
 * 复用 TutorRecommendHorizontal UI。
 *
 * @param tutors 导师列表
 * @param onReserveClick 预约按钮点击回调；为 null 时由 [TutorRecommendHorizontal] 默认跳转日程预约页
 */
@Composable
fun SearchTutorRecommendSection(
    tutors: List<TeacherItem>,
    onReserveClick: ((Long) -> Unit)? = null
) {
    if (tutors.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
    ) {
        DefaultColorLabel("导师", "推荐", showMore = false, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        TutorRecommendHorizontal(
            items = tutors.take(6),
            onReserveClick = onReserveClick
        )
    }
}

@Composable
private fun SearchTutorRecommendSectionPreview() {
    SearchTutorRecommendSection(
        tutors = listOf(
            TeacherItem(1L, null, "导师 A", listOf("量化"), 36.5f, "4.5"),
            TeacherItem(2L, null, "导师 B", listOf("DeFi"), 38f, "4.9")
        )
    )
}
