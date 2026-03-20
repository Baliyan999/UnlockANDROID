package com.subnetik.unlock.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.subnetik.unlock.domain.model.AppUserRole

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

fun getBottomNavItems(role: AppUserRole): List<BottomNavItem> {
    val home = BottomNavItem(
        route = Routes.Home.route,
        label = "Главная",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    )

    val homework = when (role) {
        AppUserRole.STUDENT -> BottomNavItem(
            route = Routes.Homework.route,
            label = "Учебный кабинет",
            selectedIcon = Icons.Filled.Book,
            unselectedIcon = Icons.Outlined.Book,
        )
        AppUserRole.TEACHER -> BottomNavItem(
            route = Routes.Homework.route,
            label = "Мои группы",
            selectedIcon = Icons.Filled.Groups,
            unselectedIcon = Icons.Outlined.Groups,
        )
        AppUserRole.ADMIN -> BottomNavItem(
            route = Routes.Homework.route,
            label = "Управление",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
        )
        else -> BottomNavItem(
            route = Routes.Homework.route,
            label = "Кабинет",
            selectedIcon = Icons.Filled.Book,
            unselectedIcon = Icons.Outlined.Book,
        )
    }

    val test = BottomNavItem(
        route = Routes.Test.route,
        label = "Тест",
        selectedIcon = Icons.Filled.Quiz,
        unselectedIcon = Icons.Outlined.Quiz,
    )

    val profile = BottomNavItem(
        route = Routes.Profile.route,
        label = "Профиль",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
    )

    return when (role) {
        AppUserRole.ADMIN -> listOf(home, homework, profile)
        else -> listOf(home, homework, test, profile)
    }
}
