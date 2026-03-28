package com.subnetik.unlock.presentation.screens.terms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*

data class TermsDocument(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val tint: Color,
    val text: String,
)

/**
 * Student terms screen - shows 5 general documents.
 */
@Composable
fun StudentTermsScreen(onAccept: () -> Unit) {
    val documents = listOf(
        TermsDocument("contract", "Договор учащегося", "Условия обучения, права и обязанности", Icons.Default.Draw, BrandBlue,
            "ДОГОВОР УЧАЩЕГОСЯ\n\nШкола китайского языка Unlock Language Studio\nг. Ташкент, Узбекистан\n\n1. ПРЕДМЕТ ДОГОВОРА\n\n1.1. Школа обязуется предоставить Учащемуся образовательные услуги по обучению китайскому языку.\n1.2. Учащийся обязуется своевременно посещать занятия и оплачивать обучение.\n\n2. ПРАВА И ОБЯЗАННОСТИ СТОРОН\n\n2.1. Школа обязуется обеспечить качественное проведение занятий.\n2.2. Учащийся обязуется посещать занятия и выполнять домашние задания.\n\nДокумент является заглушкой и будет заменён на полную версию."),
        TermsDocument("offer", "Публичная оферта", "Условия предоставления услуг", Icons.Default.Description, BrandIndigo,
            "ПУБЛИЧНАЯ ОФЕРТА\n\nна оказание образовательных услуг\nUnlock Language Studio\n\n1. ОБЩИЕ ПОЛОЖЕНИЯ\n\n1.1. Настоящий документ является официальной публичной офертой.\n1.2. Акцептом является принятие условий в мобильном приложении.\n\nДокумент является заглушкой и будет заменён на полную версию."),
        TermsDocument("privacy", "Политика конфиденциальности", "Обработка и защита данных", Icons.Default.Shield, BrandTeal,
            "ПОЛИТИКА КОНФИДЕНЦИАЛЬНОСТИ\n\nUnlock Language Studio\n\n1. СБОР ИНФОРМАЦИИ\n\nМы собираем имя, контактные данные, данные об успеваемости и устройстве.\n\n2. ИСПОЛЬЗОВАНИЕ\n\nДля предоставления образовательных услуг и улучшения качества.\n\nДокумент является заглушкой и будет заменён на полную версию."),
        TermsDocument("personalData", "Согласие на обработку данных", "Персональные данные", Icons.Default.Person, BrandCoral,
            "СОГЛАСИЕ НА ОБРАБОТКУ ПЕРСОНАЛЬНЫХ ДАННЫХ\n\nВ соответствии с Законом РУз «О персональных данных».\n\nНастоящее согласие распространяется на ФИО, email, телефон, данные об успеваемости.\n\nДокумент является заглушкой и будет заменён на полную версию."),
        TermsDocument("termsOfUse", "Правила пользования приложением", "Условия использования Unlock", Icons.Default.PhoneAndroid, BrandGold,
            "ПРАВИЛА ПОЛЬЗОВАНИЯ ПРИЛОЖЕНИЕМ UNLOCK\n\n1. Для доступа необходима регистрация.\n2. Одна учётная запись — один пользователь.\n3. Токены не являются деньгами.\n\nДокумент является заглушкой и будет заменён на полную версию."),
    )

    TermsAcceptanceScreen(
        title = "Условия обучения",
        subtitle = "Пожалуйста, ознакомьтесь с документами\nи подтвердите своё согласие",
        icon = Icons.Default.Description,
        iconTint = BrandBlue,
        documents = documents,
        onAccept = onAccept,
    )
}

/**
 * Teacher terms screen - shows 6 employment documents.
 */
