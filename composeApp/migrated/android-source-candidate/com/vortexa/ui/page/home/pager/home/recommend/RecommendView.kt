package com.vortexa.ui.page.home.pager.home.recommend

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import vortexa.composeapp.generated.resources.Res

import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.component.Banner
import com.vortexa.ui.component.LabelHeader
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular

/**
 * desc : Recommend Tab View
 *
 * @author LuXin
 * @createTime 2026/1/21
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RecommendView(
    viewModel: RecommendViewModel = viewModel(),
    onHotPostsExploreMore: (() -> Unit)? = null,
    onNavigateToSchool: (() -> Unit)? = null
) {
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pageStatus by viewModel.pageStatus.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() }
        ) {
            PostList(header = {
            // Banner Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .padding(horizontal = 18.dp)
                    .height(177.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                val bannerData = listOf(
                    "Banner 1",
                    "Banner 2",
                    "Banner 3",
                    "Banner 4"
                )

                Banner(
                    data = bannerData,
                    modifier = Modifier.fillMaxSize()
                ) { item ->
                    // Banner Image
                    Image(
                        painter = painterResource(Res.drawable.banner_sample),
                        contentDescription = item,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Dark Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x5C020940)) // rgba(2,9,64,0.36)
                    )

                    // Content Overlay (Mocked based on Figma)
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "现已上线",
                            style = FontRegular(14, Color.White),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Web3 Knowledge Base Header
            LabelHeader(
                buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = Colors.blue_74B9FD
                        )
                    ) {
                        append("Web3")
                    }
                    append(" ")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = Colors.blue_277DFF
                        )
                    ) {
                        append("知识库")
                    }
                }, modifier = Modifier
                    .padding(horizontal = 18.dp)
                    .padding(top = 24.dp, bottom = 6.dp)
            )
            // Content Categories
            ContentCategories()
            Column(modifier = Modifier.offset(y = -22.dp)) {
                LabelHeader(
                    buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = Colors.blue_74B9FD
                            )
                        ) {
                            append("热")
                        }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = Colors.blue_277DFF
                            )
                        ) {
                            append("贴")
                        }
                    }, modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .padding(top = 24.dp, bottom = 6.dp),
                    onExploreMoreClick = onHotPostsExploreMore
                )

            }
        },
            onHotPostsExploreMore = onHotPostsExploreMore,
            onNavigateToSchool = onNavigateToSchool,
            footer = {
            LabelHeader(
                buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = Colors.blue_74B9FD
                        )
                    ) {
                        append("热门")
                    }
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = Colors.blue_277DFF
                        )
                    ) {
                        append("导师")
                    }
                }, modifier = Modifier
                    .padding(horizontal = 18.dp)
                    .padding(top = 24.dp, bottom = 6.dp),
                onExploreMoreClick = onNavigateToSchool
            )
        },
            viewModel = viewModel
        )
        }
        PageStatusView(
            status = pageStatus,
            modifier = Modifier.fillMaxSize(),
            onRefresh = { viewModel.refresh(showRefreshing = false) }
        )
    }
}


@Composable
@Preview
fun RecommendViewPreview() {
    RecommendView()
}


