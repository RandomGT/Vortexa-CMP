package com.vortexa.api

import com.vortexa.lib_net.model.ApiResponse
import com.vortexa.model.SearchResultRequest
import com.vortexa.model.SearchResultResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 搜索结果相关接口。
 */
interface SearchApi {

    /**
     * 获取搜索结果（ES），支持综合/帖文/用户/导师/工具箱/课程筛选。
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 4
     */
    @POST("/v/api/search/result")
    suspend fun getSearchResult(
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 4,
        @Body request: SearchResultRequest
    ): ApiResponse<SearchResultResponse>
}