@Composable
fun TeacherTermsScreen(onAccept: () -> Unit) {
    val documents = listOf(
        TermsDocument("employment", "Трудовой договор", "Условия работы и оплата труда", Icons.Default.Draw, BrandBlue,
            "ТРУДОВОЙ ДОГОВОР\n\nИП «Unlock Language Studio»\nг. Ташкент\n\n1. ПРЕДМЕТ ДОГОВОРА\n\nРаботодатель принимает Работника на должность преподавателя.\n\n2. УСЛОВИЯ ТРУДА\n\nМесто работы: г. Ташкент / дистанционно.\n\nДокумент является заглушкой и будет заменён на полную версию."),
        TermsDocument("jobDesc", "Должностная инструкция", "Обязанности преподавателя", Icons.Default.Checklist, BrandIndigo,
            "ДОЛЖНОСТНАЯ ИНСТРУКЦИЯ\n\nПреподаватель китайского языка\n\n1. Проведение занятий по расписанию.\n2. Проверка домашних заданий.\n3. Ведение учёта посещаемости.\n\nДокумент является заглушкой и будет заменён на полную версию."),
        TermsDocument("nda", "Соглашение о неразглашении (NDA)", "Конфиденциальность данных", Icons.Default.Lock, BrandCoral,
            "СОГЛАШЕНИЕ О НЕРАЗГЛАШЕНИИ (NDA)\n\n1. Работник обязуется не разглашать конфиденциальную информацию.\n2. К конфиденциальной относятся: методики, данные учащихся, финансы.\n3. Срок: 2 года после увольнения.\n\nДокумент является заглушкой и будет заменён на полную версию."),
        TermsDocument("internalRules", "Внутренний трудовой распорядок", "Правила и дисциплина", Icons.Default.Business, BrandTeal,
            "ПОЛОЖЕНИЕ О ВНУТРЕННЕМ ТРУДОВОМ РАСПОРЯДКЕ\n\n1. Преподаватель обязан быть на рабочем месте за 10 минут до занятия.\n2. Проверять ДЗ в течение 48 часов.\n\nДокумент является заглушкой и будет заменён на полную версию."),
        TermsDocument("imageConsent", "Согласие на использование изображения", "Фото и видео", Icons.Default.CameraAlt, BrandGold,
            "СОГЛАСИЕ НА ИСПОЛЬЗОВАНИЕ ИЗОБРАЖЕНИЯ\n\nДля размещения на сайте, в приложении и соцсетях школы.\nМожет быть отозвано письменным заявлением.\n\nДокумент является заглушкой и будет заменён на полную версию."),
        TermsDocument("staffAppRules", "Правила для сотрудников", "Доступ к данным учеников", Icons.Default.PhoneAndroid, BrandGreen,
            "ПРАВИЛА ПОЛЬЗОВАНИЯ ПРИЛОЖЕНИЕМ (для сотрудников)\n\n1. Не передавать логин и пароль.\n2. Доступ только к данным своих учащихся.\n3. Запрещается экспортировать данные.\n\nДокумент является заглушкой и будет заменён на полную версию."),
    )

    TermsAcceptanceScreen(
        title = "Трудовые документы",
        subtitle = "Ознакомьтесь с трудовыми документами\nи подтвердите своё согласие",
        icon = Icons.Default.Work,
        iconTint = BrandTeal,
        documents = documents,
        onAccept = onAccept,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TermsAcceptanceScreen(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    documents: List<TermsDocument>,
    onAccept: () -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val primaryText = MaterialTheme.colorScheme.onSurface
    val secondaryText = MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)

    val agreed = remember { mutableStateMapOf<String, Boolean>() }
    var activeDocument by remember { mutableStateOf<TermsDocument?>(null) }
    val allAgreed = documents.all { agreed[it.id] == true }

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))

            // Header
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = primaryText)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, fontSize = 14.sp, color = secondaryText, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))

            // Document cards
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                documents.forEach { doc ->
                    val isAgreed = agreed[doc.id] == true
                    DocumentCard(
                        doc = doc,
                        isAgreed = isAgreed,
                        onClick = { activeDocument = doc },
                        isDark = isDark,
                    )
                }

                // Info
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BrandGold.copy(alpha = 0.08f),
                ) {
                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BrandGold, modifier = Modifier.size(16.dp))
                        Text(
                            "Откройте каждый документ, прочитайте и поставьте отметку о согласии.",
                            fontSize = 12.sp, color = secondaryText,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Accept button
            Divider(color = secondaryText.copy(alpha = 0.2f))
            Button(
                onClick = onAccept,
                enabled = allAgreed,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp).height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (allAgreed) BrandGreen else Color.Gray.copy(alpha = 0.4f),
                ),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Принять и продолжить", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    // Document viewer bottom sheet
    activeDocument?.let { doc ->
        DocumentViewerDialog(
            doc = doc,
            isAgreed = agreed[doc.id] == true,
            onAgreeToggle = { agreed[doc.id] = it },
            onDismiss = { activeDocument = null },
        )
    }
}

