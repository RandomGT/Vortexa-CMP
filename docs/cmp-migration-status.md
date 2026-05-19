# CMP Migration Status

评估日期：2026-05-19
CMP 项目：`/Users/luxin/Documents/android/outline/Vortexa-cmp`
Android 对照项目：`/Users/luxin/Documents/android/outline/vortexa-android`
迁移边界：第一期包含完整 UI、交互、网络请求和数据；不包含声网/RTC。

## 完成口径

| 状态 | 判定标准 |
| --- | --- |
| 完全完成 | UI 高度 COPY Android；点击、返回、跳转、表单校验、刷新等交互可用；页面依赖的网络请求和数据映射已接入真实接口或该页面无网络数据；没有影响主链路的 mock/stub。 |
| 部分完成 | UI 或主要交互已迁移，但仍缺少路由、数据、持久化、写操作、入口页或 Android 行为一致性。 |
| 未完成/Stub | 仅有 Activity 壳、空 start 方法、候选源码未接入，或页面不可从 CMP 路由打开。 |
| 排除 | 第一阶段明确不做的声网/RTC/视频房间能力。 |

## 当前严格结论

按“UI + 交互 + 网络/数据”全量一致的严格口径，当前可标记为完全完成的范围很少：

| 已完全完成项 | 范围 | 证据 | 备注 |
| --- | --- | --- | --- |
| 图片预览 | 图片预览页 UI、翻页指示、返回、从帖子/详情打开预览 | `AppRoute.ImagePreview`、`ImagePreviewActivity.start`、`ImagePreviewShell` 已接入 `VortexaRoot` | 无独立网络数据，属于纯 UI/交互页。 |
| 基础网络通道 | Ktor JSON 客户端、统一响应、账号/首页接口的一部分 | `ApiClient`、`ApiResponse`、`AccountApi`、`HomeApi` | 是基础能力，不代表所有页面接口都完成。 |

登录、首页、帖子详情、发布等主链路已经接近可用，但仍存在持久化、写接口、上传接口或下游页面 stub，因此在严格口径下暂不标“完全完成”。

## 页面与模块状态

