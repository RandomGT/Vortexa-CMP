package com.vortexa.repository

import android.util.Log
import com.vortexa.api.UserApi
import com.vortexa.lib_net.client.RetrofitClient
import com.vortexa.lib_net.extension.getDataOrThrow
import com.vortexa.model.InteractionRequest
import com.vortexa.model.InteractionResponse

/**
 * 互动管理数据仓库。
 * 负责 /v/api/user/interactions 接口调用与数据转换。
 *
 * @author LuXin
 */
class InteractionRepository {

    private val api: UserApi by lazy {
        RetrofitClient.createService()
    }

    /**
     * 获取互动列表。
     *
     * @param actorType 互动对象：0=所有人，1=我的关注
     * @param actionType 互动类型：0=点赞，1=回复
     * @param direction 互动方向：0=全部，1=我发起的，2=被互动的
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return Result<InteractionResponse>
     */
    suspend fun getInteractions(
        actorType: Int,
        actionType: Int,
        direction: Int,
        pageNum: Int = 1,
        pageSize: Int = 20
    ): Result<InteractionResponse> = runCatching {
        Log.d(TAG, "getInteractions: actorType=$actorType, actionType=$actionType, direction=$direction")
        val request = InteractionRequest(
            actorType = actorType,
            actionType = actionType,
            direction = direction
        )
        api.getInteractions(pageNum = pageNum, pageSize = pageSize, request = request).getDataOrThrow()
    }

    companion object {
        private const val TAG = "InteractionRepository"
    }
}