@Composable
private fun DocumentCard(
    doc: TermsDocument,
    isAgreed: Boolean,
    onClick: () -> Unit,
    isDark: Boolean,
) {
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val primaryText = if (isDark) Color.White else Color.Black
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = cardColor,
        border = if (isAgreed) androidx.compose.foundation.BorderStroke(1.5.dp, BrandGreen.copy(alpha = 0.3f)) else null,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp))
                    .background(if (isAgreed) BrandGreen.copy(alpha = 0.15f) else doc.tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isAgreed) Icons.Default.Check else doc.icon,
                    contentDescription = null,
                    tint = if (isAgreed) BrandGreen else doc.tint,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(doc.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = primaryText)
                Text(
                    if (isAgreed) "Ознакомлен(а) и согласен(на)" else doc.description,
                    fontSize = 12.sp,
                    color = if (isAgreed) BrandGreen else secondaryText,
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = secondaryText.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
        }
    }
}

/**
 * Read-only document viewer dialog - for viewing documents in Settings.
 */
@Composable
fun ReadOnlyDocumentViewer(
    doc: TermsDocument,
    onDismiss: () -> Unit,
) {
    DocumentViewerDialog(
        doc = doc,
        isAgreed = false,
        onAgreeToggle = {},
        onDismiss = onDismiss,
        isReadOnly = true,
    )
}

@Composable
private fun DocumentViewerDialog(
    doc: TermsDocument,
    isAgreed: Boolean,
    onAgreeToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    isReadOnly: Boolean = false,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val primaryText = MaterialTheme.colorScheme.onSurface
    val secondaryText = MaterialTheme.colorScheme.onSurfaceVariant
    val scrollState = rememberScrollState()

    // Track if user scrolled to bottom
    val hasReachedBottom by remember(scrollState.maxValue) {
        derivedStateOf {
            scrollState.maxValue == 0 || scrollState.value >= scrollState.maxValue - 50
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(doc.title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 400.dp)
                        .verticalScroll(scrollState),
                ) {
                    Text(doc.text, fontSize = 14.sp, lineHeight = 22.sp, color = primaryText)
                }
                if (!isReadOnly) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = hasReachedBottom) {
                                onAgreeToggle(!isAgreed)
                                if (!isAgreed) onDismiss()
                            }
                            .background(
                                if (isAgreed) BrandGreen.copy(alpha = 0.1f) else Color.Transparent,
                                RoundedCornerShape(14.dp),
                            )
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            if (isAgreed) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = null,
                            tint = if (isAgreed) BrandGreen else if (hasReachedBottom) primaryText else secondaryText.copy(alpha = 0.3f),
                        )
                        Text(
                            "Ознакомлен(а) и согласен(на)",
                            fontWeight = FontWeight.SemiBold,
                            color = if (hasReachedBottom) primaryText else secondaryText.copy(alpha = 0.4f),
                        )
                    }
                    if (!hasReachedBottom) {
                        Text("Прокрутите документ до конца", fontSize = 11.sp, color = secondaryText, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        },
    )
}

/** All student (general) documents for use in Settings. */
fun getStudentDocuments(): List<TermsDocument> = listOf(
    TermsDocument("contract", "Договор учащегося", "Условия обучения, права и обязанности", Icons.Default.Draw, BrandBlue,
        "ДОГОВОР УЧАЩЕГОСЯ\n\nШкола китайского языка Unlock Language Studio\nг. Ташкент, Узбекистан\n\n1. ПРЕДМЕТ ДОГОВОРА\n\n1.1. Школа обязуется предоставить Учащемуся образовательные услуги по обучению китайскому языку.\n1.2. Учащийся обязуется своевременно посещать занятия и оплачивать обучение.\n\n2. ПРАВА И ОБЯЗАННОСТИ СТОРОН\n\n2.1. Школа обязуется обеспечить качественное проведение занятий.\n2.2. Учащийся обязуется посещать занятия и выполнять домашние задания.\n\nДокумент является заглушкой и будет заменён на полную версию."),
    TermsDocument("offer", "Публичная оферта", "Условия предоставления услуг", Icons.Default.Description, BrandIndigo,
        "ПУБЛИЧНАЯ ОФЕРТА\n\nна оказание образовательных услуг\nUnlock Language Studio\n\n1. ОБЩИЕ ПОЛОЖЕНИЯ\n\n1.1. Настоящий документ является официальной публичной офертой.\n1.2. Акцептом является принятие условий в мобильном приложении.\n\nДокумент является заглушкой и будет заменён на полную версию."),
    TermsDocument("privacy", "Политика конфиденциальности", "Обработка и защита данных", Icons.Default.Shield, BrandTeal,
        "ПОЛИТИКА КОНФИДЕНЦИАЛЬНОСТИ\n\nUnlock Language Studio\n\n1. СБОР ИНФОРМАЦИИ\n\nМы собираем имя, контактные данные, данные об успеваемости и устройстве.\n\n2. ИСПОЛЬЗОВАНИЕ\n\nДля предоставления образовательных услуг и улучшения качества.\n\nДокумент является заглушкой и будет заменён на полную версию."),
    TermsDocument("personalData", "Согласие на обработку данных", "Персональные данные", Icons.Default.Person, BrandCoral,
        "СОГЛАСИЕ НА ОБРАБОТКУ ПЕРСОНАЛЬНЫХ ДАННЫХ\n\nВ соответствии с Законом РУз «О персональных данных».\n\nНастоящее согласие распространяется на ФИО, email, телефон, данные об успеваемости.\n\nДокумент является заглушкой и будет заменён на полную версию."),
    TermsDocument("termsOfUse", "Правила пользования приложением", "Условия использования Unlock", Icons.Default.PhoneAndroid, BrandGold,
        "ПРАВИЛА ПОЛЬЗОВАНИЯ ПРИЛОЖЕНИЕМ UNLOCK\n\n1. Для доступа необходима регистрация.\n2. Одна учётная запись — один пользователь.\n3. Токены не являются деньгами.\n\nДокумент является заглушкой и будет заменён на полную версию."),
)

