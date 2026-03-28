package com.subnetik.unlock.presentation.screens.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.graphics.vector.ImageVector

enum class AdminSection(
    val title: String,
    val icon: ImageVector,
) {
    LEADS("Заявки", Icons.Default.Inbox),
    SUPPORT("Support", Icons.Default.SupportAgent),
    REVIEWS("Отзывы", Icons.Default.RateReview),
    PROMOCODES("Промокоды", Icons.Default.LocalOffer),
    BLOG("Блог", Icons.AutoMirrored.Filled.Article),
    USERS("Пользователи", Icons.Default.People),
    GROUPS("Учетная таблица", Icons.Default.TableChart),
    TOKENS("Токены", Icons.Default.Storefront),
    MARKET("Маркет", Icons.Default.Storefront),
    LESSONS("Уроки Live", Icons.Default.Videocam),
    HOMEWORK("Дом.задания", Icons.AutoMirrored.Filled.Assignment),
    NOTIFICATIONS("Уведомления", Icons.Default.Notifications),
    RECEIPTS("Квитанции", Icons.Default.Receipt),
    CALENDAR("Календарь", Icons.Default.CalendarMonth),
}
