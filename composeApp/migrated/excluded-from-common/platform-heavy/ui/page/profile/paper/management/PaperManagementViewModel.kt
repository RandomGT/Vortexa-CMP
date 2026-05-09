package com.vortexa.ui.page.profile.paper.management

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.repository.UserRepository
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.page.creator.statistics.DataCenterActivity
import com.vortexa.ui.page.post.create.PostCreateActivity
import com.vortexa.ui.page.post.detail.PostDetailActivity
import com.vortexa.util.ToastUtil
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 稿件管理页 ViewModel。
 * 负责列表加载、筛选及编辑跳转。
 */
class PaperManagementViewModel(
    private val repository: PaperManagementRepository = PaperManagementRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    val paperFilters = listOf("全部", "草稿箱", "发布成功")

    private val _selectedFilter = MutableStateFlow(0)
    val selectedFilter: StateFlow<Int> = _selectedFilter.asStateFlow()

    private val _paperList = MutableStateFlow<List<PaperItemData>>(emptyList())
    val paperList: StateFlow<List<PaperItemData>> = _paperList.asStateFlow()

    private val _pageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    val pageStatus: StateFlow<PageStatus> = _pageStatus.asStateFlow()

    private val _deletingPostId = MutableStateFlow(0L)
    /** 正在删除的帖子 id，0 表示无 */
    val deletingPostId: StateFlow<Long> = _deletingPostId.asStateFlow()

    private val _deletePostFinished = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    /** 删除成功并已更新列表后发出，用于关闭确认弹窗 */
    val deletePostFinished: SharedFlow<Unit> = _deletePostFinished.asSharedFlow()

    init {
        loadPosts()
    }

    /**
     * 加载稿件列表。
     * 按当前选中的 status 筛选。
     *
     * @param silent 为 true 时不进入全屏 Loading，失败时保留当前列表（用于从详情/编辑返回后的刷新）。
     */
    fun loadPosts(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _pageStatus.value = PageStatus.Loading
            }
            val statusIndex = _selectedFilter.value
            val status = if (statusIndex == 0) null else statusIndex
            Log.d(TAG, "loadPosts: silent=$silent, statusIndex=$statusIndex, status=$status")
            repository.getPosts(
                pageNum = 1,
                pageSize = 20,
                status = status
            )
                .onSuccess { response ->
                    val items = response.list.map { repository.mapToPaperItemData(it) }
                    _paperList.value = items
                    _pageStatus.value = if (items.isEmpty()) PageStatus.Empty else PageStatus.Success
                    Log.d(TAG, "loadPosts: success, size=${items.size}, total=${response.total}")
                }
                .onFailure {
                    if (!silent) {
                        _pageStatus.value = PageStatus.Fail
                        _paperList.value = emptyList()
                    }
                    Log.e(TAG, "loadPosts: failed", it)
                }
        }
    }

    /**
     * 筛选点击，切换 status 并重新加载。
     */
    fun onFilterClick(index: Int) {
        if (_selectedFilter.value == index) return
        _selectedFilter.value = index
        loadPosts()
    }

    /**
     * 点击稿件内容区域进入帖子详情（只读加载，不带编辑入参）。
     */
    fun onOpenPostDetail(context: Context, item: PaperItemData) {
        if (item.postId <= 0L) return
        PostDetailActivity.start(context, item.postId.toString())
    }

    /** 打开创作者数据中心。 */
    fun onOpenDataCenter(context: Context) {
        context.startActivity(Intent(context, DataCenterActivity::class))
    }

    /**
     * 点击编辑：进入发帖页 [PostCreateActivity] 编辑态（标题、正文、媒体由列表数据预填；完整正文可保存后再由接口刷新列表）。
     */
    fun onEditClick(item: PaperItemData, context: Context) {
        if (item.postId <= 0L) return
        Log.i(
            TAG,
            "onEditClick: postId=${item.postId}, titleLen=${item.title.length}, " +
                "contentLen=${item.content.length}, images=${item.imageResources.size}, videos=${item.videoResources.size}"
        )
        PostCreateActivity.startForEdit(
            context = context,
            postId = item.postId.toString(),
            title = item.title,
            content = item.content,
            board = item.board,
            imageResources = item.imageResources,
            videoResources = item.videoResources
        )
    }

    /** 删除帖子（DELETE /v/api/user/posts/{postId}），成功后从列表移除并 [deletePostFinished] 通知 UI。 */
    fun deletePost(postId: Long) {
        if (postId <= 0L) return
        viewModelScope.launch {
            _deletingPostId.value = postId
            try {
                userRepository.deletePost(postId)
                    .onSuccess { data ->
                        val msg = data.msg?.takeIf { it.isNotBlank() } ?: "删除成功"
                        ToastUtil.show(msg)
                        val next = _paperList.value.filter { it.postId != postId }
                        _paperList.value = next
                        if (next.isEmpty()) {
                            _pageStatus.value = PageStatus.Empty
                        }
                        _deletePostFinished.emit(Unit)
                        Log.d(TAG, "deletePost: success postId=$postId")
                    }
                    .onFailure { e ->
                        ToastUtil.show(e.message ?: "删除失败")
                        Log.e(TAG, "deletePost: failed postId=$postId", e)
                    }
            } finally {
                _deletingPostId.value = 0L
            }
        }
    }

    companion object {
        private const val TAG = "PaperManagementViewModel"
    }
}