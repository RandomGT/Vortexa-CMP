package com.vortexa.ui.page.profile.paper.management
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.ui.theme.Colors
import com.vortexa.util.extension.click
import com.vortexa.util.findActivity
import vortexa.composeapp.generated.resources.Res

/**
 * 页面头部组件
 * 显示标题和操作图标（新建文件夹、搜索、菜单）
 */
@Composable
fun PaperManagementHeader() {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(Res.drawable.icon_back),
            contentDescription = "Back",
            modifier = Modifier.size(24.dp)
                .click{
                    context.findActivity().finish()
                }
        )
        Text(
            text = "稿件管理",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Colors.black_101828,
            modifier = Modifier.weight(1f)
        )
    }
}