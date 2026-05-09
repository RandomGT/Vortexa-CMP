# CMP Migration Fix Plan

Owner: AgentTeam Worker D  
Scope date: 2026-05-09  
Plan boundary: pure COPY from existing Android Compose UI into CMP. Do not redesign, restyle, or change product behavior. Only resolve CMP-vs-Android platform differences.

## Current Inventory

Android source root recorded by migration report:

- `/Users/luxin/Documents/android/outline/vortexa-android`

CMP candidate source:

- `composeApp/migrated/android-source-candidate/com/vortexa`

CMP active source:

- `composeApp/src/commonMain/kotlin/com/vortexa`

Important finding: many P2 Android pages are present in `migrated/android-source-candidate`, but only Activity shells or partial files exist in `src/commonMain`. CMP routing currently covers Splash, Login/Register/Forget, Home, Search/SearchResult, PostDetail, PostCreate, ImagePreview, and ProfileSubPage for Collection/History/Interaction. The first migration phase should add routeable pure-UI pages and shared logic, while excluding RTC/video-room implementation.

## Agent Split

- Worker A, P0: build health, navigation baseline, platform shims, compile unblockers. Owns shared runtime risk and must not change Android visual output.
- Worker B, P1: core feed/auth/search/post/profile flows already in route tree. Owns parity for Home, Login, Search, PostDetail, PostCreate, Collection, History, Interaction.
- Worker C, P1/P2 bridge: common repository/model/API shims, image preview/media picker wrappers, route bridge, shared UI components.
- Worker D, P2 GapInventoryAndSlices: owns this document and future task slicing only. May add docs. Must not modify `composeApp` business code.

## File Boundaries

Allowed for Worker D:

- `docs/cmp-migration-fix-plan.md`

Forbidden for Worker D:

- `composeApp/src/commonMain/kotlin/**`
- `composeApp/src/iosMain/kotlin/**`
- `composeApp/migrated/**`
- Gradle files, resource files, generated files

General forbidden actions for all implementation workers:

- Do not redesign UI, change colors, typography, spacing, icons, copy, or empty/loading/error visuals unless required to match Android source.
- Do not delete migrated Android candidate files as a cleanup step.
- Do not replace Android behavior with new product assumptions.
- Do not introduce RTC/Agora implementation in first phase.
- Do not revert another worker's files. If a file changed during the same migration window, re-read it and layer the smallest compatible edit.

## Shared Acceptance Commands

Run after implementation slices, from repo root:

```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
./gradlew :composeApp:iosSimulatorArm64Test
```

Optional focused checks when touching routing/resources:

```bash
./gradlew :composeApp:assemble
./gradlew :composeApp:tasks --all
```

If a command is unavailable for the current local setup, record the exact failure and the fallback compile command used.

## P0 Tasks

### P0-01 Build and Platform Baseline

- Android source path: `vortexa-android/app/src/main/java/com/vortexa/**` and migration report sources under `composeApp/migrated/android-source-candidate/com/vortexa`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa`, `composeApp/src/iosMain/kotlin/com/vortexa`
- Reuse: `AppConfig`, `TokenConfig`, `UserConfig`, `ApiClient`, `AuthNavGate`, `SessionUnauthorizedHandler`, `ToastUtil`, `BrowserLink`, `ActivityCompat`, `MediaPicker`, `ExternalBrowser`
- Replace Android-only: `android.util.Log`, `Toast`, `Context`, `Activity`, `Intent`, Android `Uri`, `SharedPreferences`, Activity Result contracts
- Acceptance: iOS simulator compile succeeds; platform abstractions are `expect/actual` or common wrappers; no UI visual edits.

### P0-02 Navigation Parity Backbone

- Android source path: Activity entry points in `composeApp/migrated/android-source-candidate/com/vortexa/ui/page/**/**Activity.kt`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/navigation/AppRoute.kt`, `VortexaRoot.kt`, `NavigationRouteBridge.kt`
- Reuse: existing `AppRoute`, `VortexaRoot`, `PostAuthNavigator`, `AppSchemeContract`, current `ProfileSubPageKind`
- Replace Android-only: Activity `start(context)`, `Intent` extras, `finish()`, `setResult()`
- Acceptance: every first-phase screen has a typed route and back behavior; old `start` behavior is represented through route callbacks or bridge functions; compile passes.