/** All teacher (employment) documents for use in Settings. */
fun getTeacherDocuments(): List<TermsDocument> = listOf(
    TermsDocument("employment", "Трудовой договор", "Условия работы и оплата труда", Icons.Default.Draw, BrandBlue,
        "ТРУДОВОЙ ДОГОВОР\n\nИП «Unlock Language Studio»\nг. Ташкент\n\n1. ПРЕДМЕТ ДОГОВОРА\n\nРаботодатель принимает Работника на должность преподавателя.\n\n2. УСЛОВИЯ ТРУДА\n\nМесто работы: г. Ташкент / дистанционно.\n\nДокумент является заглушкой и будет заменён на полную версию."),
    TermsDocument("jobDesc", "Должностная инструкция", "Обязанности преподавателя", Icons.Default.Checklist, BrandIndigo,
        "ДОЛЖНОСТНАЯ ИНСТРУКЦИЯ\n\nПреподаватель китайского языка\n\n1. Проведение занятий по расписанию.\n2. Проверка домашних заданий.\n3. Ведение учёта посещаемости.\n\nДокумент является заглушкой и будет заменён на полную версию."),
    TermsDocument("nda", "Соглашение о неразглашении (NDA)", "Конфиденциальность данных", Icons.Default.Lock, BrandCoral,
        "СОГЛАШЕНИЕ О НЕРАЗГЛАШЕНИИ (NDA)\n\n1. Работник обязуется не разглашать конфиденциальную информацию.\n2. К конфиденциальной относятся: методики, данные учащихся, финансы.\n3. Срок: 2 года после увольнения.\n\nДокумент является заглушкой и будет заменён на полную версию."),
    TermsDocument("internalRules", "Внутренний трудовой распорядок", "Правила и дисциплина", Icons.Default.Business, BrandTeal,
        "ПОЛОЖЕНИЕ О ВНУТРЕННЕМ ТРУДОВОМ РАСПОРЯДКЕ\n\n1. Преподаватель обязан быть на рабочем месте за 10 минут до занятия.\n2. Проверять ДЗ в течение 48 часов.\n\nДокумент является заглушкой и будет заменён на полную версию."),
    TermsDocument("imageConsent", "Согласие на использование изображения", "Фото и видео", Icons.Default.CameraAlt, BrandGold,
        "СОГЛАСИЕ НА ИСПОЛЬЗОВАНИЕ ИЗОБРАЖЕНИЯ\n\nДля размещения на сайте, в приложении и соцсетях школы.\nМожет быть отозвано письменным заявлением.\n\nДокумент является заглушкой и будет заменён на полную версию."),
    TermsDocument("staffAppRules", "Правила для сотрудников", "Доступ к данным учеников", Icons.Default.PhoneAndroid, BrandGreen,
        "ПРАВИЛА ПОЛЬЗОВАНИЯ ПРИЛОЖЕНИЕМ (для сотрудников)\n\n1. Не передавать логин и пароль.\n2. Доступ только к данным своих учащихся.\n3. Запрещается экспортировать данные.\n\nДокумент является заглушкой и будет заменён на полную версию."),
)
