package com.vortexa.ui.page.post.create

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.mediaUrlListToJsonOrNull
import com.vortexa.repository.HomeRepository
import com.vortexa.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/** 发布贴文板块（发布到 下拉顺序；与发帖接口 body.module 一致） */
val POST_CREATE_MODULES = listOf("杂谈", "交易经验", "玩法")

/** 发布页默认可选话题（接口失败时兜底） */
val POST_CREATE_TOPICS = listOf("量化交易入门", "短线策略", "DeFi玩法", "空投指南", "趋势跟踪")

/** 发帖校验：标题为空（先于正文校验） */
const val POST_CREATE_ERR_TITLE_EMPTY = "请输入标题"

/** 发帖校验：正文为空 */
const val POST_CREATE_ERR_BODY_EMPTY = "请输入正文（必填）"

/**
 * 发布贴文页 ViewModel。
 * 负责标题、正文、板块、话题插入；新建走 insert，编辑走 /v/api/user/posts/update/{postId}。
 */
class PostCreateViewModel(
    private val homeRepository: HomeRepository = HomeRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _title = MutableStateFlow("")
    /** 标题，最长 30 字 */
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    /** 正文内容 */
    val content: StateFlow<String> = _content.asStateFlow()

    private val _topicSuggestions = MutableStateFlow(POST_CREATE_TOPICS)
    /** 话题候选列表，优先取热搜话题，失败时回退默认值 */
    val topicSuggestions: StateFlow<List<String>> = _topicSuggestions.asStateFlow()

    private val _selectedModuleIndex = MutableStateFlow(0)
    /** 选中的板块索引 0~3 */
    val selectedModuleIndex: StateFlow<Int> = _selectedModuleIndex.asStateFlow()

    private val _isPublishing = MutableStateFlow(false)
    /** 是否正在发布 */
    val isPublishing: StateFlow<Boolean> = _isPublishing.asStateFlow()

    private val _publishError = MutableStateFlow<String?>(null)
    /** 发布失败时的错误信息 */
    val publishError: StateFlow<String?> = _publishError.asStateFlow()

    private val _publishSuccess = MutableStateFlow(false)
    /** 发布成功，用于关闭页面 */
    val publishSuccess: StateFlow<Boolean> = _publishSuccess.asStateFlow()

    private val _editingPostId = MutableStateFlow<String?>(null)

    init {
        loadTopicSuggestions()
    }

    /**
     * 从详情「编辑」进入时预填标题、正文、板块；发布见 [publish] 内更新接口 TODO。
     */
    fun applyEditDraft(postId: String, title: String, content: String, board: String? = null) {
        _editingPostId.value = postId
        _title.value = title.take(30)
        _content.value = content
        val idx = board?.let { b -> POST_CREATE_MODULES.indexOf(b).takeIf { it >= 0 } } ?: 0
        _selectedModuleIndex.value = idx
        Log.i(TAG, "applyEditDraft: postId=$postId, moduleIndex=$idx")
    }

    /** 更新标题，超过 30 字截断 */
    fun updateTitle(value: String) {
        _title.value = value.take(30)
    }

    /** 更新正文 */
    fun updateContent(value: String) {
        _content.value = value
    }

    /**
     * 加载发布页话题候选。
     * 优先复用搜索页热搜话题接口，失败时保留本地默认话题。
     */
    private fun loadTopicSuggestions() {
        viewModelScope.launch {
//            homeRepository.getSearchSuggest()
//                .onSuccess { topics ->
//                    if (topics.isNotEmpty()) {
//                        _topicSuggestions.value = topics
//                        Log.d(TAG, "loadTopicSuggestions: loaded ${topics.size} topics")
//                    }
//                }
//                .onFailure { error ->
//                    Log.w(TAG, "loadTopicSuggestions: fallback to default topics", error)
//                }
        }
    }

    /** 选择板块 */
    fun selectModule(index: Int) {
        if (index in 0..<POST_CREATE_MODULES.size) {
            _selectedModuleIndex.value = index
        }
    }

    /**
     * 点击发帖/保存按钮，校验后调用接口。
     * @param selectedMedia 当前选中媒体（远程 URL 与本地 Uri；图片本地会先上传）
     */
    fun publish(selectedMedia: List<PostCreateSelectedMedia> = emptyList()) {
        val t = _title.value.trim()
        val c = _content.value.trim()
        if (t.isEmpty()) {
            _publishError.value = POST_CREATE_ERR_TITLE_EMPTY
            return
        }
        if (c.isEmpty()) {
            _publishError.value = POST_CREATE_ERR_BODY_EMPTY
            return
        }
        val module = POST_CREATE_MODULES.getOrNull(_selectedModuleIndex.value)
            ?: POST_CREATE_MODULES.first()
        val editingId = _editingPostId.value
        viewModelScope.launch {
            _isPublishing.value = true
            _publishError.value = null
            try {
                val mediaUrls = resolveMediaUrlsForPublish(selectedMedia)
                if (editingId != null) {
                    val postIdLong = editingId.toLongOrNull()
                    if (postIdLong == null) {
                        _publishError.value = "帖子 ID 无效"
                        Log.e(TAG, "updatePost: invalid postId=$editingId")
                        return@launch
                    }
                    Log.i(TAG, "updatePost: postId=$postIdLong, module=$module, mediaCount=${mediaUrls.size}")
                    userRepository.updatePost(
                        postId = postIdLong,
                        module = module,
                        title = t,
                        content = c,
                        mediaListJson = mediaUrlListToJsonOrNull(mediaUrls)
                    )
                        .onSuccess {
                            Log.i(TAG, "updatePost: success")
                            _publishSuccess.value = true
                        }
                        .onFailure { e ->
                            _publishError.value = e.message ?: "保存失败"
                            Log.e(TAG, "updatePost: failed", e)
                        }
                } else {
                    Log.i(TAG, "publish: start, title=$t, module=$module, mediaCount=${mediaUrls.size}")
                    homeRepository.createPost(
                        title = t,
                        content = c,
                        module = module,
                        mediaList = mediaUrls.ifEmpty { null }
                    )
                        .onSuccess {
                            Log.i(TAG, "publish: success")
                            _publishSuccess.value = true
                        }
                        .onFailure { e ->
                            _publishError.value = e.message ?: "发布失败"
                            Log.e(TAG, "publish: failed", e)
                        }
                }
            } catch (e: Throwable) {
                _publishError.value = e.message
                    ?: if (editingId != null) "保存失败" else "发布失败"
                Log.e(TAG, "publish: unexpected error", e)
            } finally {
                _isPublishing.value = false
                Log.d(TAG, "publish: finished, isPublishing cleared")
            }
        }
    }

    /**
     * 将发布页选中的媒体解析为 URL 列表：已存在的 http(s) 直传；本地图片先上传；本地视频暂无上传能力则跳过。
     */
    private suspend fun resolveMediaUrlsForPublish(
        items: List<PostCreateSelectedMedia>
    ): List<String> {
        val out = ArrayList<String>(items.size)
        for (item in items) {
            val s = item.uri.toString().trim()
            if (s.isEmpty()) continue
            val scheme = item.uri.scheme?.lowercase(Locale.ROOT)
            when {
                scheme == "http" || scheme == "https" -> out.add(s)
                item.type == PostCreateMediaType.Image -> {
                    homeRepository.uploadPostImage(item.uri)
                        .onSuccess { url -> out.add(url) }
                        .onFailure { throw it }
                }
                else -> Log.w(TAG, "resolveMediaUrlsForPublish: skip non-remote video uri=$s")
            }
        }
        return out
    }

    /** 清除发布错误（用户可再次尝试） */
    fun clearPublishError() {
        _publishError.value = null
    }

    /**
     * 在正文指定位置插入完整话题 token。
     * 序列化为 `#话题名`（仅前导 `#`），并按上下文自动补空格。
     *
     * @param text 当前正文
     * @param selectionStart 当前选区起点
     * @param selectionEnd 当前选区终点
     * @param topic 待插入的话题文案
     * @return Pair<新文本, 新光标位置>
     */
    fun insertTopicToken(
        text: String,
        selectionStart: Int,
        selectionEnd: Int = selectionStart,
        topic: String
    ): Pair<String, Int> {
        val normalizedTopic = topic.trim().trim('#')
        if (normalizedTopic.isBlank()) {
            Log.w(TAG, "insertTopicToken: blank topic ignored")
            return text to selectionEnd.coerceIn(0, text.length)
        }

        val start = selectionStart.coerceIn(0, text.length)
        val end = selectionEnd.coerceIn(start, text.length)
        val replacement = buildTopicReplacement(
            text = text,
            start = start,
            end = end,
            topicToken = "#$normalizedTopic"
        )
        val newText = text.replaceRange(start, end, replacement)
        val newCursor = start + replacement.length
        Log.d(TAG, "insertTopicToken: topic=$normalizedTopic, start=$start, end=$end")
        return newText to newCursor
    }

    /**
     * 在正文指定位置插入自定义话题模板。
     * 仅插入前缀 `#`，正文序列化为 `#话题名`（无结尾 `#`）；光标置于 `#` 后便于继续输入。
     *
     * @param text 当前正文
     * @param selectionStart 当前选区起点
     * @param selectionEnd 当前选区终点
     * @return Pair<新文本, 新光标位置>
     */
    fun insertCustomTopicTemplate(
        text: String,
        selectionStart: Int,
        selectionEnd: Int = selectionStart
    ): Pair<String, Int> {
        val start = selectionStart.coerceIn(0, text.length)
        val end = selectionEnd.coerceIn(start, text.length)
        val needTrailingSpace = end >= text.length || !text[end].isWhitespace()
        val replacement = buildTopicReplacement(
            text = text,
            start = start,
            end = end,
            topicToken = "#"
        )
        val newText = text.replaceRange(start, end, replacement)
        val newCursor = start + replacement.length - (if (needTrailingSpace) 1 else 0)
        Log.d(TAG, "insertCustomTopicTemplate: start=$start, end=$end")
        return newText to newCursor
    }

    /**
     * 根据插入位置补齐前后空格。
     * 这样既能避免与前后文字粘连，也能让用户插入后直接继续输入。
     *
     * @param text 当前正文
     * @param start 替换起点
     * @param end 替换终点
     * @param topicToken 已格式化好的话题 token，例如 `#量化交易`
     * @return 可直接 replaceRange 的插入文本
     */
    private fun buildTopicReplacement(
        text: String,
        start: Int,
        end: Int,
        topicToken: String
    ): String {
        val needLeadingSpace = start > 0 && !text[start - 1].isWhitespace()
        val needTrailingSpace = end >= text.length || !text[end].isWhitespace()
        return buildString {
            if (needLeadingSpace) append(' ')
            append(topicToken)
            if (needTrailingSpace) append(' ')
        }
    }

    private companion object {
        const val TAG = "PostCreateVM"
    }
}