### P0-03 Common UI Component Compatibility

- Android source path: `composeApp/migrated/android-source-candidate/com/vortexa/ui/component`, `ui/base`, `ui/theme`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/ui/component`, `ui/base`, `ui/theme`
- Reuse: `AvatarImage`, `ClickableLinkText`, `PageStatusView`, `Widgets`, `VortexaTheme`, resources in `composeResources`
- Replace Android-only: `BaseActivity`, `LocalContext` resource queries, Android drawable/resource references not available in CMP
- Acceptance: common components compile on iOS; UI call sites use identical layout parameters and resource names where possible.

### P0-04 Network Repository Baseline

- Android source path: `composeApp/migrated/android-source-candidate/com/vortexa/api`, `repository`, `net`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/repository`, `net`, `lib_net`
- Reuse: current `AccountRepository`, `HomeRepository`, `UserRepository`, `MessageRepository`, `FollowRepository`, `InteractionRepository`, `SearchRepository`, `C2cRepository`
- Replace Android-only: Retrofit annotations/client assumptions, `android.net.Uri` upload paths, Android file conversion
- Acceptance: repository interfaces used by first-phase screens are available in common code; platform upload is routed through common media/file abstractions.

## P1 Tasks

### P1-01 Auth and Splash

- Android source path: `ui/page/splash`, `ui/page/login`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/splash`, `ui/page/login`
- Reuse: `AuthModels`, `AccountRepository`, password/phone validators, `AuthNavGate`
- Replace Android-only: Activity shell, Toast, Android back/finish calls
- Acceptance: Splash routes to auth/home according to session; login/register/forget preserve Android UI and validation.

### P1-02 Home Shell and Tabs

- Android source path: `ui/page/home`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/home`
- Reuse: `HomeRepository`, `RecommendCard`, `DynamicPostsModels`, `SchoolCourseCard`, `TeacherListModels`, existing resources
- Replace Android-only: Activity shell, local context navigation, Android log
- Acceptance: Home tab structure, guest login prompt, recommend/follow/message/profile/school tab visuals match Android.

### P1-03 Search

- Android source path: `ui/page/search`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/search`
- Reuse: `SearchModels`, `SearchRepository`, `SearchHistoryRepository` common replacement
- Replace Android-only: SharedPreferences search history, Activity navigation
- Acceptance: search input, history, hot topics, tutor recommendation, result tabs, and post/composite lists are routeable and compile.

### P1-04 Post Detail and Hot List

- Android source path: `ui/page/post/detail`, `ui/page/post/list`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/post/detail`, `ui/page/post/list`
- Reuse: `PostDetailModels`, `Post`, `InteractionRepository`, `HomeRepository`, `ImagePreviewShell`, comment/reply components
- Replace Android-only: `ImagePreviewActivity.start`, Android media preview `Bitmap/MediaMetadataRetriever/Uri`, `PostDetailActivity.start`
- Acceptance: post detail, comments, replies, emoji panel, media preview handoff, and hot-post list route without Android classes.

### P1-05 Post Create

- Android source path: `ui/page/post/create`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/post/create`
- Reuse: `PostCreateModels`, `PostUploadModels`, `PostCreateViewModel`, `MediaPicker`, `ImagePickValidator`
- Replace Android-only: Activity Result photo picker, Android `Uri`, Android thumbnail loader, Activity result return
- Acceptance: create/edit route accepts existing arguments; image/video pickers use CMP platform abstraction; selection limits and validation match Android.

### P1-06 Profile Core Subpages

- Android source path: `ui/page/profile/collection`, `history`, `interaction`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/profile/collection`, `history`, `interaction`
- Reuse: `CollectionModels`, `HistoryModels`, `InteractionModels`, `UserRepository`, `InteractionRepository`
- Replace Android-only: Activity shell, `PostDetailActivity.start`, Toast/Log
- Acceptance: collection/history/interaction list filters, tabs, and post navigation preserve Android behavior.

