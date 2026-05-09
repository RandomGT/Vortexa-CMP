package com.vortexa.ui.page.home.pager.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.bg_profile
import vortexa.composeapp.generated.resources.icon_logout

@Composable
fun ProfileBG(
    onLogoutClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(182.dp)
    ) {
        Image(
            modifier = Modifier
                .height(182.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.FillBounds,
            painter = painterResource(Res.drawable.bg_profile),
            contentDescription = ""
        )

        Image(
            painter = painterResource(Res.drawable.icon_logout),
            contentDescription = "",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 28.dp, end = 16.dp)
                .size(25.dp)
                .click(onLogoutClick)
        )
    }
}