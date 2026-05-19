package com.vortexa.ui.page.teach.schedule

import com.vortexa.navigation.AppRoute
import com.vortexa.navigation.NavigationRouteBridge

object ScheduleActivity {
    fun start(context: Any?, teacherId: Long) {
        NavigationRouteBridge.navigate(AppRoute.Schedule(teacherId))
    }
}
