package com.vortexa.repository

import com.vortexa.model.TeacherListResponse

class C2cRepository {
    suspend fun getTeacherList(
        tags: String? = null,
        minPrice: String? = null,
        maxPrice: String? = null,
        pageNum: Int = 1,
        pageSize: Int = 5,
    ): Result<TeacherListResponse> =
        Result.success(TeacherListResponse(pageNum, pageSize, 0, emptyList()))
}

