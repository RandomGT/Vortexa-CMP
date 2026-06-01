package com.vortexa.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.vortexa.config.TokenConfig
import com.vortexa.ui.page.creator.CreatorCenterPage
import com.vortexa.ui.page.creator.statistics.DataCenterPage
import com.vortexa.ui.page.home.HomePostCreateSyncCenter
import com.vortexa.ui.page.home.HomePage
import com.vortexa.ui.page.login.LoginScreen
import com.vortexa.ui.page.login.forget.ForgetView
import com.vortexa.ui.page.login.register.RegisterPage
import com.vortexa.ui.page.imagepreview.ImagePreviewView
import com.vortexa.ui.page.post.create.PostCreateEditArgs
import com.vortexa.ui.page.post.create.PostCreateView
import com.vortexa.ui.page.post.detail.PostDetailReplyComposerHint
import com.vortexa.ui.page.post.detail.PostDetailView
import com.vortexa.ui.page.post.list.HotPostListView
import com.vortexa.ui.page.profile.collection.CollectionView
import com.vortexa.ui.page.profile.focus.MyFocusView
import com.vortexa.ui.page.profile.history.HistoryView
import com.vortexa.ui.page.profile.interaction.InteractionView
import com.vortexa.ui.page.profile.other.OtherUserProfile
import com.vortexa.ui.page.profile.other.OtherUserProfileActivity
import com.vortexa.ui.page.profile.paper.management.PaperItemData
import com.vortexa.ui.page.profile.paper.management.PaperManagementView
import com.vortexa.ui.page.profile.paper.post.PublishPostShortcutView
import com.vortexa.ui.page.search.SearchView
import com.vortexa.ui.page.search.result.SearchResultView
import com.vortexa.ui.page.splash.SplashPage
import com.vortexa.ui.page.systemmsg.SystemMessageView
import com.vortexa.ui.page.teach.helper.ClassAssistantRoute
import com.vortexa.ui.page.teach.myclass.MyClassView
import com.vortexa.ui.page.teach.order.one2one.OrderDetailRoute
import com.vortexa.ui.page.teach.profile.TeacherProfileView
import com.vortexa.ui.page.teach.schedule.ScheduleView
import com.vortexa.ui.page.teach.schedule.confirm.ConfirmView
import com.vortexa.ui.page.teach.schedule.confirm2.Confirm2ViewModel
import com.vortexa.ui.page.teach.schedule.confirm2.PayConfirmView
import com.vortexa.ui.page.teach.video.VideoRtcRoute
import com.vortexa.ui.page.wallet.WalletRecord
import com.vortexa.ui.page.wallet.WalletRecordType
import com.vortexa.ui.page.wallet.DealDetailState
import com.vortexa.ui.page.wallet.defaultDetailRows
import com.vortexa.ui.page.wallet.WalletView
import com.vortexa.ui.page.wallet.detail.DealDetailView
import com.vortexa.ui.page.wallet.pay.PointRechargeView
import com.vortexa.ui.theme.belowStatusBar
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.util.ToastUtil

