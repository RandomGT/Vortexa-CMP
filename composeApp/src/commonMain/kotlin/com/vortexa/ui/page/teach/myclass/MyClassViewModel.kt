package com.vortexa.ui.page.teach.myclass

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortexa.model.ReserveListApiStatus
import com.vortexa.model.ReserveListItem
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.page.teach.helper.TeachingC2cRepository
import com.vortexa.ui.page.teach.myclass.one2one.MyClassOneToOneItemUi
import com.vortexa.ui.page.teach.myclass.one2one.mapMyClassOneToOneReserveStatusToChinese
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 浮窗样式：学员（全部/学习进度） / 导师（全部/已下架/已上架/销量）
 */
enum class SchoolFilterPopupStyle {
    /** 学员样式：全部、学习进度 */
    STUDENT,
    /** 导师样式：全部、已下架、已上架、销量 */
    TUTOR
}

/** 预约列表 type：1-学生端 2-导师端 */
const val RESERVE_LIST_TYPE_STUDENT = 1
const val RESERVE_LIST_TYPE_TEACHER = 2

/**
 * 我的课程页 ViewModel。
 * 管理涡联学院 Filter 右侧菜单按钮触发的浮窗；管理一对一 Tab 预约列表的加载（按筛选项请求接口 `status`）。
 */
class MyClassViewModel : ViewModel() {

    private companion object {
        private const val TAG = "MyClassViewModel"

        /** 一对一筛选下标 → 预约列表接口的 `status` 参数；null 表示全部 */
        private fun statusQueryForOne2OneFilter(index: Int): String? = when (index) {
            1 -> ReserveListApiStatus.TO_ACCEPT
            2 -> ReserveListApiStatus.TO_START
            3 -> ReserveListApiStatus.CANCELED
            4 -> ReserveListApiStatus.COMPLETED
            else -> null
        }
    }

    private val c2cRepository = TeachingC2cRepository()

    private val _showFilterPopup = MutableStateFlow(false)
    /** 是否显示浮窗，由 Flow 暴露供 UI 订阅 */
    val showFilterPopup: StateFlow<Boolean> = _showFilterPopup.asStateFlow()

    private val _filterPopupStyle = MutableStateFlow(SchoolFilterPopupStyle.STUDENT)
    /** 浮窗样式（学员/导师），由 Flow 暴露 */
    val filterPopupStyle: StateFlow<SchoolFilterPopupStyle> = _filterPopupStyle.asStateFlow()

    private val _schoolMenuSelectedIndex = MutableStateFlow(0)
    /** 浮窗内当前选中项下标。学员：0=全部 1=学习进度；导师：0=全部 1=已下架 2=已上架 3=销量 */
    val schoolMenuSelectedIndex: StateFlow<Int> = _schoolMenuSelectedIndex.asStateFlow()

    /** 打开浮窗 */
    fun openFilterPopup() {
        viewModelScope.launch {
            _showFilterPopup.value = true
        }
    }

    /** 关闭浮窗 */
    fun dismissFilterPopup() {
        viewModelScope.launch {
            _showFilterPopup.value = false
        }
    }

    /** 设置浮窗样式（学员 / 导师） */
    fun setFilterPopupStyle(style: SchoolFilterPopupStyle) {
        viewModelScope.launch {
            _filterPopupStyle.value = style
            // 切换样式时重置选中为第一项
            _schoolMenuSelectedIndex.value = 0
        }
    }

    /** 选择浮窗内某一项并关闭浮窗 */
    fun selectSchoolMenuOption(index: Int) {
        viewModelScope.launch {
            _schoolMenuSelectedIndex.value = index
            _showFilterPopup.value = false
        }
    }

    // ——— 一对一预约列表 ———

    private val _one2OnePageStatus = MutableStateFlow<PageStatus>(PageStatus.Loading)
    /** 一对一预约列表页状态，供 PageStatusView 使用 */
    val one2OnePageStatus: StateFlow<PageStatus> = _one2OnePageStatus.asStateFlow()

    private val _one2OneList = MutableStateFlow<List<MyClassOneToOneItemUi>>(emptyList())
    /** 当前筛选后的预约列表（已映射为 UI 模型） */
    val one2OneList: StateFlow<List<MyClassOneToOneItemUi>> = _one2OneList.asStateFlow()

    /** 一对一 Tab 筛选下标，与 [MyClassOneToOneFilterOptions] 下标一致 */
    private val _one2OneFilterIndex = MutableStateFlow(0)
    val one2OneFilterIndex: StateFlow<Int> = _one2OneFilterIndex.asStateFlow()

    /** 最近一次列表请求的 type，切换筛选项时复用 */
    private var lastReserveListType: Int = RESERVE_LIST_TYPE_TEACHER

    /**
     * 加载预约列表；当前筛选项通过接口 `status` 查询参数传递（与 [setOne2OneFilterIndex] 联动）。
     * @param type 1-学生端 2-导师端
     */
    fun loadReserveList(type: Int) {
        lastReserveListType = type
        viewModelScope.launch {
            _one2OnePageStatus.value = PageStatus.Loading
            val status = statusQueryForOne2OneFilter(_one2OneFilterIndex.value)
            c2cRepository.getReserveList(type, status)
                .onSuccess { list ->
                    val uiList = list.map { toOneToOneItemUi(it) }
                    _one2OneList.value = uiList
                    _one2OnePageStatus.value = when {
                        uiList.isEmpty() -> PageStatus.Empty
                        else -> PageStatus.Success
                    }
                    Log.d(TAG, "loadReserveList success, type=$type status=$status size=${list.size}")
                }
                .onFailure { e ->
                    Log.w(TAG, "loadReserveList fail", e)
                    _one2OnePageStatus.value = PageStatus.Fail
                }
        }
    }

    /** 设置一对一筛选下标并重新请求列表 */
    fun setOne2OneFilterIndex(index: Int) {
        _one2OneFilterIndex.value = index
        loadReserveList(lastReserveListType)
    }

    /**
     * 将接口单条数据映射为列表项 UI 模型。
     *
     * @param r 预约列表接口项
     * @return 用于 Compose 列表展示的数据，[status] 为接口枚举经中文映射后的文案
     */
    private fun toOneToOneItemUi(r: ReserveListItem): MyClassOneToOneItemUi {
        val duration = if (r.hour == 1) "1小时" else "${r.hour}小时"
        return MyClassOneToOneItemUi(
            reserveId = r.reserveId,
            status = mapMyClassOneToOneReserveStatusToChinese(r.status),
            startTime = r.courseStartTime,
            bookTime = r.reserveCreateTime,
            studentName = r.studentName.orEmpty(),
            teacherName = r.teacherName,
            teacherId = r.teacherId,
            duration = duration
        )
    }
}