## P2 First-Phase Tasks

### P2-01 Creator Center

- First phase: yes
- Android source path: `composeApp/migrated/android-source-candidate/com/vortexa/ui/page/creator`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/creator`
- Reuse: `CreatorModels.kt`, add/port `CreatorRepository` if absent, `InteractionRepository`, `PostManagementModels`, existing creator banner/data/task components
- Replace Android-only: `CreatorCenterActivity`, `Intent`, `LocalContext.startActivity`, `routeToPage`, Android `Log`
- Task slice:
  1. Copy missing pure Composable files: `CreatorCenterHeader`, `CreatorCenterDataCard`, `CreatorCenterQuickEntry`, `CreatorCenterBannerSection`, `CreatorCenterTaskSection`, `CreatorCenterView`, `CreatorViewModel`.
  2. Add typed routes for creator center, data center, interaction, paper management.
  3. Replace quick-entry navigation with callbacks supplied by CMP route tree.
- Acceptance: Creator center screen is routeable from profile/menu; data cards, banner activities, task cards match Android layout; no Activity/Intent imports remain.

### P2-02 Data Center

- First phase: yes
- Android source path: `composeApp/migrated/android-source-candidate/com/vortexa/ui/page/creator/statistics`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/creator/statistics`
- Reuse: `CreatorModels.kt`, `CreatorRepository`, `DataOverviewCard`, `DataCenterPostItem`, `DataCenterSortByPopup`
- Replace Android-only: `DataCenterActivity`, `LocalContext`, Android `Log`
- Task slice:
  1. Copy all data-center Composable and ViewModel files.
  2. Convert Activity `teacherId/userId` extras to route parameters if needed.
  3. Keep sorting popup and list pagination behavior identical.
- Acceptance: overview metrics and post data list render with Android copy/layout; sorting and empty/loading/error states match candidate source.

### P2-03 System Message

- First phase: yes
- Android source path: `composeApp/migrated/android-source-candidate/com/vortexa/ui/page/systemmsg`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/systemmsg`
- Reuse: `MessageModels.kt`, `MessageRepository.kt`, `SystemMessagePageType`, existing message tab entry from Home message page
- Replace Android-only: `SystemMessageActivity`, `LocalContext`, `PostDetailActivity.start`, Android `Log`
- Task slice:
  1. Copy missing header/list/page-type/view/viewmodel files.
  2. Add `AppRoute.SystemMessage(type)` or equivalent typed route.
  3. Replace item click side effects with route callbacks.
- Acceptance: system message list page opens from message center; notification types and detail navigation match Android.

### P2-04 My Focus

- First phase: yes
- Android source path: `composeApp/migrated/android-source-candidate/com/vortexa/ui/page/profile/focus`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/profile/focus`
- Reuse: `FollowingListModels.kt`, `FollowRepository.kt`, `UserRepository.kt`, existing avatar/resources
- Replace Android-only: `MyFocusActivity`, Android `Log`, Activity navigation to other profile
- Task slice:
  1. Copy focus header/item/view/viewmodel.
  2. Add `ProfileSubPageKind.Focus` or dedicated route.
  3. Route user click to other-user profile route.
- Acceptance: following/follower list UI matches Android; unfollow/follow state updates through repository; back behavior matches profile subpages.

### P2-05 Other User Profile

- First phase: yes
- Android source path: `composeApp/migrated/android-source-candidate/com/vortexa/ui/page/profile/other`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/profile/other`
- Reuse: `UserCenterFeedModels.kt`, `UserModels.kt`, `Post.kt`, `PostDetailModels.kt`, `UserRepository`, `FollowRepository`, `InteractionRepository`
- Replace Android-only: `OtherUserProfileActivity`, `Intent` extras, `PostDetailActivity.start`, `ImagePreviewActivity.start`, self-profile Activity guard
- Task slice:
  1. Copy header/stats/tab/posts/replies/comment item/view/viewmodel.
  2. Add route `OtherUserProfile(userId)` and self-profile redirect behavior in route bridge.
  3. Replace image preview/post detail navigation with callbacks.
- Acceptance: other-user header stats, follow button, posts tab, replies tab, image preview, and post detail links behave like Android.

### P2-06 Paper Management

- First phase: yes
- Android source path: `composeApp/migrated/android-source-candidate/com/vortexa/ui/page/profile/paper/management`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/profile/paper/management`
- Reuse: `PostManagementModels.kt`, `HomeRepository`, `UserRepository`, local `PaperManagementRepository` if still needed
- Replace Android-only: `PaperManagementActivity`, Activity navigation to post detail/edit/create, Android `Log`
- Task slice:
  1. Copy filter/header/item/view/viewmodel/repository.
  2. Convert edit action to `AppRoute.PostCreate(editPostId=...)`.
  3. Convert detail action to `AppRoute.PostDetail(postId=...)`.
