package com.vortexa.ui.page.search.result.composite

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vortexa.ui.page.search.result.SearchResultViewModel
import com.vortexa.ui.page.search.result.post.PostPage

@Composable
fun CompositePage(
    viewModel: SearchResultViewModel,
    modifier: Modifier = Modifier
) {
    PostPage(viewModel = viewModel, modifier = modifier)
}
