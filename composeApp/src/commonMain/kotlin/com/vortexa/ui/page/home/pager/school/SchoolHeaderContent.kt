package com.vortexa.ui.page.home.pager.school

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.Banner
import com.vortexa.ui.page.search.SearchBar
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.banner_sample

/**
 * 校园页可折叠头部：包含搜索栏与 Banner，整体随列表上滑收起。
 *
 * @param query 搜索框文案。
 * @param onQueryChange 搜索框输入变化回调。
 * @param onSubmit 搜索提交回调。
 * @return 无返回值。
 */
@Composable
fun SchoolHeaderContent(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
//        SearchBar(
//            query = query,
//            onQueryChange = onQueryChange,
//            onBack = { }
//            onSubmit = onSubmit
//        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .height(177.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            val bannerData = listOf("Banner 1", "Banner 2", "Banner 3")
            Banner(data = bannerData, modifier = Modifier.fillMaxSize()) { item ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(Res.drawable.banner_sample),
                        contentDescription = item,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x5C020940))
                    )
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "校园 Banner",
                            style = FontRegular(14, Color.White),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 预览校园页可折叠头部样式。
 *
 * @return 无返回值。
 */
@Composable
private fun SchoolHeaderContentPreview() {
    BaseTheme {
        SchoolHeaderContent(
            query = "",
            onQueryChange = {},
            onSubmit = {}
        )
    }
}