@Composable
fun VortexaRoot() {
    val navController = rememberNavController()

    val dispatcher = remember(navController) {
        object : NavigationDispatcher {
            override fun navigate(route: AppRoute) {
                navController.navigate(route.toNavRoute())
            }

            override fun back() {
                navController.popBackStack()
            }

            override fun canGoBack(): Boolean = navController.previousBackStackEntry != null

            override fun replaceRoot(route: AppRoute) {
                navController.replaceRoot(route)
            }
        }
    }

    DisposableEffect(dispatcher) {
        NavigationRouteBridge.register(dispatcher)
        onDispose { NavigationRouteBridge.unregister(dispatcher) }
    }

    DisposableEffect(dispatcher) {
        val token = OtherUserProfileActivity.bindNavigation(
            onOpenOtherUserProfile = { userId -> dispatcher.navigate(AppRoute.OtherUserProfile(userId)) },
            onOpenSelfProfile = { dispatcher.navigate(AppRoute.Home(4)) },
        )
        onDispose { OtherUserProfileActivity.clearNavigationCallbacks(token) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Splash,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(NavRoutes.Splash) {
                SplashPage(onSplashFinish = {
                    val route = if (TokenConfig.getToken().isNotEmpty()) {
                        AppRoute.Home()
                    } else {
                        AppRoute.Login
                    }
                    dispatcher.replaceRoot(route)
                })
            }
            composable(NavRoutes.Home) {
                val tab = NavigationPayloadStore.homeTab
                HomePage(
                    initialTab = tab,
                    onOpenMyClass = { dispatcher.navigate(AppRoute.MyClass) },
                    onOpenWallet = { dispatcher.navigate(AppRoute.Wallet) },
                    onOpenCreatorCenter = { dispatcher.navigate(AppRoute.CreatorCenter) },
                    onOpenPaperManagement = { dispatcher.navigate(AppRoute.PaperManagement) },
                    onOpenMyFocus = { dispatcher.navigate(AppRoute.ProfileSubPage(ProfileSubPageKind.Focus)) },
                    onOpenSystemMessage = { entry ->
                        dispatcher.navigate(
                            AppRoute.SystemMessage(
                                messageType = entry.messageType,
                                markReadDialogId = entry.markReadDialogId,
                                markReadMessageId = entry.markReadMessageId,
                            )
                        )
                    },
                    onOpenTeacherProfile = { teacherId -> dispatcher.navigate(AppRoute.TeacherProfile(teacherId)) },
                    onOpenSchedule = { teacherId -> dispatcher.navigate(AppRoute.Schedule(teacherId)) },
                    onOpenOtherUserProfile = { userId -> dispatcher.navigate(AppRoute.OtherUserProfile(userId)) },
                )
            }
            composable(NavRoutes.Login) {
                LoginScreen(
                    onRegisterClick = { dispatcher.navigate(AppRoute.Register) },
                    onForgetClick = { dispatcher.navigate(AppRoute.ForgetPassword) },
                    onLoginSuccess = { dispatcher.replaceRoot(AppRoute.Home()) },
                )
            }
            composable(NavRoutes.Register) {
                RegisterPage(onRegisterSuccess = { dispatcher.replaceRoot(AppRoute.Home()) })
            }
            composable(NavRoutes.ForgetPassword) {
                ForgetView(
                    onResetSuccess = { dispatcher.replaceRoot(AppRoute.Login) },
                    onLoginClick = { dispatcher.replaceRoot(AppRoute.Login) },
                )
            }
            composable(NavRoutes.Search) { SearchView(onBack = dispatcher::back) }
            composable(NavRoutes.SearchResult) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .belowStatusBar()
                        .background(Color.White)
                ) {
                    SearchResultView(keyword = NavigationPayloadStore.searchKeyword)
                }
            }
            composable(NavRoutes.CreatorCenter) {
                CreatorCenterPage(
                    onBackClick = dispatcher::back,
                    onDataCenterClick = { dispatcher.navigate(AppRoute.DataCenter) },
                    onInteractionClick = { dispatcher.navigate(AppRoute.ProfileSubPage(ProfileSubPageKind.Interaction)) },
                    onPaperManagementClick = { dispatcher.navigate(AppRoute.PaperManagement) },
                )
            }
            composable(NavRoutes.DataCenter) {
                DataCenterPage(
                    onBackClick = dispatcher::back,
                    onPostClick = { postId -> dispatcher.navigate(AppRoute.PostDetail(postId.toString())) },
                )
            }
            composable(NavRoutes.SystemMessage) {
                SystemMessageView(
                    messageType = NavigationPayloadStore.systemMessageType,
                    markReadDialogId = NavigationPayloadStore.systemMessageMarkReadDialogId,
                    markReadMessageId = NavigationPayloadStore.systemMessageMarkReadMessageId,
                    onBackClick = dispatcher::back,
                    onOpenScheme = { scheme -> NavigationRouteBridge.routeToPage(scheme) },
                )
            }
            composable(NavRoutes.PostDetail) {
                PostDetailView(
                    postId = NavigationPayloadStore.postId,
                    replyComposerHint = NavigationPayloadStore.replyComposerHint,
                    openReplyComposerOnLoad =
                        NavigationPayloadStore.openReplyComposer &&
                            NavigationPayloadStore.replyComposerHint == null,
                    onBack = dispatcher::back,
                )
            }
            composable(NavRoutes.HotPostList) {
                HotPostListView(onBackClick = dispatcher::back)
            }
            composable(NavRoutes.PostCreate) {
                PostCreateView(
                    editArgs = NavigationPayloadStore.postCreateEditArgs,
                    onPublishSuccess = {
                        HomePostCreateSyncCenter.notifyPostCreated()
                        dispatcher.back()
                    }
                )
            }
            composable(NavRoutes.PaperManagement) {
                PaperManagementView(
                    onBackClick = dispatcher::back,
                    onPostClick = { item -> dispatcher.navigate(AppRoute.PostDetail(item.postId.toString())) },
                    onDataClick = { item -> dispatcher.navigate(AppRoute.DataCenter) },
                    onEditClick = { item ->
                        dispatcher.navigate(
                            AppRoute.PostCreate(
                                editPostId = item.postId.toString(),
                                title = item.title,
                                content = item.content,
                                board = item.board.orEmpty(),
                                imageResourcesJson = encodeRouteStringList(item.imageResources),
                                videoResourcesJson = encodeRouteStringList(item.videoResources),
                            )
                        )
                    },
                )
            }
            composable(NavRoutes.PublishPostShortcut) {
                PublishPostShortcutView(onOpenPostCreate = { dispatcher.navigate(AppRoute.PostCreate()) })
            }
            composable(NavRoutes.MyFocus) {
                MyFocusView(
                    onBackClick = dispatcher::back,
                    onOpenOtherUserProfile = { userId -> dispatcher.navigate(AppRoute.OtherUserProfile(userId)) },
                )
            }
            composable(NavRoutes.OtherUserProfile) {
                OtherUserProfile(
                    userId = NavigationPayloadStore.otherUserProfileUserId,
                    onOpenOtherUserProfile = { userId -> dispatcher.navigate(AppRoute.OtherUserProfile(userId)) },
                    onSelfProfileRequested = { dispatcher.navigate(AppRoute.Home(4)) },
                )
            }
            composable(NavRoutes.Wallet) {
                WalletView(
                    onBackClick = dispatcher::back,
                    onRechargeClick = { dispatcher.navigate(AppRoute.PointRecharge) },
                    onWithdrawClick = { ToastUtil.show("暂未开放提现") },
                    onRecordClick = { record ->
                        dispatcher.navigate(
                            AppRoute.WalletDealDetail(
                                dealId = record.id,
                                amount = record.amount,
                                action = record.action,
                                date = record.date,
                            )
                        )
                    },
                )
            }
            composable(NavRoutes.PointRecharge) {
                PointRechargeView(onBackClick = dispatcher::back)
            }
            composable(NavRoutes.WalletDealDetail) {
                val state = NavigationPayloadStore.walletDealDetailState
                DealDetailView(
                    onBackClick = dispatcher::back,
                    onRecordClick = { dispatcher.back() },
                    onQuestionClick = { ToastUtil.show("帮助中心暂未接入") },
                    onContactClick = { ToastUtil.show("客服暂未接入") },
                    viewModel = vortexaViewModel(key = "wallet-deal-${state.dealId.orEmpty()}") {
                        com.vortexa.ui.page.wallet.DealDetailViewModel(state)
                    },
                )
            }
            composable(NavRoutes.MyClass) {
                MyClassView(
                    onBackClick = dispatcher::back,
                    onOpenClassAssistant = { reserveId, roleQuery ->
                        dispatcher.navigate(AppRoute.ClassAssistant(reserveId, roleQuery.orEmpty()))
                    },
                    onOpenOrderDetail = { reserveId ->
                        dispatcher.navigate(AppRoute.OrderDetail(reserveId))
                    },
                )
            }
            composable(NavRoutes.TeacherProfile) {
                TeacherProfileView(
                    teacherId = NavigationPayloadStore.teacherProfileTeacherId,
                    onBackClick = dispatcher::back,
                    onProfileClick = { userId -> dispatcher.navigate(AppRoute.OtherUserProfile(userId)) },
                    onScheduleClick = { teacherId -> dispatcher.navigate(AppRoute.Schedule(teacherId)) },
                )
            }
            composable(NavRoutes.Schedule) {
                ScheduleView(
                    teacherId = NavigationPayloadStore.scheduleTeacherId,
                    onPayConfirmClick = { teacherId, reserveDate, reserveHour ->
                        dispatcher.navigate(AppRoute.ScheduleConfirm(teacherId, reserveDate, reserveHour))
                    },
                )
            }
            composable(NavRoutes.ScheduleConfirm) {
                ScheduleConfirmRoute(
                    teacherId = NavigationPayloadStore.scheduleConfirmTeacherId,
                    reserveDate = NavigationPayloadStore.scheduleConfirmReserveDate,
                    reserveHour = NavigationPayloadStore.scheduleConfirmReserveHour,
                    onBackClick = dispatcher::back,
                    onModifyClick = dispatcher::back,
                    onConfirmClick = {
                        dispatcher.navigate(
                            AppRoute.SchedulePayConfirm(
                                teacherId = NavigationPayloadStore.scheduleConfirmTeacherId,
                                reserveDate = NavigationPayloadStore.scheduleConfirmReserveDate,
                                reserveHour = NavigationPayloadStore.scheduleConfirmReserveHour,
                            )
                        )
                    },
                )
            }
            composable(NavRoutes.SchedulePayConfirm) {
                SchedulePayConfirmRoute(
                    teacherId = NavigationPayloadStore.schedulePayConfirmTeacherId,
                    reserveDate = NavigationPayloadStore.schedulePayConfirmReserveDate,
                    reserveHour = NavigationPayloadStore.schedulePayConfirmReserveHour,
                    onBackClick = dispatcher::back,
                    onPaySuccess = { reserveId -> dispatcher.navigate(AppRoute.OrderDetail(reserveId)) },
                )
            }
            composable(NavRoutes.ClassAssistant) {
                ClassAssistantRoute(
                    reserveId = NavigationPayloadStore.classAssistantReserveId,
                    roleQuery = NavigationPayloadStore.classAssistantRoleQuery,
                    onBackClick = dispatcher::back,
                    onAcceptedOpenOrderDetail = { reserveId -> dispatcher.navigate(AppRoute.OrderDetail(reserveId)) },
                    onRebookClick = { teacherId -> dispatcher.navigate(AppRoute.Schedule(teacherId)) },
                )
            }
            composable(NavRoutes.OrderDetail) {
                OrderDetailRoute(
                    reserveId = NavigationPayloadStore.orderDetailReserveId,
                    onBackClick = dispatcher::back,
                    onTeacherProfileClick = { teacherId -> dispatcher.navigate(AppRoute.TeacherProfile(teacherId)) },
                    onRebookClick = { teacherId -> dispatcher.navigate(AppRoute.Schedule(teacherId)) },
                    onCourseEntryClick = { ui ->
                        dispatcher.navigate(
                            AppRoute.VideoRtc(
                                channelName = ui.channelName.orEmpty(),
                                teacherId = ui.teacherId,
                                courseStartMs = ui.courseStartEpochMilli(),
                                courseEndMs = ui.courseEndEpochMilli(),
                            )
                        )
                    },
                    onClosedAfterCancel = dispatcher::back,
                )
            }
            composable(NavRoutes.VideoRtc) {
                VideoRtcRoute(
                    channelName = NavigationPayloadStore.videoRtcChannelName,
                    courseTeacherId = NavigationPayloadStore.videoRtcTeacherId,
                    courseStartTimeMs = NavigationPayloadStore.videoRtcCourseStartMs,
                    courseEndTimeMs = NavigationPayloadStore.videoRtcCourseEndMs,
                    onClose = dispatcher::back,
                )
            }
            composable(NavRoutes.ImagePreview) {
                ImagePreviewView(
                    imageUrls = NavigationPayloadStore.imagePreviewUrls,
                    initialIndex = NavigationPayloadStore.imagePreviewInitialIndex,
                    onBack = dispatcher::back,
                )
            }
            composable(NavRoutes.ProfileSubPage) {
                val kind = NavigationPayloadStore.profileSubPageKind
                when (kind) {
                    ProfileSubPageKind.Collection -> CollectionView(onBackClick = dispatcher::back)
                    ProfileSubPageKind.History -> HistoryView(onBackClick = dispatcher::back)
                    ProfileSubPageKind.Interaction -> InteractionView(onBackClick = dispatcher::back)
                    ProfileSubPageKind.Focus -> MyFocusView(onBackClick = dispatcher::back)
                }
            }
        }
    }
}

