package com.vortexa.ui.page.post.detail.reply

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.vortexa.ui.theme.BaseTheme



@Preview(showBackground = true)
@Composable
private fun ReplyItemViewPreview() {
    BaseTheme {
        ReplyItemView(
            reply = Reply(
                id = "r1",
                authorName = "铅大家将有几个瞬间",
                replyToName = "张三",
                content = "我一般 3-5 倍，不敢开太高",
                likeCount = 5,
                time = "8 分钟前"
            ),
            rootCommentId = "c1"
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun CommentListViewPreview() {
//    BaseTheme {
//        CommentListView()
//    }
//}
