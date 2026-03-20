package com.subnetik.unlock.presentation.screens.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subnetik.unlock.presentation.screens.admin.AdminSection
import com.subnetik.unlock.presentation.theme.*

// ─── Admin Background ────────────────────────────────────────

@Composable
fun AdminBackground(isDark: Boolean) {
    val bgTop = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC)
    val bgBottom = if (isDark) Color(0xFF1A1E33) else Color(0xFFE6EDF8)
    val glowBlue = if (isDark) BrandBlue.copy(alpha = 0.18f) else BrandBlue.copy(alpha = 0.10f)
    val glowPurple = if (isDark) Color(0xFF8552DB).copy(alpha = 0.18f) else Color(0xFF8552DB).copy(alpha = 0.10f)

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(bgTop, bgBottom))),
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = (-70).dp, y = (-60).dp)
                .background(glowBlue, CircleShape),
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 40.dp)
                .background(glowPurple, CircleShape),
        )
    }
}

// ─── Admin Glass Card ────────────────────────────────────────

@Composable
fun AdminGlassCard(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val cardBg = if (isDark) Color(0xFF171E33).copy(alpha = 0.96f) else Color.White.copy(alpha = 0.98f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, strokeColor, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        onClick = onClick ?: {},
        enabled = onClick != null,
    ) {
        content()
    }
}

// ─── Admin Panel Header ──────────────────────────────────────

@Composable
fun AdminPanelHeader(
    isDark: Boolean,
    isLoading: Boolean = false,
    onRefresh: () -> Unit,
) {
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                "Админ панель",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
        }
        IconButton(onClick = onRefresh, enabled = !isLoading) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = subtextColor,
                )
            } else {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Обновить",
                    tint = subtextColor,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

// ─── Section Tabs (Horizontal Scrollable) ────────────────────

@Composable
fun AdminSectionTabs(
    selectedSection: AdminSection,
    onSectionSelected: (AdminSection) -> Unit,
    isDark: Boolean,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Brand.Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
    ) {
        items(AdminSection.entries.toList()) { section ->
            AdminTabChip(
                section = section,
                isSelected = section == selectedSection,
                onClick = { onSectionSelected(section) },
                isDark = isDark,
            )
        }
    }
}

@Composable
private fun AdminTabChip(
    section: AdminSection,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean,
) {
    val textColor = when {
        isSelected -> Color.White
        isDark -> Color.White.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val bgModifier = if (isSelected) {
        Modifier.background(
            Brush.horizontalGradient(GradientIndigo),
            Brand.Shapes.full,
        )
    } else {
        val bg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
        val stroke = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
        Modifier
            .background(bg, Brand.Shapes.full)
            .border(1.dp, stroke, Brand.Shapes.full)
    }

    Surface(
        onClick = onClick,
        shape = Brand.Shapes.full,
        color = Color.Transparent,
    ) {
        Row(
            modifier = bgModifier
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                section.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
            )
        }
    }
}

// ─── Filter Tabs ─────────────────────────────────────────────

data class AdminFilter<T>(
    val key: T,
    val label: String,
    val count: Int,
)

@Composable
fun <T> AdminFilterTabs(
    filters: List<AdminFilter<T>>,
    selectedFilter: T,
    onFilterSelected: (T) -> Unit,
    isDark: Boolean,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
    ) {
        items(filters, key = { it.label }) { filter ->
            val isSelected = filter.key == selectedFilter
            AdminFilterChip(
                label = filter.label,
                count = filter.count,
                isSelected = isSelected,
                onClick = { onFilterSelected(filter.key) },
                isDark = isDark,
            )
        }
    }
}

@Composable
private fun AdminFilterChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean,
) {
    val textColor = if (isSelected) Color.White else {
        if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        shape = Brand.Shapes.full,
        color = Color.Transparent,
    ) {
        Row(
            modifier = (if (isSelected) {
                Modifier.background(
                    Brush.horizontalGradient(GradientIndigo),
                    Brand.Shapes.full,
                )
            } else {
                val bg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
                val stroke = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                Modifier
                    .background(bg, Brand.Shapes.full)
                    .border(1.dp, stroke, Brand.Shapes.full)
            }).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "$label $count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
            )
        }
    }
}

// ─── Status Tag ──────────────────────────────────────────────

@Composable
fun AdminStatusTag(
    text: String,
    color: Color,
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

fun statusColor(status: String): Color = when (status.lowercase()) {
    "pending" -> BrandGold
    "processed", "published", "active", "approved" -> BrandTeal
    "deleted", "rejected", "inactive", "cancelled" -> BrandCoral
    "draft" -> BrandIndigo
    else -> BrandBlue
}

fun statusLabel(status: String): String = when (status.lowercase()) {
    "pending" -> "Ожидает"
    "processed" -> "Обработана"
    "deleted" -> "Удалена"
    "published" -> "Опубликован"
    "rejected" -> "Отклонён"
    "active" -> "Активен"
    "inactive" -> "Неактивен"
    "cancelled" -> "Отменён"
    "approved" -> "Подтверждён"
    "draft" -> "Черновик"
    else -> status
}

// ─── Section Header ──────────────────────────────────────────

@Composable
fun AdminSectionHeader(
    icon: ImageVector,
    title: String,
    isDark: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = BrandGreen,
        )
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ─── Empty State ─────────────────────────────────────────────

@Composable
fun AdminEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    isDark: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Brand.Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = if (isDark) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
        )
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = if (isDark) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
    }
}

// ─── Action Button ───────────────────────────────────────────

@Composable
fun AdminActionButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        enabled = enabled,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = color,
            )
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color,
            )
        }
    }
}

// ─── Error Banner ───────────────────────────────────────────

@Composable
fun AdminErrorBanner(
    error: String,
    isDark: Boolean,
    onRetry: () -> Unit,
) {
    AdminGlassCard(isDark = isDark) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Brand.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = BrandCoral,
                modifier = Modifier.size(20.dp),
            )
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = BrandCoral,
                modifier = Modifier.weight(1f),
                maxLines = 3,
            )
            Surface(
                onClick = onRetry,
                shape = RoundedCornerShape(8.dp),
                color = BrandBlue.copy(alpha = 0.12f),
            ) {
                Text(
                    "Повторить",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandBlue,
                )
            }
        }
    }
}

// ─── Coming Soon Placeholder ─────────────────────────────────

@Composable
fun AdminComingSoonSection(
    section: AdminSection,
    isDark: Boolean,
) {
    AdminEmptyState(
        icon = Icons.Default.Construction,
        title = section.title,
        description = "Раздел в разработке",
        isDark = isDark,
    )
}
