package com.vortexa.ui.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vortexa.navigation.AppRoute
import com.vortexa.navigation.ProfileSubPageKind
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.bg_login
import vortexa.composeapp.generated.resources.default_pic
import vortexa.composeapp.generated.resources.profile_default
import vortexa.composeapp.generated.resources.splash_bg

@Composable
fun SplashShell(onSplashFinish: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(600)
        onSplashFinish()
    }
    ShellScaffold(title = "Vortexa") {
        Image(
            painter = painterResource(Res.drawable.splash_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(220.dp),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.height(24.dp))
        Text("正在进入 iOS Compose 版本", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun HomeShell(selectedTab: Int, onNavigate: (AppRoute) -> Unit) {
    val tabs = listOf("首页", "关注", "消息", "课堂", "我的")
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeContentPadding()) {
            Column(Modifier.weight(1f).fillMaxWidth().padding(20.dp)) {
                Text("Vortexa", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("核心页面已接入 Navigation3，原 Android Compose 页面源码已搬入迁移候选区。")
                Spacer(Modifier.height(20.dp))
                when (selectedTab) {
                    0 -> HomeLanding(onNavigate)
                    1 -> CandidateList("关注动态", listOf("关注用户动态", "推荐帖子流", "互动入口"), onNavigate)
                    2 -> CandidateList("消息中心", listOf("系统通知", "课堂消息", "会话列表"), onNavigate)
                    3 -> CandidateList("课堂", listOf("老师列表", "预约课程", "我的课堂"), onNavigate)
                    else -> ProfileLanding(onNavigate)
                }
            }
            NavigationBar {
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { onNavigate(AppRoute.Home(index)) },
                        icon = { Text(label.take(1)) },
                        label = { Text(label) },
                    )
                }
            }
        }
    }
}

@Composable
fun LoginShell(onBack: () -> Unit, onNavigate: (AppRoute) -> Unit) {
    ShellScaffold(title = "登录", onBack = onBack) {
        Image(
            painter = painterResource(Res.drawable.bg_login),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(180.dp),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.height(18.dp))
        Text("登录表单将在下一轮从 LoginScreen 迁移闭包接入。")
        Button(onClick = { onNavigate(AppRoute.Register) }) { Text("注册") }
        Button(onClick = { onNavigate(AppRoute.ForgetPassword) }) { Text("忘记密码") }
    }
}

@Composable
fun RegisterShell(onBack: () -> Unit) {
    ShellScaffold(title = "注册", onBack = onBack) {
        Text("注册页面壳已接入，等待 RegisterPage 跨平台化替换。")
    }
}

@Composable
fun ForgetPasswordShell(onBack: () -> Unit) {
    ShellScaffold(title = "忘记密码", onBack = onBack) {
        Text("忘记密码页面壳已接入，等待 ForgetView 跨平台化替换。")
    }
}

