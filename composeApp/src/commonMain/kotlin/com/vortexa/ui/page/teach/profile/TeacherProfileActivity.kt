package com.vortexa.ui.page.teach.profile

import com.vortexa.navigation.AppRoute
import com.vortexa.navigation.NavigationRouteBridge

object TeacherProfileActivity {
    fun start(context: Any?, teacherId: Long) {
        NavigationRouteBridge.navigate(AppRoute.TeacherProfile(teacherId))
    }
}
