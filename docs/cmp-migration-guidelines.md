# CMP Migration Guidelines

适用范围：从 Android 项目 `/Users/luxin/Documents/android/outline/vortexa-android` 向 CMP 项目 `/Users/luxin/Documents/android/outline/Vortexa-cmp` 迁移页面、交互、网络请求和数据。  
核心原则：纯 COPY Android 现有 UI 和产品行为，只处理 Android 与 Compose Multiplatform 的平台差异。

## 迁移边界

必须遵守：

1. 不改 UI 设计：不改颜色、字号、字重、间距、圆角、图标、文案、空态、加载态、错误态、列表密度。
2. 不改产品行为：Android 怎么跳、怎么校验、怎么刷新、怎么分页、怎么确认弹窗，CMP 就保持同样行为。
3. 不做“顺手优化”：除非 Android 源码本身就如此，否则不要新增交互、重排布局、改筛选逻辑、改接口语义。
4. 不实现声网/RTC：第一期排除视频课堂/声网能力，只保留必要入口边界和占位，不接入 RTC SDK。
5. 不删除候选迁移源码：`composeApp/migrated/android-source-candidate/**` 是对照资产，不作为清理对象。
6. 不回滚他人改动：如果文件已有并行修改，先重新阅读，再做最小兼容修改。

允许修改的内容：

1. Android-only API 替换为 CMP bridge/wrapper，例如 Activity、Intent、Context、Toast、Log、SharedPreferences、Uri、ActivityResult。
2. Retrofit/Android 网络实现迁移为 commonMain 可用的 Ktor/API wrapper。
3. `Activity.start(...)`、`routeToPage(...)` 等入口迁移为 `AppRoute`、`VortexaRoot`、`NavigationRouteBridge`。
4. Android 本地存储迁移为 common expect/actual 或统一平台抽象。
5. Android 文件/媒体选择、图片上传、外部浏览器等能力迁移为平台封装。

## 标准迁移流程

### 1. 先读 Android 源码

每个页面开始前，先定位 Android 对照文件：

```text
/Users/luxin/Documents/android/outline/vortexa-android
composeApp/migrated/android-source-candidate/com/vortexa
```

需要确认：

1. 页面入口 Activity 和 extras。
2. Composable 层级、参数默认值、颜色和资源引用。
3. ViewModel 状态字段、加载时机、分页规则。
4. 点击事件、返回行为、Toast/弹窗、登录拦截。
5. Repository 接口路径、请求参数、响应映射、错误处理。

### 2. 再看 CMP 已有实现

优先复用现有文件和桥接：

```text
composeApp/src/commonMain/kotlin/com/vortexa/navigation/AppRoute.kt
composeApp/src/commonMain/kotlin/com/vortexa/navigation/VortexaRoot.kt
composeApp/src/commonMain/kotlin/com/vortexa/navigation/NavigationRouteBridge.kt
composeApp/src/commonMain/kotlin/com/vortexa/net/ApiClient.kt
composeApp/src/commonMain/kotlin/com/vortexa/repository
composeApp/src/commonMain/kotlin/com/vortexa/platform
```

不要新建第二套导航、第二套网络客户端或第二套 UI 组件，除非现有抽象确实无法表达 Android 行为。

### 3. 搬 UI 时只做平台适配

迁移 Composable 的顺序：

1. 先复制页面 Composable、子组件、ViewModel、Model。
2. 替换不可用 import，不改布局参数。
3. 资源名保持 Android 对照含义，缺资源时补资源或桥接，不用新视觉替代。
4. 如果 Android 页面依赖 Activity context，只把 context 行为改成 callback 或 route bridge。
5. 保留 Android 原有 preview/sample 数据，只用于 preview，不进入真实运行路径。

禁止做法：

1. 因 CMP 编译报错就重写成另一套布局。
2. 把原页面拆成新的设计系统组件。
3. 用“差不多”的文案或图标替换 Android 原文案/图标。
4. 为了简单，把未接接口的列表改成静态假数据。

### 4. 路由和返回统一接入

新增页面必须走 typed route：

```text
AppRoute -> VortexaRoot entry -> Page Composable
```

Android 入口迁移规则：

| Android 写法 | CMP 写法 |
| --- | --- |
| `Activity.start(context, id)` | `NavigationRouteBridge.navigate(AppRoute.X(id))` |
| `Intent` extras | `AppRoute` 参数 |
| `finish()` | `NavigationDispatcher.back()` 或页面 `onBack` callback |
| `setResult()` | callback、sync center、共享 state 或 route 返回后刷新 |
| `routeToPage(...)` | `NavigationRouteBridge.routeToPage(...)` 内集中映射 |

每接一个页面，要同步检查所有旧入口是否仍会点击无响应。

### 5. 网络和数据必须对齐 Android 契约

Repository 迁移原则：