@Composable
fun SearchShell(onBack: () -> Unit, onNavigate: (AppRoute) -> Unit) {
    ShellScaffold(title = "搜索", onBack = onBack) {
        listOf("Kotlin", "Compose", "Vortexa").forEach { keyword ->
            Row(
                Modifier.fillMaxWidth().clickable { onNavigate(AppRoute.SearchResult(keyword)) }.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(keyword)
                Text("查看")
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun SearchResultShell(keyword: String, onBack: () -> Unit) {
    ShellScaffold(title = "搜索结果", onBack = onBack) {
        Text("keyword = $keyword")
        CandidateRows(listOf("综合", "帖子", "老师", "课程"))
    }
}

@Composable
fun PostDetailShell(
    postId: String,
    openReplyComposer: Boolean,
    onBack: () -> Unit,
    onNavigate: (AppRoute) -> Unit,
) {
    ShellScaffold(title = "帖子详情", onBack = onBack) {
        Image(
            painter = painterResource(Res.drawable.default_pic),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(180.dp),
            contentScale = ContentScale.Crop,
        )
        Text("postId = $postId")
        Text("openReplyComposer = $openReplyComposer")
        Button(onClick = { onNavigate(AppRoute.ImagePreview(listOf("sample"), 0)) }) {
            Text("图片预览")
        }
    }
}

@Composable
fun PostCreateShell(
    editPostId: String?,
    title: String,
    content: String,
    imageResources: List<String>,
    videoResources: List<String>,
    onBack: () -> Unit,
) {
    ShellScaffold(title = if (editPostId == null) "发布帖子" else "编辑帖子", onBack = onBack) {
        Text("title = $title")
        Text("content length = ${content.length}")
        Text("images = ${imageResources.size}, videos = ${videoResources.size}")
        Text("媒体选择和上传会在平台边界接入后启用。")
    }
}

@Composable
fun ImagePreviewShell(urls: List<String>, initialIndex: Int, onBack: () -> Unit) {
    ShellScaffold(title = "图片预览", onBack = onBack) {
        Image(
            painter = painterResource(Res.drawable.default_pic),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(260.dp),
            contentScale = ContentScale.Crop,
        )
        Text("urls = ${urls.size}, initialIndex = $initialIndex")
    }
}

@Composable
fun ProfileSubPageShell(kind: ProfileSubPageKind, onBack: () -> Unit, onNavigate: (AppRoute) -> Unit) {
    ShellScaffold(title = kind.name, onBack = onBack) {
        CandidateRows(
            when (kind) {
                ProfileSubPageKind.Collection -> listOf("收藏帖子", "收藏课程")
                ProfileSubPageKind.History -> listOf("浏览记录", "评论记录")
                ProfileSubPageKind.Interaction -> listOf("点赞", "评论", "回复")
            },
        )
        Button(onClick = { onNavigate(AppRoute.PostDetail("1")) }) { Text("打开示例帖子") }
    }
}

@Composable
private fun HomeLanding(onNavigate: (AppRoute) -> Unit) {
    CandidateRows(listOf("推荐帖子", "课程推荐", "老师推荐"))
    Spacer(Modifier.height(12.dp))
    Button(onClick = { onNavigate(AppRoute.Search) }) { Text("搜索") }
    Button(onClick = { onNavigate(AppRoute.PostDetail("1")) }) { Text("帖子详情") }
    Button(onClick = { onNavigate(AppRoute.PostCreate()) }) { Text("发布帖子") }
}

@Composable
private fun ProfileLanding(onNavigate: (AppRoute) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(Res.drawable.profile_default),
            contentDescription = null,
            modifier = Modifier.size(56.dp),
        )
        Column(Modifier.padding(start = 12.dp)) {
            Text("个人中心", fontWeight = FontWeight.Bold)
            Text("ProfileView 待从迁移候选区接入")
        }
    }
    Spacer(Modifier.height(18.dp))
    Button(onClick = { onNavigate(AppRoute.Login) }) { Text("登录") }
    Button(onClick = { onNavigate(AppRoute.ProfileSubPage(ProfileSubPageKind.Collection)) }) { Text("我的收藏") }
    Button(onClick = { onNavigate(AppRoute.ProfileSubPage(ProfileSubPageKind.History)) }) { Text("浏览记录") }
    Button(onClick = { onNavigate(AppRoute.ProfileSubPage(ProfileSubPageKind.Interaction)) }) { Text("互动管理") }
}

@Composable
private fun CandidateList(title: String, rows: List<String>, onNavigate: (AppRoute) -> Unit) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    CandidateRows(rows)
    Spacer(Modifier.height(12.dp))
    Button(onClick = { onNavigate(AppRoute.Search) }) { Text("搜索") }
}

@Composable
private fun CandidateRows(rows: List<String>) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(rows) { row ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(14.dp),
            ) {
                Text(row)
            }
        }
    }
}

@Composable
private fun ShellScaffold(title: String, onBack: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().safeContentPadding().padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) {
                        Text("返回", modifier = Modifier.clickable(onClick = onBack).padding(end = 16.dp))
                    }
                    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(20.dp))
                content()
            }
        }
    }
}
