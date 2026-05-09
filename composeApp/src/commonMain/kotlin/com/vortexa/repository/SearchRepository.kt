package com.vortexa.repository

import com.vortexa.model.Post

class SearchRepository {
    suspend fun getSearchResult(keyword: String, type: String = "general", pageNum: Int = 1, pageSize: Int = 4): Result<List<Post>> =
        Result.success(emptyList())
}

