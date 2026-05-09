package com.vortexa.ui.page.profile.other

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.vortexa.config.UserConfig
import com.vortexa.ui.base.BaseActivity

/**
 * 他人个人主页。传入用户 ID，进入后请求 `GET /v/api/user/profile/{userId}`。
 *
 * @author LuXin
 */
class OtherUserProfileActivity : BaseActivity() {

    @Composable
    override fun ContentPage() {
        val userId = intent.getLongExtra(EXTRA_USER_ID, INVALID_USER_ID)
        OtherUserProfileView(
            userId = userId,
            onBackClick = { finish() }
        )
    }

    companion object {
        private const val EXTRA_USER_ID = "extra_user_id"
        private const val INVALID_USER_ID = -1L

        /** 打开他人主页 */
        fun start(context: Context, userId: Long) {
            context.startActivity(
                Intent(context, OtherUserProfileActivity::class.java).apply {
                    putExtra(EXTRA_USER_ID, userId)
                }
            )
        }

        /** userId 有效且不是当前登录用户时打开他人主页 */
        fun startIfNotSelf(context: Context, userId: Long) {
            if (userId <= 0L) return
            if (userId == UserConfig.getUserId()) return
            start(context, userId)
        }
    }
}
