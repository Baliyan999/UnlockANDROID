package com.subnetik.unlock.presentation.screens.admin

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.presentation.screens.admin.components.*
import com.subnetik.unlock.presentation.screens.admin.sections.*
import com.subnetik.unlock.presentation.theme.Brand

@Composable
fun AdminPanelScreen(
    viewModel: AdminPanelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // "Админ панель" header with refresh
            AdminPanelHeader(
                isDark = isDark,
                onRefresh = { viewModel.refresh() },
            )

            // Horizontal scrollable section tabs
            AdminSectionTabs(
                selectedSection = uiState.selectedSection,
                onSectionSelected = { viewModel.selectSection(it) },
                isDark = isDark,
            )

            Spacer(Modifier.height(Brand.Spacing.sm))

            // Content area
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState.selectedSection) {
                    AdminSection.LEADS -> AdminLeadsSection(isDark = isDark)
                    AdminSection.SUPPORT -> AdminSupportSection(isDark = isDark)
                    AdminSection.REVIEWS -> AdminReviewsSection(isDark = isDark)
                    AdminSection.PROMOCODES -> AdminPromocodesSection(isDark = isDark)
                    AdminSection.BLOG -> AdminBlogSection(isDark = isDark)
                    AdminSection.USERS -> AdminUsersSection(isDark = isDark)
                    AdminSection.GROUPS -> AdminGroupsSection(isDark = isDark)
                    AdminSection.TOKENS -> AdminTokensSection(isDark = isDark)
                    AdminSection.MARKET -> AdminMarketSection(isDark = isDark)
                    AdminSection.LESSONS -> AdminLessonsSection(isDark = isDark)
                    AdminSection.HOMEWORK -> AdminHomeworkSection(isDark = isDark)
                    AdminSection.NOTIFICATIONS -> AdminNotificationsSection(isDark = isDark)
                    AdminSection.RECEIPTS -> AdminReceiptsSection(isDark = isDark, refreshTrigger = uiState.refreshTrigger)
                    AdminSection.CALENDAR -> AdminCalendarSection(isDark = isDark, refreshTrigger = uiState.refreshTrigger)
                }
            }
        }
    }
}
