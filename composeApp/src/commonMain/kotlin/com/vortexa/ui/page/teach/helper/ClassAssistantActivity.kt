package com.vortexa.ui.page.teach.helper

import androidx.compose.runtime.Composable
import com.vortexa.navigation.AppRoute
import com.vortexa.navigation.NavigationRouteBridge
import com.vortexa.ui.theme.BaseTheme
import com.vortexa.ui.viewmodel.vortexaViewModel

object ClassAssistantActivity {
    const val EXTRA_RESERVE_ID: String = "reserve_id"
    const val EXTRA_ROLE: String = "role"

    fun start(context: Any?, reserveId: Int, roleQuery: String? = null) {
        NavigationRouteBridge.navigate(AppRoute.ClassAssistant(reserveId, roleQuery.orEmpty()))
    }
}

@Composable
fun ClassAssistantRoute(
    reserveId: Int,
    roleQuery: String? = null,
    onBackClick: () -> Unit = {},
    onClosedAfterCancel: () -> Unit = {},
    onAcceptedOpenOrderDetail: (reserveId: Int) -> Unit = {},
    onRebookClick: (teacherId: Long) -> Unit = {},
    viewModel: ClassAssistantViewModel = vortexaViewModel(
        key = "class-assistant-$reserveId-${roleQuery.orEmpty()}"
    ) {
        ClassAssistantViewModel(
            reserveId = reserveId,
            roleOverride = ClassAssistantRoleScheme.parse(roleQuery)
        )
    }
) {
    BaseTheme(belowStatusBar = true, aboveNavigationBar = true) {
        ClassAssistantView(
            viewModel = viewModel,
            onBackClick = onBackClick,
            onClosedAfterCancel = onClosedAfterCancel,
            onAcceptedOpenOrderDetail = onAcceptedOpenOrderDetail,
            onRebookClick = onRebookClick
        )
    }
}