- Acceptance: draft/published/rejected filtering and item actions match Android; management page opens from creator/profile entry.

### P2-07 Publish Post Shortcut Page

- First phase: yes
- Android source path: `composeApp/migrated/android-source-candidate/com/vortexa/ui/page/profile/paper/post`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/profile/paper/post`
- Reuse: `PostCreateView`, `PostCreateModels`, existing publish header/view
- Replace Android-only: `PublishPostActivity`, direct Activity launch
- Task slice:
  1. Copy `PublishPostHeader` and `PublishPostView` only if Android has a distinct wrapper visual.
  2. Otherwise map entry directly to existing `AppRoute.PostCreate`.
  3. Keep visual parity with Android publish entry, including top bar.
- Acceptance: profile/creator publish entry opens the same Android-equivalent publish UI; no duplicate divergent compose layout.

### P2-08 Teaching Non-RTC My Class

- First phase: yes, excluding video classroom
- Android source path: `composeApp/migrated/android-source-candidate/com/vortexa/ui/page/teach/myclass`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/teach/myclass`
- Reuse: `ReserveListItem.kt`, `ReserveListApiStatus.kt`, `TeacherReserveModels.kt`, `C2cRepository.kt`
- Replace Android-only: `MyClassActivity`, `ClassAssistantActivity.start`, `OrderDetailActivity.start`, Toast/Log
- Task slice:
  1. Copy tab/title/view/viewmodel plus one-to-one and school list/filter pages.
  2. Add typed routes for MyClass, ClassAssistant, OrderDetail.
  3. Preserve list filters, tabs, status chips, and empty states.
- Acceptance: my-class one-to-one and school pages compile and navigate without RTC/video code.

### P2-09 Teaching Non-RTC Teacher Profile and Schedule

- First phase: yes, excluding video classroom
- Android source path: `composeApp/migrated/android-source-candidate/com/vortexa/ui/page/teach/profile`, `teach/schedule`, `teach/schedule/confirm`, `teach/schedule/confirm2`, `teach/order/one2one`, `teach/helper`
- CMP target path: matching `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/teach/**`
- Reuse: `TeacherDetailModels.kt`, `TeacherListModels.kt`, `TeacherReserveTime.kt`, `ReserveDetail.kt`, `ReserveClassroomDetail.kt`, `C2cRepository.kt`
- Replace Android-only: Activity starts/extras/results, Toast/Log, support/customer-service Android intents
- Task slice:
  1. Copy teacher profile content/header/course/review/bottom button/view/viewmodel.
  2. Copy schedule calendar/list and confirm/pay-confirm views.
  3. Copy class assistant and order detail non-video flows.
  4. Route appointment, cancel, accept/refuse actions through repository and callback events.
- Acceptance: teacher profile, schedule, pay confirm, order detail, and class assistant non-RTC flows match Android visuals and compile in common code.

### P2-10 Wallet

- First phase: yes, with payment SDK excluded
- Android source path: `composeApp/migrated/android-source-candidate/com/vortexa/ui/page/wallet`
- CMP target path: `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/wallet`
- Reuse: `AccountRepository.kt`, wallet/recharge/record models from existing or candidate account models, `ic_profile_wallet`, pay-channel resources if available
- Replace Android-only: `WalletActivity`, `PointRechargeActivity`, `DealDetailActivity`, Android payment SDK calls, external browser/payment intents
- Task slice:
  1. Copy wallet header, medium info, tabs, pagination, record item/list, view/viewmodel.
  2. Copy deal detail body/bottom bar/toolbar/view.
  3. Copy recharge top bar/agreement/pay-channel/list/view but gate actual payment invocation behind future platform payment abstraction.
  4. Add routes for wallet, recharge, transaction detail.