@Composable
private fun ScheduleConfirmRoute(
    teacherId: Long,
    reserveDate: String,
    reserveHour: String,
    onBackClick: () -> Unit,
    onModifyClick: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    val viewModel = vortexaViewModel(key = "schedule-confirm-$teacherId-$reserveDate-$reserveHour") {
        Confirm2ViewModel(teacherId, reserveDate, reserveHour)
    }
    val teacherName by viewModel.teacherDisplayName.collectAsState()
    val teacherAvatarUrl by viewModel.teacherAvatarUrl.collectAsState()
    val guideFee by viewModel.guideFeeText.collectAsState()
    val totalPoints by viewModel.totalPointsText.collectAsState()

    ConfirmView(
        reserveDate = reserveDate,
        reserveHour = reserveHour,
        teacherName = teacherName.ifBlank { "导师" },
        teacherAvatarUrl = teacherAvatarUrl,
        orderPriceText = guideFee,
        payAmountText = totalPoints,
        onBackClick = onBackClick,
        onModifyClick = onModifyClick,
        onConfirmClick = onConfirmClick,
    )
}

@Composable
private fun SchedulePayConfirmRoute(
    teacherId: Long,
    reserveDate: String,
    reserveHour: String,
    onBackClick: () -> Unit,
    onPaySuccess: (Int) -> Unit,
) {
    val viewModel = vortexaViewModel(key = "schedule-pay-$teacherId-$reserveDate-$reserveHour") {
        Confirm2ViewModel(teacherId, reserveDate, reserveHour)
    }
    val teacherName by viewModel.teacherDisplayName.collectAsState()
    val teacherAvatarUrl by viewModel.teacherAvatarUrl.collectAsState()
    val guideFee by viewModel.guideFeeText.collectAsState()
    val balancePoints by viewModel.balancePointsText.collectAsState()
    val totalPoints by viewModel.totalPointsText.collectAsState()
    val payLoading by viewModel.payLoading.collectAsState()
    val reserveSuccessReserveId by viewModel.reserveSuccessReserveId.collectAsState()

    LaunchedEffect(reserveSuccessReserveId) {
        val reserveId = reserveSuccessReserveId ?: return@LaunchedEffect
        viewModel.clearReserveSuccess()
        onPaySuccess(reserveId.toInt())
    }

    PayConfirmView(
        teacherName = teacherName.ifBlank { "导师" },
        teacherAvatarUrl = teacherAvatarUrl,
        courseStartTime = buildCourseStartTime(reserveDate, reserveHour),
        durationText = buildDurationText(reserveHour),
        guideFee = guideFee,
        balancePoints = balancePoints,
        totalPoints = totalPoints,
        payLoading = payLoading,
        onBackClick = onBackClick,
        onPayClick = viewModel::reserve,
    )
}

