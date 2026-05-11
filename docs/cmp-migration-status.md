# CMP Migration Status

评估日期：2026-05-09  
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
| 首页 Shell/Tab | `ui/page/home` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/home` | 基本完成 | 基本完成 | 部分 | 部分完成 | 首页 Tab、初始 tab 路由、发帖刷新已接入；关注筛选恢复 Android 行为；系统消息/创作中心等未迁移下游入口先安全提示。 |
| 首页推荐 | `ui/page/home/pager/home/recommend` | 同路径 commonMain | 基本完成 | 部分 | 完成 | 部分完成 | 推荐帖子、课程、教师数据已走 `HomeApi`；教师卡片进入教师详情仍是空 stub。 |
| 首页交流/讨论 | `ui/page/home/pager/home/communicate` | 同路径 commonMain | 基本完成 | 部分 | 完成 | 部分完成 | 讨论帖子、课程推荐、导师推荐、发帖入口接入；排课/教师详情入口仍空。 |
| 首页关注 | `ui/page/home/pager/follow` | 同路径 commonMain | 基本完成 | 基本完成 | 完成 | 部分完成 | 关注用户和动态接口已接入；点击关注用户会筛选动态并显示选中边框，再次点击恢复全部关注流。 |
| 首页消息 | `ui/page/home/pager/message` | 同路径 commonMain | 基本完成 | 部分 | 完成 | 部分完成 | 消息列表与批量已读已接入；系统消息/课堂助手详情页仍未迁移，当前入口安全提示不崩溃。 |
| 首页我的/Profile | `ui/page/home/pager/profile` | 同路径 commonMain | 基本完成 | 部分 | 部分 | 部分完成 | 个人中心基础信息接口已接入；收藏/浏览/互动入口走 typed route；头像上传、资料修改、钱包等仍待平台化/真实接口。 |
| Vortexa 学堂/学校 | `ui/page/home/pager/school` | 同路径 commonMain | 基本完成 | 部分 | 完成 | 部分完成 | 课程/教师列表数据已有真实接口；筛选下游、预约/教师详情/排课仍未完整。 |
| 搜索首页 | `ui/page/search` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/search` | 基本完成 | 基本完成 | 部分 | 部分完成 | 首页搜索入口可打开；搜索建议可从首页接口取；搜索历史已从内存 stub 改为 `SpHelper`/multiplatform settings 持久化。 |
| 搜索结果 | `ui/page/search/result` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/search/result` | 基本完成 | 基本完成 | 部分 | 部分完成 | 已接入 `POST /v/api/search/result`，结果 Tab 已启用；综合/帖文复用帖子列表，用户/导师/工具箱/课程等非帖子 Tab 先给稳定空态。 |
| 热帖列表 | `ui/page/post/list` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/post/list` | 基本完成 | 完成 | 完成 | 部分完成 | 列表数据接入首页帖子接口；热帖列表已补 CMP route bridge，点赞/收藏写操作走真实接口。 |
| 帖子详情 | `ui/page/post/detail` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/post/detail` | 基本完成 | 基本完成 | 基本完成 | 部分完成 | 详情、评论、回复、发表评论已真实接口；点赞、收藏、关注、删除、评论点赞均走真实接口；他人主页入口仍属 P2 占位。 |
| 发帖/编辑帖子 | `ui/page/post/create` | `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/post/create` | 基本完成 | 基本完成 | 基本完成 | 部分完成 | 新建、编辑更新和图片上传均已走真实接口；编辑路由预填已接入；本地视频上传明确失败不假成功，远程视频 URL 可保留。 |
| 收藏列表 | `ui/page/profile/collection` | 同路径 commonMain | 基本完成 | 基本完成 | 完成 | 部分完成 | 已接入 `POST /v/api/user/collections`，筛选 module 放 body，分页走 query；点赞/取消收藏走真实接口，取消收藏成功后移出列表。 |
| 浏览历史 | `ui/page/profile/history` | 同路径 commonMain | 基本完成 | 基本完成 | 完成 | 部分完成 | 已接入 `GET /v/api/user/viewHistory`，module/pageNum/pageSize 走 query；点赞/收藏写操作走真实接口。 |
| 互动记录 | `ui/page/profile/interaction` | 同路径 commonMain | 基本完成 | 基本完成 | 完成 | 部分完成 | 已接入 `POST /v/api/user/interactions`，actorType/actionType/direction 放 body，分页走 query；记录点击可进入帖子详情。 |
| 系统消息 | `ui/page/systemmsg` | `SystemMessageActivity.kt` | 未完成 | 未完成 | 未完成 | 未完成/Stub | commonMain 只有 Activity 壳，候选页面未接入。 |
| 创作者中心 | `ui/page/creator` | `CreatorCenterActivity.kt` | 未完成 | 未完成 | 未完成 | 未完成/Stub | commonMain 只有 Activity 壳，候选页面未接入。 |
| 数据中心 | `ui/page/creator/statistics` | 无完整 commonMain 页面 | 未完成 | 未完成 | 未完成 | 未完成/Stub | 候选源码存在，未路由、未编译接入。 |
| 我的关注/粉丝 | `ui/page/profile/focus` | 无完整 commonMain 页面 | 未完成 | 未完成 | 未完成 | 未完成/Stub | 候选源码存在，未接入。 |
| 他人主页 | `ui/page/profile/other` | `OtherUserProfileActivity.kt` | 未完成 | 未完成 | 部分 | 未完成/Stub | commonMain startIfNotSelf 为空；用户资料接口已有基础能力但页面未接入。 |
| 稿件/帖子管理 | `ui/page/profile/paper/management` | `PaperManagementActivity.kt` | 未完成 | 未完成 | 未完成 | 未完成/Stub | commonMain 只有 Activity 壳。 |
| 发布入口包装页 | `ui/page/profile/paper/post` | 无完整 commonMain 页面 | 未完成 | 未完成 | 部分 | 未完成/Stub | 可复用 `AppRoute.PostCreate`，但 Android 包装视觉未确认接入。 |
| 我的课程 | `ui/page/teach/myclass` | `MyClassActivity.kt` | 未完成 | 未完成 | 部分 | 未完成/Stub | commonMain 只有 Activity 壳；部分 C2C 列表接口已接入。 |
| 教师主页 | `ui/page/teach/profile` | `TeacherProfileActivity.kt` | 未完成 | 未完成 | 部分 | 未完成/Stub | `TeacherProfileActivity.start` 为空，候选页面未接入。 |
| 排课/预约 | `ui/page/teach/schedule` | `ScheduleActivity.kt` | 未完成 | 未完成 | 部分 | 未完成/Stub | `ScheduleActivity.start` 为空，候选页面未接入。 |
| 订单详情/确认支付 | `ui/page/teach/order`, `ui/page/teach/schedule/confirm*` | 无完整 commonMain 页面 | 未完成 | 未完成 | 未完成 | 未完成/Stub | 候选源码存在，未路由、未数据接入。 |
| 钱包/充值/交易详情 | `ui/page/wallet` | 无完整 commonMain 页面 | 未完成 | 未完成 | 未完成 | 未完成/Stub | Android 侧部分入口可能为“即将上线”，CMP 仍未完整接入。 |
| 声网/RTC 视频课堂 | `ui/page/teach/video` | 候选源码存在 | 不评估 | 不评估 | 不评估 | 排除 | 第一期明确排除声网部分。 |

## 已完成的关键基础设施

| 能力 | CMP 文件 | 状态 | 说明 |
| --- | --- | --- | --- |
| typed route | `AppRoute.kt`、`VortexaRoot.kt` | 部分完成 | 已覆盖 Splash、Auth、Home、Search、PostDetail、PostCreate、ImagePreview、Collection/History/Interaction。 |
| Activity start 桥接 | `NavigationRouteBridge.kt`、`PostDetailActivity.kt`、`ImagePreviewActivity.kt` | 部分完成 | PostDetail/ImagePreview 可路由；TeacherProfile、Schedule、OtherUserProfile 等仍为空。 |
| 账号网络 | `AccountApi.kt`、`AccountRepository.kt` | 完成 | 登录、验证码、短信验证、重置密码已走 Ktor JSON。 |
| 首页/帖子网络 | `HomeApi.kt`、`HomeRepository.kt` | 部分完成 | 推荐、讨论、详情、评论、发帖、编辑、图片上传和详情 `isLiked` 已接入；评论本地视频上传仍不做。 |
| 用户/消息/关注/C2C 数据 | `UserRepository.kt`、`MessageRepository.kt`、`FollowRepository.kt`、`C2cRepository.kt` | 部分完成 | 读取类接口已有真实请求；follow/like/collect/commentLike/delete 已接入；avatar/updateUserCenter/wallet 仍待平台化或真实接口。 |

## P0/P1 剩余高风险项

1. 搜索结果已接真实接口，但用户/导师/工具箱/课程等非帖子 Tab 仍是稳定空态，后续需补齐 Android-parity 展示、跳转和模型映射。
2. `UserRepository` 中 avatar/updateUserCenter/wallet 等资料与钱包接口仍固定成功或待平台化，会造成相关 UI 状态与服务端不一致。
3. 发帖/评论图片上传已接入 iOS 本地文件读取和 multipart 上传；仍需真机/后端联调验证图片权限、文件 URI 与失败提示。
4. 首页消息、Profile、帖子作者等下游仍依赖 P2 页面；当前多处入口采用安全提示或占位路由，不能视为完整产品闭环。
5. 多个 Android Activity 入口在 CMP 中仍是空壳或空 start，点击后无响应：他人主页、教师主页、排课、系统消息、创作者中心、稿件管理、我的课程等。
6. 声网/RTC 不纳入第一期，不应被实现任务误判为缺陷。

## 最近验证记录

2026-05-11 P1 AgentTeam 修复 Auth/Splash、Home、Search、PostDetail/HotList、PostCreate、Profile Core 后已运行：

```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
./gradlew :composeApp:compileCommonMainKotlinMetadata
./gradlew :composeApp:iosSimulatorArm64Test
```

结果：`BUILD SUCCESSFUL`。其中 `iosSimulatorArm64Test` 因当前无测试源最终为 `SKIPPED`，但 Gradle 任务成功完成。`./gradlew :composeApp:assemble --no-configuration-cache` 在源码编译阶段通过，但本机缺少 `iphoneos` SDK，最终失败于 `linkDebugFrameworkIosArm64` 的 `/usr/bin/xcrun` exit 72，判定为本地 Xcode/iOS SDK 环境问题。
