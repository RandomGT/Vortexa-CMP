package com.vortexa.ui.page.creator

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.page.creator.statistics.DataCenterActivity
import com.vortexa.ui.theme.BaseTheme

class CreatorCenterActivity : BaseActivity() {
    @Composable
    override fun ContentPage() {
        val context = LocalContext.current
        BaseTheme(belowStatusBar = false, aboveNavigationBar = true) {
            CreatorCenterView(
                onBackClick = { finish() },
                onQuickEntryClick = { label ->
                    when (label) {
                        "数据中心" -> {
                            context.startActivity(Intent(context, DataCenterActivity::class.java))
                        }
                        else -> { }
                    }
                }
            )
        }
    }
}