- Acceptance: balance, transaction list, recharge package selection, agreement row, pay-channel visual selection, and deal detail UI match Android; pay submit may show existing no-op/error toast until payment abstraction exists.

## P2 RTC-Excluded Tasks

### P2-X01 Video RTC Classroom

- First phase: no
- Android source path: `composeApp/migrated/android-source-candidate/com/vortexa/ui/page/teach/video`
- CMP target path: future `composeApp/src/commonMain/kotlin/com/vortexa/ui/page/teach/video`
- Reuse later: `RtcChannelUserProfile.kt`, `C2cRepository.getRtcChannelUserProfile`, `RtcPlayView` concept
- Excluded Android-only: Agora/RTC engine, camera/mic permissions, audio/video rendering surfaces, room lifecycle
- Acceptance for first phase: no route exposes RTC room; non-RTC teaching pages must not import video RTC components.

### P2-X02 RTC Component Shim

- First phase: no
- Android source path: `composeApp/migrated/android-source-candidate/com/vortexa/ui/component/RtcPlayView.kt`
- CMP target path: future platform component under `ui/component` plus iOS actuals
- Reuse later: visual placeholder dimensions and user item layout from Android
- Excluded Android-only: native view interop for video surface, Agora SDK binding
- Acceptance for first phase: any compile blocker from RTC is avoided by not copying dependent files.

## Cross-Cutting Replacement Backlog

- Navigation: replace `Activity.start`, `context.startActivity(Intent(...))`, `routeToPage`, `finish`, `setResult` with route callbacks and `AppRoute`.
- Toast: replace Android context overloads with `com.vortexa.platform.AppToast` or existing common `ToastUtil`.
- Logging: replace `android.util.Log` with common platform logger or remove only where equivalent no-op already exists.
- Media: replace Android `Uri`, `ContentResolver.loadThumbnail`, `MediaMetadataRetriever`, Activity Result picker with `MediaPicker` plus common media-preview model.
- Storage: replace `SharedPreferences`-based search history/session with common storage abstraction.
- Payment: keep wallet/recharge visuals, but route actual Alipay/WeChat/Apple Pay invocation to future platform payment interface.
- Deep links: keep `AppSchemeContract` semantics, but parse to typed routes rather than Activity classes.
- Resources: copy Android resource references only when corresponding files exist under `composeResources`; otherwise add missing resource assets in a separate resource-only slice.

## Complete Task Order

1. P0-01 Build and Platform Baseline
2. P0-02 Navigation Parity Backbone
3. P0-03 Common UI Component Compatibility
4. P0-04 Network Repository Baseline
5. P1-01 Auth and Splash
6. P1-02 Home Shell and Tabs
7. P1-03 Search
8. P1-04 Post Detail and Hot List
9. P1-05 Post Create
10. P1-06 Profile Core Subpages
11. P2-01 Creator Center
12. P2-02 Data Center
13. P2-03 System Message
14. P2-04 My Focus
15. P2-05 Other User Profile
16. P2-06 Paper Management
17. P2-07 Publish Post Shortcut Page
18. P2-08 Teaching Non-RTC My Class
19. P2-09 Teaching Non-RTC Teacher Profile and Schedule
20. P2-10 Wallet
21. P2-X01 Video RTC Classroom, after first phase
22. P2-X02 RTC Component Shim, after first phase

## First-Phase Definition of Done

- All P0 and P1 tasks compile on iOS.
- P2 first-phase pages are routeable from existing entries or typed routes.
- Android candidate UI layout is copied without visual redesign.
- No first-phase file imports `com.vortexa.ui.page.teach.video` or `RtcPlayView`.
- No new business behavior is invented for payment or RTC.
- Acceptance commands have been run or failures documented with exact output summary.
