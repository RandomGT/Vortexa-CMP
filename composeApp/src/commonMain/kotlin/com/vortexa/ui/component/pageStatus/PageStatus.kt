package com.vortexa.ui.component.pageStatus

/**
 * 页面请求状态枚举
 *
 * @property Loading 加载中
 * @property Success 成功（隐藏状态视图）
 * @property Fail 请求失败
 * @property Empty 数据为空
 *
 * @author LuXin
 * @createTime 2026/2/5
 */
enum class PageStatus {
    /** 加载中，展示 loading 动画 */
    Loading,

    /** 成功，隐藏状态视图 */
    Success,

    /** 请求失败，展示失败图标和文案 */
    Fail,

    /** 数据为空，展示空状态图标和文案 */
    Empty
}