1. 路径、query、body 字段名以 Android/后端契约为准。
2. 响应字段兼容 Android 已使用字段，缺省值只能用于防崩，不可掩盖接口未接。
3. 读取接口和写接口都要接：点赞、收藏、关注、删除、编辑、上传不能固定 `Result.success`。
4. 上传类接口必须处理 CMP 文件/URI 差异，不能返回空 URL。
5. 错误态要传回 ViewModel，让 Android 原有错误 UI/Toast 能触发。

发现 mock/stub 时，优先修 Repository，而不是在 UI 层补假数据。

### 6. 状态和持久化要平台化

Android `SharedPreferences`、缓存、搜索历史、token、用户信息，不能长期停留在 commonMain 内存 Map。

推荐边界：

1. commonMain 暴露统一接口，例如 `KeyValueStore` 或现有 `SpHelper` 的平台实现。
2. androidMain 使用 SharedPreferences 或 DataStore。
3. iosMain 使用 NSUserDefaults 或 Keychain，token 按安全要求选择。
4. ViewModel 不直接感知平台存储细节。

### 7. 媒体和文件能力必须走平台封装

涉及头像、发帖图片、评论图片、视频缩略图、图片预览时：

1. 选择媒体走 `MediaPicker` 或同类 expect/actual。
2. 预览使用 URL/平台可加载资源，不把 Android `Uri` 假装成通用 String。
3. 上传前校验数量、格式、大小，与 Android `ImagePickValidator` 行为一致。
4. 上传成功后把服务端 URL 写回 ViewModel 状态。

## 修复优先级

### P0：点击无响应和主链路中断

1. 空 `Activity.start` 或空 Activity 壳。
2. 已显示按钮但没有 route 的页面入口。
3. 登录、首页、帖子详情、发布、图片预览这类主链路崩溃或无法返回。
4. iOS 编译失败。

### P1：页面可打开但数据/写操作不真实

1. Repository 返回空列表。
2. 写操作固定成功。
3. 上传返回空 URL。
4. Android 有分页/筛选/Cursor，CMP 没有保持。

### P2：第一期内但非主链路页面

1. 创作者中心、数据中心、系统消息、我的关注、他人主页、稿件管理。
2. 教学非 RTC 页面：我的课程、教师主页、排课、订单详情。
3. 钱包/充值/交易详情，按 Android 当前真实开放状态处理。

## AgentTeam 分工建议

| Agent | 负责范围 | 文件边界 | 验收 |
| --- | --- | --- | --- |
| Worker A：导航/平台桥 | `AppRoute`、`VortexaRoot`、`NavigationRouteBridge`、平台 shims | navigation、platform、Activity wrapper | 所有第一期入口点击有响应，返回行为一致。 |
| Worker B：首页/帖子主链路 | Home、Recommend、Communicate、Follow、PostDetail、PostCreate | home、post、HomeRepository、UserRepository 写操作 | 首页和帖子主链路 UI/交互/数据一致。 |
| Worker C：个人中心/搜索 | Search、Collection、History、Interaction、Profile | search、profile、对应 Repository | 列表接口真实，筛选/分页/跳转一致。 |
| Worker D：P2 页面接入 | Creator、SystemMessage、OtherProfile、Teach 非 RTC | 对应页面目录和 route | 候选页面接入 commonMain，不引入 RTC。 |
| Worker E：验收/文档 | 迁移状态、差异清单、编译记录 | docs only | 每轮修复后更新完成度和剩余风险。 |

Agent 之间的硬边界：不同 Worker 不同时重写同一个页面目录；Repository 改动需要提前说明影响页面；任何 UI 视觉差异必须回到 Android 对照确认。

## 每个任务的验收清单

提交前逐项确认：

1. Android 对照文件已阅读，关键行为没有凭空猜测。
2. UI 没有主动改版，Compose 参数与资源语义保持一致。
3. 页面可以从真实入口打开，不只是 Composable 能编译。
4. 返回、登录拦截、刷新、分页、空态、错误态可触发。
5. Repository 没有新增固定成功或固定空列表。
6. Android-only import 已替换为 common/platform abstraction。
7. 不包含声网/RTC SDK 接入。
8. 已运行必要编译验证。

推荐验证命令：

```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

涉及 shared common 逻辑较多时追加：

```bash
./gradlew :composeApp:iosSimulatorArm64Test
```

如果命令失败，记录失败命令、错误摘要、是否为环境问题，以及本轮代码是否需要回滚或继续修复。

## 文档更新规则

每完成一个页面或一条接口链路，都要更新：

1. `docs/cmp-migration-status.md`：状态从未完成/部分完成移动到更高等级，并写明证据。
2. `docs/cmp-migration-fix-plan.md`：如果任务已经完成，标注完成结果或移出待办。
3. 若发现 Android 与 CMP 现状差异，记录在状态文档的说明中，不直接改 UI 逃避差异。

文档要反映真实状态。页面“能显示”不等于完成；只有 UI、交互、网络/数据都对齐，才可以标记为完全完成。
