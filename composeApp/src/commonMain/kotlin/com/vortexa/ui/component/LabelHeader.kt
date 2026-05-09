package com.vortexa.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.ic_arrow_right_gray

@Composable
fun LabelHeader(
    text: AnnotatedString, showMore: Boolean = true,
    modifier: Modifier = Modifier,
    onExploreMoreClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (showMore) {
            // Explore More
            Row(
                modifier = if (onExploreMoreClick != null) {
                    Modifier.click(onClickListener = onExploreMoreClick)
                } else {
                    Modifier
                },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "探索更多",
                    style = FontRegular(11, Colors.gray_B1B8C6)
                )
                Image(
                    painter = painterResource(Res.drawable.ic_arrow_right_gray),
                    contentDescription = "Explore More",
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun DefaultColorLabel(
    first: String,
    last: String,
    showMore: Boolean = false,
    modifier: Modifier = Modifier
) {
    LabelHeader(buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = Colors.blue_74B9FD
            )
        ) {
            append(first)
        }
        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = Colors.blue_277DFF
            )
        ) {
            append(last)
        }
    }, showMore = showMore, modifier)
}