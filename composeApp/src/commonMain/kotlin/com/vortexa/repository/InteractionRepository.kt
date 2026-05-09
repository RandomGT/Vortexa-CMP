package com.vortexa.repository

import com.vortexa.model.InteractionResponse

class InteractionRepository {
    suspend fun getInteractions(
        actorType: Int = 0,
        actionType: Int = 0,
        direction: Int = 0,
        pageNum: Int = 1,
        pageSize: Int = 20,
    ): Result<InteractionResponse> =
        Result.success(InteractionResponse(total = 0, page = pageNum, pageSize = pageSize, list = emptyList()))
}

