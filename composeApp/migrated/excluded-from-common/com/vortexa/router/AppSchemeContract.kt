package com.vortexa.router

/**
 * 服务端下发 **完整 URI 字符串**，客户端仅识别 [SCHEME]。
 *
 * ## 路径与 query（与后端对齐）
 *
 * | 语义 | URI 示例 | 必填 query |
 * |------|----------|------------|
 * | 首页 Tab | `vortexa://home?tab=0` | `tab`：0–4，缺省为 0 |
 * | 帖子详情 | `vortexa://post/detail?postId=xxx` | `postId`；兼容 `id` |
 * | 用户主页 | `vortexa://user/profile?userId=123` | `userId`（Long） |
 * | 搜索 | `vortexa://search` | 无 |
 * | 热帖列表 | `vortexa://post/hot` | 无 |
 * | 发布贴文 | `vortexa://post/create` | 无；编辑态见下行 |
 * | 编辑贴文 | `vortexa://post/create?postId=&title=&content=` | `postId`；可选 `board`、`images`、`videos`（逗号分隔 URL） |
 * | 纸片发布 | `vortexa://paper/publish` | 可选 `postId`（Long，0 新建） |
 * | 系统消息 | `vortexa://message/system` | 无 |
 * | 导师主页 | `vortexa://teacher/profile?teacherId=` | `teacherId` 或 `userId`（Long） |
 * | 讲师日程 | `vortexa://teach/schedule?teacherId=` | `teacherId`（Long） |
 * | 预约确认 | `vortexa://teach/schedule/confirm?teacherId=&reserveDate=&reserveHour=` | 同上；可选 `courseTitle`、`teacherName` |
 * | 支付确认 | `vortexa://teach/schedule/confirm2` | 同确认页 query |
 * | 我的课程 | `vortexa://teach/myclass` | 无 |
 * | 订单详情 | `vortexa://order/detail?orderId=` | `orderId`（Int，同预约 ID）；兼容 `reserveId` |
 * | 课堂小助手 | `vortexa://teach/class-assistant?reserveId=` | `reserveId`（Int）；兼容 `orderId`；可选 `role`：`teacher` / `student`；也可用 Intent extra [EXTRA_MERGE_ROLE_FROM_INTENT] 传入；缺省按登录用户与详情推断 |
 * | 视频 RTC | `vortexa://teach/rtc?channelName=&teacherId=` | `channelName`（兼容 `channel`）、`teacherId`（Long）；可选 `courseStartMs`（Long） |
 * | 创作者中心 | `vortexa://creator/center` | 无 |
 * | 数据中心 | `vortexa://creator/statistics` 或 `vortexa://creator/data` | 无 |
 * | 钱包 | `vortexa://wallet` | 无 |
 * | 交易详情 | `vortexa://wallet/deal` | 可选 `dealId` |
 * | 互动 | `vortexa://profile/interaction` | 无 |
 * | 收藏 | `vortexa://profile/collection` | 无 |
 * | 浏览记录 | `vortexa://profile/history` | 无 |
 * | 我的关注 | `vortexa://profile/focus` | 无 |
 * | 纸片管理 | `vortexa://paper/management` | 无 |
 * | 图片预览 | `vortexa://image/preview` | 多图：`url` 可重复，或 `urls` 逗号分隔，或单 `url`；可选 `index` |
 * | 沟通（占位页） | `vortexa://communicate` | 无 |
 *
 * 形态：`vortexa://{host}/{path}?...`，[routeKey] 归一化为 `{host}` 或 `{host}/{path去掉两侧斜杠}`。
 *
 * [open][AppSchemeRouter.open] 的返回值为 [OpenResult]，便于埋点：成功 / 格式错误 / 未注册路由 / 需登录（已写入待跳转并拉起登录）。
 */
object AppSchemeContract {

    const val SCHEME = "vortexa"

    /** 防止异常超长字符串 */
    const val MAX_URI_LENGTH = 2048

    /**
     * 与 [android.intent.action.VIEW] 的 data 一并传入时，可将 `role=teacher|student` 放进 extra，
     * 由 [AppSchemeRouter.consumeViewIntentIfScheme] 拼回 URI query（避免 adb / 部分 shell 截断 `&role=`）。
     *
     * 示例：`am start -a VIEW -d "vortexa://teach/class-assistant?reserveId=43" --es vortexa_role teacher com.vortexa`
     */
    const val EXTRA_MERGE_ROLE_FROM_INTENT = "vortexa_role"

    /** 非 VIEW data、由 [AppSchemeRouter] 启动 Home 时携带的主 Tab */
    const val EXTRA_HOME_TAB = "extra_scheme_home_tab"
}
