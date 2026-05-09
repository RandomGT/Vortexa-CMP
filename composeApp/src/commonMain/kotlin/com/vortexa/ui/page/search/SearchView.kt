package com.vortexa.ui.page.search

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.ui.page.search.history.HotTopicSection
import com.vortexa.ui.page.search.history.SearchHistorySection
import com.vortexa.ui.page.search.history.SearchTutorRecommendSection
import com.vortexa.ui.page.search.result.SearchResultView

/**
 * 搜索页：顶部搜索条 + 下方根据状态展示搜索历史/热搜话题/导师推荐或搜索结果。
 * - SearchBar 有内容时点击搜索：展示 SearchResultView。
 * - 删除全部内容后：展示搜索历史、热搜话题、导师推荐三个模块。
 *
 * @param onBack 点击返回时回调，通常 finish Activity
 */
@Composable
fun SearchView(onBack: () -> Unit = {}) {
    val context = Context()
    val repo = remember { SearchHistoryRepository(context.applicationContext) }
    val viewModel: SearchViewModel = vortexaViewModel { SearchViewModel() }
    var history by remember { mutableStateOf(repo.getHistory()) }
    var query by remember { mutableStateOf("") }
    var searchKeyword by remember { mutableStateOf<String?>(null) }

    val hotTopics by viewModel.hotTopics.collectAsState()
    val tutorList by viewModel.tutorList.collectAsState()

    // 有提交过的关键词且输入框未清空时展示结果；删除全部内容后展示历史等模块
    val showResult = searchKeyword != null && query.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .background(Color.White)
    ) {
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onBack = onBack,
            onSubmit = { keyword ->
                if (keyword.isNotBlank()) {
                    repo.add(keyword)
                    history = repo.getHistory()
                    searchKeyword = keyword
                }
            }
        )
        if (showResult) {
            SearchResultView(keyword = searchKeyword ?: "")
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                SearchHistorySection(
                    history = history,
                    onHistoryItemClick = { keyword ->
                        if (keyword.isNotBlank()) {
                            repo.add(keyword)
                            history = repo.getHistory()
                            query = keyword
                            searchKeyword = keyword
                        }
                    },
                    onClearHistory = {
                        repo.clear()
                        history = repo.getHistory()
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                HotTopicSection(
                    topics = hotTopics,
                    onTopicClick = { keyword ->
                        if (keyword.isNotBlank()) {
                            repo.add(keyword)
                            history = repo.getHistory()
                            query = keyword
                            searchKeyword = keyword
                        }
                    }
                )
//                Spacer(modifier = Modifier.height(24.dp))
//                SearchTutorRecommendSection(tutors = tutorList)
//                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}



@Composable
fun SearchPreview() {
    SearchView()
}