private fun buildCourseStartTime(reserveDate: String, reserveHour: String): String {
    val start = reserveHour.substringBefore("-").trim().ifBlank { "18:00" }
    return "${reserveDate.ifBlank { "2025-10-08" }} $start"
}

private fun buildDurationText(reserveHour: String): String {
    val parts = reserveHour.split("-")
    if (parts.size != 2) return "1小时"
    val start = parts[0].trim().substringBefore(":").toIntOrNull() ?: 0
    val end = parts[1].trim().substringBefore(":").toIntOrNull() ?: 0
    return "${(end - start).coerceAtLeast(1)}小时"
}

private fun NavController.replaceRoot(route: AppRoute) {
    navigate(route.toNavRoute()) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

private object NavRoutes {
    const val Splash = "splash"
    const val Home = "home"
    const val Login = "login"
    const val Register = "register"
    const val ForgetPassword = "forgetPassword"
    const val Search = "search"
    const val SearchResult = "searchResult"
    const val PostDetail = "postDetail"
    const val HotPostList = "hotPostList"
    const val PostCreate = "postCreate"
    const val ImagePreview = "imagePreview"
    const val ProfileSubPage = "profileSubPage"
    const val CreatorCenter = "creatorCenter"
    const val DataCenter = "dataCenter"
    const val SystemMessage = "systemMessage"
    const val MyFocus = "myFocus"
    const val OtherUserProfile = "otherUserProfile"
    const val PaperManagement = "paperManagement"
    const val PublishPostShortcut = "publishPostShortcut"
    const val MyClass = "myClass"
    const val TeacherProfile = "teacherProfile"
    const val Schedule = "schedule"
    const val ScheduleConfirm = "scheduleConfirm"
    const val SchedulePayConfirm = "schedulePayConfirm"
    const val ClassAssistant = "classAssistant"
    const val OrderDetail = "orderDetail"
    const val VideoRtc = "videoRtc"
    const val Wallet = "wallet"
    const val PointRecharge = "pointRecharge"
    const val WalletDealDetail = "walletDealDetail"
}

private object NavigationPayloadStore {
    var homeTab: Int = 0
    var searchKeyword: String = ""
    var postId: String = ""
    var openReplyComposer: Boolean = false
    var replyComposerHint: PostDetailReplyComposerHint? = null
    var imagePreviewUrls: List<String> = emptyList()
    var imagePreviewInitialIndex: Int = 0
    var profileSubPageKind: ProfileSubPageKind = ProfileSubPageKind.Collection
    var postCreateEditArgs: PostCreateEditArgs? = null
    var systemMessageType: Int = 0
    var systemMessageMarkReadDialogId: Long? = null
    var systemMessageMarkReadMessageId: Long? = null
    var otherUserProfileUserId: Long = 0L
    var teacherProfileTeacherId: Long = 0L
    var scheduleTeacherId: Long = 0L
    var scheduleConfirmTeacherId: Long = 0L
    var scheduleConfirmReserveDate: String = ""
    var scheduleConfirmReserveHour: String = ""
    var schedulePayConfirmTeacherId: Long = 0L
    var schedulePayConfirmReserveDate: String = ""
    var schedulePayConfirmReserveHour: String = ""
    var classAssistantReserveId: Int = 0
    var classAssistantRoleQuery: String = ""
    var orderDetailReserveId: Int = 0
    var videoRtcChannelName: String = ""
    var videoRtcTeacherId: Long = 0L
    var videoRtcCourseStartMs: Long? = null
    var videoRtcCourseEndMs: Long? = null
    var walletDealDetailState: DealDetailState = DealDetailState()
}

private fun AppRoute.toNavRoute(): String = when (this) {
    AppRoute.Splash -> NavRoutes.Splash
    is AppRoute.Home -> {
        NavigationPayloadStore.homeTab = tab
        NavRoutes.Home
    }
    AppRoute.Login -> NavRoutes.Login
    AppRoute.Register -> NavRoutes.Register
    AppRoute.ForgetPassword -> NavRoutes.ForgetPassword
    AppRoute.Search -> NavRoutes.Search
    is AppRoute.SearchResult -> {
        NavigationPayloadStore.searchKeyword = keyword
        NavRoutes.SearchResult
    }
    is AppRoute.PostDetail -> {
        NavigationPayloadStore.postId = postId
        NavigationPayloadStore.openReplyComposer = openReplyComposer
        NavigationPayloadStore.replyComposerHint = replyCommentId?.let {
            PostDetailReplyComposerHint(
                commentId = it,
                authorName = replyAuthorName,
                commentSnippet = replyCommentSnippet,
                authorAvatar = replyAuthorAvatar,
            )
        }
        NavRoutes.PostDetail
    }
    AppRoute.HotPostList -> NavRoutes.HotPostList
    is AppRoute.PostCreate -> {
        NavigationPayloadStore.postCreateEditArgs = editPostId?.takeIf { it.isNotBlank() }?.let {
            PostCreateEditArgs(
                postId = it,
                title = title,
                content = content,
                board = board.takeIf { value -> value.isNotBlank() },
                imageResources = imageResources(),
                videoResources = videoResources()
            )
        }
        NavRoutes.PostCreate
    }
    is AppRoute.ImagePreview -> {
        NavigationPayloadStore.imagePreviewUrls = urls()
        NavigationPayloadStore.imagePreviewInitialIndex = initialIndex
        NavRoutes.ImagePreview
    }
    is AppRoute.ProfileSubPage -> {
        NavigationPayloadStore.profileSubPageKind = kind
        NavRoutes.ProfileSubPage
    }
    AppRoute.CreatorCenter -> NavRoutes.CreatorCenter
    AppRoute.DataCenter -> NavRoutes.DataCenter
    is AppRoute.SystemMessage -> {
        NavigationPayloadStore.systemMessageType = messageType
        NavigationPayloadStore.systemMessageMarkReadDialogId = markReadDialogId
        NavigationPayloadStore.systemMessageMarkReadMessageId = markReadMessageId
        NavRoutes.SystemMessage
    }
    is AppRoute.OtherUserProfile -> {
        NavigationPayloadStore.otherUserProfileUserId = userId
        NavRoutes.OtherUserProfile
    }
    AppRoute.PaperManagement -> NavRoutes.PaperManagement
    AppRoute.PublishPostShortcut -> NavRoutes.PublishPostShortcut
    AppRoute.MyClass -> NavRoutes.MyClass
    is AppRoute.TeacherProfile -> {
        NavigationPayloadStore.teacherProfileTeacherId = teacherId
        NavRoutes.TeacherProfile
    }
    is AppRoute.Schedule -> {
        NavigationPayloadStore.scheduleTeacherId = teacherId
        NavRoutes.Schedule
    }
    is AppRoute.ScheduleConfirm -> {
        NavigationPayloadStore.scheduleConfirmTeacherId = teacherId
        NavigationPayloadStore.scheduleConfirmReserveDate = reserveDate
        NavigationPayloadStore.scheduleConfirmReserveHour = reserveHour
        NavRoutes.ScheduleConfirm
    }
    is AppRoute.SchedulePayConfirm -> {
        NavigationPayloadStore.schedulePayConfirmTeacherId = teacherId
        NavigationPayloadStore.schedulePayConfirmReserveDate = reserveDate
        NavigationPayloadStore.schedulePayConfirmReserveHour = reserveHour
        NavRoutes.SchedulePayConfirm
    }
    is AppRoute.ClassAssistant -> {
        NavigationPayloadStore.classAssistantReserveId = reserveId
        NavigationPayloadStore.classAssistantRoleQuery = roleQuery
        NavRoutes.ClassAssistant
    }
    is AppRoute.OrderDetail -> {
        NavigationPayloadStore.orderDetailReserveId = reserveId
        NavRoutes.OrderDetail
    }
    is AppRoute.VideoRtc -> {
        NavigationPayloadStore.videoRtcChannelName = channelName
        NavigationPayloadStore.videoRtcTeacherId = teacherId
        NavigationPayloadStore.videoRtcCourseStartMs = courseStartMs
        NavigationPayloadStore.videoRtcCourseEndMs = courseEndMs
        NavRoutes.VideoRtc
    }
    AppRoute.Wallet -> NavRoutes.Wallet
    AppRoute.PointRecharge -> NavRoutes.PointRecharge
    is AppRoute.WalletDealDetail -> {
        NavigationPayloadStore.walletDealDetailState = DealDetailState(
            dealId = dealId.takeIf { it.isNotBlank() },
            statusText = "支付成功",
            amountDisplay = amount.ifBlank { "$100" },
            detailRows = if (dealId.isNotBlank()) {
                listOf(
                    "当前状态" to "完成",
                    "订单金额" to amount.ifBlank { "120.00" },
                    "优惠折扣" to "-20.00",
                    "支付时间" to "${date.ifBlank { "2025-10-08" }} 16:00",
                    "支付方式" to action.ifBlank { "支付宝" },
                    "商品说明" to "积分充值",
                    "订单号" to dealId,
                )
            } else {
                defaultDetailRows()
            }
        )
        NavRoutes.WalletDealDetail
    }
}