| 模块/页面 | Android 对照路径 | CMP 当前路径 | UI | 交互 | 网络/数据 | 状态 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Splash | `ui/page/splash` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/splash` | 完成 | 完成 | 完成 | 部分完成 | 2 秒后按 `TokenConfig.getToken()` 分流到 Home/Login；iOS token/user 基础信息已通过 `NSUserDefaults` 持久化。 |
| 登录 | `ui/page/login` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/login` | 完成 | 完成 | 完成 | 部分完成 | 登录跳转已接入 `AppRoute.Home`，账号接口真实请求；登录成功写入持久化 session。 |
| 注册 | `ui/page/login/register` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/login/register` | 完成 | 完成 | 完成 | 部分完成 | 注册成功可进首页，验证码/认证接口接入；注册成功写入持久化 session。 |
| 忘记密码 | `ui/page/login/forget` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/login/forget` | 完成 | 完成 | 完成 | 部分完成 | 重置密码、登录页跳转已接入。 |
| 首页 Shell/Tab | `ui/page/home` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/home` | 基本完成 | 基本完成 | 部分 | 部分完成 | 首页 Tab、初始 tab 路由、发帖刷新已接入；消息系统通知、Profile 创作/钱包/课程/稿件/关注入口已接 P2 typed routes。 |
| 首页推荐 | `ui/page/home/pager/home/recommend` | 同路径 commonMain | 基本完成 | 基本完成 | 完成 | 部分完成 | 推荐帖子、课程、教师数据已走 `HomeApi`；教师卡片兼容 `TeacherProfileActivity.start`，现已进入 P2 教师主页 route。 |
| 首页交流/讨论 | `ui/page/home/pager/home/communicate` | 同路径 commonMain | 基本完成 | 基本完成 | 完成 | 部分完成 | 讨论帖子、课程推荐、导师推荐、发帖入口接入；预约/教师详情通过 P2 `Schedule`/`TeacherProfile` route。 |
| 首页关注 | `ui/page/home/pager/follow` | 同路径 commonMain | 基本完成 | 基本完成 | 完成 | 部分完成 | 关注用户和动态接口已接入；点击关注用户会筛选动态并显示选中边框，再次点击恢复全部关注流。 |
| 首页消息 | `ui/page/home/pager/message` | 同路径 commonMain | 基本完成 | 基本完成 | 完成 | 部分完成 | 消息列表与批量已读已接入；系统通知/课堂小助手点击进入 `SystemMessageView`，私信仍待后续。 |
| 首页我的/Profile | `ui/page/home/pager/profile` | 同路径 commonMain | 基本完成 | 基本完成 | 部分 | 部分完成 | 个人中心基础信息接口已接入；收藏/浏览/互动/关注、创作中心、稿件管理、钱包、我的课程均走 typed route；头像上传、资料修改仍待平台化/真实接口。 |
| Vortexa 学堂/学校 | `ui/page/home/pager/school` | 同路径 commonMain | 基本完成 | 部分 | 完成 | 部分完成 | 课程/教师列表数据已有真实接口；筛选下游、预约/教师详情/排课仍未完整。 |
| 搜索首页 | `ui/page/search` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/search` | 基本完成 | 基本完成 | 部分 | 部分完成 | 首页搜索入口可打开；搜索建议可从首页接口取；搜索历史已从内存 stub 改为 `SpHelper`/multiplatform settings 持久化。 |
| 搜索结果 | `ui/page/search/result` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/search/result` | 基本完成 | 基本完成 | 部分 | 部分完成 | 已接入 `POST /v/api/search/result`，结果 Tab 已启用；综合/帖文复用帖子列表，用户/导师/工具箱/课程等非帖子 Tab 先给稳定空态。 |
| 热帖列表 | `ui/page/post/list` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/post/list` | 基本完成 | 完成 | 完成 | 部分完成 | 列表数据接入首页帖子接口；热帖列表已补 CMP route bridge，点赞/收藏写操作走真实接口。 |
| 帖子详情 | `ui/page/post/detail` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/post/detail` | 基本完成 | 基本完成 | 基本完成 | 部分完成 | 详情、评论、回复、发表评论已真实接口；点赞、收藏、关注、删除、评论点赞均走真实接口；作者头像/用户名进入 P2 他人主页 bridge。 |
| 发帖/编辑帖子 | `ui/page/post/create` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/post/create` | 基本完成 | 基本完成 | 基本完成 | 部分完成 | 新建、编辑更新和图片上传均已走真实接口；编辑路由预填已接入；本地视频上传明确失败不假成功，远程视频 URL 可保留。 |
| 收藏列表 | `ui/page/profile/collection` | 同路径 commonMain | 基本完成 | 基本完成 | 完成 | 部分完成 | 已接入 `POST /v/api/user/collections`，筛选 module 放 body，分页走 query；点赞/取消收藏走真实接口，取消收藏成功后移出列表。 |
| 浏览历史 | `ui/page/profile/history` | 同路径 commonMain | 基本完成 | 基本完成 | 完成 | 部分完成 | 已接入 `GET /v/api/user/viewHistory`，module/pageNum/pageSize 走 query；点赞/收藏写操作走真实接口。 |
| 互动记录 | `ui/page/profile/interaction` | 同路径 commonMain | 基本完成 | 基本完成 | 完成 | 部分完成 | 已接入 `POST /v/api/user/interactions`，actorType/actionType/direction 放 body，分页走 query；记录点击可进入帖子详情。 |
| 系统消息 | `ui/page/systemmsg` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/systemmsg` | 基本完成 | 基本完成 | 基本完成 | 部分完成 | `SystemMessageView`、系统通知/课堂小助手类型、分页/刷新/已读参数已接入；scheme/detail 跳转仍预留 callback。 |
| 创作者中心 | `ui/page/creator` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/creator` | 基本完成 | 基本完成 | 基本完成 | 部分完成 | 已迁移 header、数据卡、快捷入口、活动 banner、任务卡与 `CreatorRepository`；Profile 入口可打开。 |
| 数据中心 | `ui/page/creator/statistics` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/creator/statistics` | 基本完成 | 基本完成 | 基本完成 | 部分完成 | 已迁移 overview、排序弹层、帖子数据列表与分页；帖子点击进详情。 |
| 我的关注/粉丝 | `ui/page/profile/focus` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/profile/focus` | 基本完成 | 基本完成 | 完成 | 部分完成 | 已接 `FollowRepository` 关注列表，follow/unfollow 走 `UserRepository`；Profile 关注数字可进入。 |
| 他人主页 | `ui/page/profile/other` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/profile/other` | 基本完成 | 基本完成 | 基本完成 | 部分完成 | 他人主页、帖子/回复 tab、关注与 self-profile guard 已接入；全局 `OtherUserProfileActivity.startIfNotSelf` 绑定到 typed route。 |
| 稿件/帖子管理 | `ui/page/profile/paper/management` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/profile/paper/management` | 基本完成 | 基本完成 | 基本完成 | 部分完成 | 过滤、列表、删除、详情、编辑进入 `PostCreate(edit...)` 已接入；数据按钮进入数据中心。 |
| 发布入口包装页 | `ui/page/profile/paper/post` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/profile/paper/post` | 基本完成 | 基本完成 | 部分 | 部分完成 | 已迁移 `PublishPostView`/header，并提供 shortcut route 映射到既有 `AppRoute.PostCreate()`。 |
| 我的课程 | `ui/page/teach/myclass` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/teach/myclass` | 基本完成 | 基本完成 | 基本完成 | 部分完成 | 非 RTC 我的课程、一对一列表、学院列表、课堂小助手/订单详情入口已接；上课视频入口保持提示。 |
| 教师主页 | `ui/page/teach/profile` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/teach/profile` | 基本完成 | 基本完成 | 基本完成 | 部分完成 | 教师主页、头像到个人主页、预约按钮到排课 route；`TeacherProfileActivity.start` 已桥接。 |
| 排课/预约 | `ui/page/teach/schedule` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/teach/schedule` | 基本完成 | 基本完成 | 基本完成 | 部分完成 | 日历、时段选择、订单确认、支付确认与预约接口接入；支付仍为积分预约流程，不含第三方支付 SDK。 |
| 订单详情/确认支付 | `ui/page/teach/order`, `ui/page/teach/schedule/confirm*` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/teach/**` | 基本完成 | 基本完成 | 基本完成 | 部分完成 | 订单详情、取消预约、再次预约、课堂助手接受/拒绝/取消非 RTC 流程已接；进入视频课堂仅 toast 占位。 |
| 钱包/充值/交易详情 | `ui/page/wallet` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/wallet` | 基本完成 | 基本完成 | 部分 | 部分完成 | 钱包首页、交易列表、交易详情、充值视觉与校验已迁移；真实钱包接口/第三方支付 SDK 仍待后续。 |
| 声网/RTC 视频课堂 | `ui/page/teach/video` | 候选源码存在 | 不评估 | 不评估 | 不评估 | 排除 | 第一期明确排除声网部分。 |

## 已完成的关键基础设施

| 能力 | CMP 文件 | 状态 | 说明 |
| --- | --- | --- | --- |
| typed route | `AppRoute.kt`、`VortexaRoot.kt` | 部分完成 | 已覆盖 Splash、Auth、Home、Search、PostDetail、PostCreate、ImagePreview、ProfileSubPage、Creator/DataCenter、SystemMessage、OtherProfile、Paper、Teach 非 RTC、Wallet。 |
| Activity start 桥接 | `NavigationRouteBridge.kt`、各 `*Activity` 兼容壳 | 部分完成 | P0/P1/P2 first-phase 入口已桥接；RTC/视频课堂和私信等后续页面仍不在第一阶段。 |
| 账号网络 | `AccountApi.kt`、`AccountRepository.kt` | 完成 | 登录、验证码、短信验证、重置密码已走 Ktor JSON。 |
| 首页/帖子网络 | `HomeApi.kt`、`HomeRepository.kt` | 部分完成 | 推荐、讨论、详情、评论、发帖、编辑、图片上传和详情 `isLiked` 已接入；评论本地视频上传仍不做。 |
| 用户/消息/关注/C2C 数据 | `UserRepository.kt`、`MessageRepository.kt`、`FollowRepository.kt`、`C2cRepository.kt` | 部分完成 | 读取类接口已有真实请求；follow/like/collect/commentLike/delete 已接入；avatar/updateUserCenter/wallet 仍待平台化或真实接口。 |

## P0/P1 剩余高风险项

1. 搜索结果已接真实接口，但用户/导师/工具箱/课程等非帖子 Tab 仍是稳定空态，后续需补齐 Android-parity 展示、跳转和模型映射。
2. `UserRepository` 中 avatar/updateUserCenter/wallet 等资料与钱包接口仍固定成功或待平台化，会造成相关 UI 状态与服务端不一致。
3. 发帖/评论图片上传已接入 iOS 本地文件读取和 multipart 上传；仍需真机/后端联调验证图片权限、文件 URI 与失败提示。
4. 私信、客服、scheme/deep-link 明细和第三方支付仍是后续项；P2 first-phase 仅保证相关页面视觉、状态和主路由闭环。
5. 声网/RTC 不纳入第一期；P2 教学页中的进入课堂入口保持安全提示，不应被实现任务误判为缺陷。

## 最近验证记录

2026-05-11 P1 AgentTeam 修复 Auth/Splash、Home、Search、PostDetail/HotList、PostCreate、Profile Core 后已运行：

```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
./gradlew :composeApp:compileCommonMainKotlinMetadata
./gradlew :composeApp:iosSimulatorArm64Test
```

结果：`BUILD SUCCESSFUL`。其中 `iosSimulatorArm64Test` 因当前无测试源最终为 `SKIPPED`，但 Gradle 任务成功完成。`./gradlew :composeApp:assemble --no-configuration-cache` 在源码编译阶段通过，但本机缺少 `iphoneos` SDK，最终失败于 `linkDebugFrameworkIosArm64` 的 `/usr/bin/xcrun` exit 72，判定为本地 Xcode/iOS SDK 环境问题。

2026-05-19 P2 AgentTeam 完成 Creator/DataCenter、SystemMessage、MyFocus/OtherProfile、Paper/PublishShortcut、Teach 非 RTC、Wallet 后已运行：

```bash
./gradlew :composeApp:compileCommonMainKotlinMetadata --no-configuration-cache
./gradlew :composeApp:compileKotlinIosSimulatorArm64 --no-configuration-cache
./gradlew :composeApp:iosSimulatorArm64Test --no-configuration-cache
```

结果：`BUILD SUCCESSFUL`。`iosSimulatorArm64Test` 因当前无测试源最终为 `SKIPPED`。另运行 `git diff --check` 时仅发现本轮前已存在的 `composeApp/src/iosMain/kotlin/android/util/PlatformLog.kt` 末尾空行；该文件未在 P2 实现中修改。RTC 扫描 `Agora|VideoRtc|RtcPlayView|teach/video` 无命中。
