package com.subnetik.unlock.presentation.screens.shifu

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.R
import com.subnetik.unlock.presentation.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiFuChatScreen(
    onBack: () -> Unit,
    viewModel: ShiFuChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    var inputText by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val bgTop = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC)
    val bgBottom = if (isDark) Color(0xFF1A1E33) else Color(0xFFE6EDF8)

    // Auto-scroll when messages change
    LaunchedEffect(uiState.messages.size, uiState.isSending) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(
                // +1 for typing indicator if sending
                uiState.messages.size - 1 + if (uiState.isSending) 1 else 0,
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.currentTitle,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showHistory = true }) {
                        Icon(Icons.Default.History, contentDescription = "История")
                    }
                    IconButton(onClick = { viewModel.startNewConversation() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Новый диалог")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        containerColor = Color.Transparent,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(bgTop, bgBottom))),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                // Messages
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Welcome view
                    if (uiState.messages.isEmpty() && !uiState.isLoadingMessages) {
                        item {
                            WelcomeView(
                                isDark = isDark,
                                onSuggestionClick = { text ->
                                    viewModel.sendMessage(text)
                                },
                            )
                        }
                    }

                    // Chat messages
                    items(uiState.messages, key = { it.id }) { msg ->
                        ChatBubble(message = msg, isDark = isDark)
                    }

                    // Typing indicator
                    if (uiState.isSending) {
                        item(key = "typing") {
                            TypingIndicator(isDark = isDark)
                        }
                    }

                    // Error
                    uiState.error?.let { error ->
                        item(key = "error") {
                            ErrorBubble(error = error)
                        }
                    }
                }

                // Input bar
                InputBar(
                    text = inputText,
                    onTextChange = { inputText = it },
                    canSend = inputText.isNotBlank() && !uiState.isSending,
                    isDark = isDark,
                    onSend = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                )
            }
        }
    }

    // Conversations history sheet
    if (showHistory) {
        ConversationsSheet(
            conversations = uiState.conversations,
            isLoading = uiState.isLoadingConversations,
            isDark = isDark,
            onDismiss = { showHistory = false },
            onSelect = { id ->
                viewModel.loadConversation(id)
                showHistory = false
            },
            onDelete = { id -> viewModel.deleteConversation(id) },
        )
    }
}

// ─── Welcome View ─────────────────────────────────────────────

@Composable
private fun WelcomeView(isDark: Boolean, onSuggestionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.snow_leopard_wave),
            contentDescription = "Ши Фу",
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(20.dp)),
        )

        Text(
            "Привет! Я Ши Фу",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
        )

        Text(
            "AI ассистент школы Unlock.\nМогу помочь с анализом данных,\nпосещаемости, платежей и групп.",
            style = MaterialTheme.typography.bodySmall,
            color = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Column(
            modifier = Modifier.padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SuggestionChip("Кто из учеников часто пропускает?", onSuggestionClick)
            SuggestionChip("Статистика платежей за этот месяц", onSuggestionClick)
            SuggestionChip("Какие группы самые загруженные?", onSuggestionClick)
        }
    }
}

@Composable
private fun SuggestionChip(text: String, onClick: (String) -> Unit) {
    Surface(
        onClick = { onClick(text) },
        shape = RoundedCornerShape(50),
        color = BrandBlue.copy(alpha = 0.08f),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = BrandBlue,
        )
    }
}

// ─── Chat Bubble ──────────────────────────────────────────────

@Composable
private fun ChatBubble(message: ChatMessage, isDark: Boolean) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (isUser) {
            Spacer(Modifier.width(48.dp))
        }

        if (!isUser) {
            val drawableRes = when (message.mood) {
                ShiFuMood.HAPPY -> R.drawable.snow_leopard_happy
                ShiFuMood.SAD -> R.drawable.snow_leopard_sad
                ShiFuMood.THINKING -> R.drawable.snow_leopard_thinking
                ShiFuMood.WAVE -> R.drawable.snow_leopard_wave
                else -> R.drawable.snow_leopard
            }
            Image(
                painter = painterResource(drawableRes),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
            )
            Spacer(Modifier.width(8.dp))
        }

        val fieldBg = if (isDark) Color(0xFF1E2540) else Color(0xFFF0F0F5)
        val fieldStroke = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isUser) Color.Transparent else fieldBg,
            border = if (isUser) null else ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = Brush.linearGradient(listOf(fieldStroke, fieldStroke)),
            ),
        ) {
            Box(
                modifier = if (isUser) {
                    Modifier.background(
                        Brush.linearGradient(listOf(BrandIndigo, BrandBlue)),
                        RoundedCornerShape(16.dp),
                    )
                } else {
                    Modifier
                },
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White
                    else if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        if (!isUser) {
            Spacer(Modifier.width(48.dp))
        }
    }
}

// ─── Typing Indicator ─────────────────────────────────────────

@Composable
private fun TypingIndicator(isDark: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )

    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.snow_leopard_thinking),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Gray.copy(alpha = 0.1f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .offset(y = (sin(phase + i * 0.8f) * 3).dp)
                            .background(BrandBlue.copy(alpha = 0.5f), CircleShape),
                    )
                }
            }
        }
    }
}

// ─── Error Bubble ─────────────────────────────────────────────

@Composable
private fun ErrorBubble(error: String) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.snow_leopard_sad),
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape),
        )
        Text(
            text = error,
            modifier = Modifier
                .background(BrandCoral.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                .padding(10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = BrandCoral,
        )
    }
}

// ─── Input Bar ────────────────────────────────────────────────

@Composable
private fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    canSend: Boolean,
    isDark: Boolean,
    onSend: () -> Unit,
) {
    val cardBg = if (isDark) Color(0xFF171E33) else Color.White
    val fieldBg = if (isDark) Color(0xFF1E2540) else Color(0xFFF0F0F5)
    val fieldStroke = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    Surface(
        color = cardBg,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Написать сообщение...",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                maxLines = 5,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = fieldBg,
                    focusedContainerColor = fieldBg,
                    unfocusedBorderColor = fieldStroke,
                    focusedBorderColor = BrandBlue.copy(alpha = 0.4f),
                ),
            )

            IconButton(
                onClick = onSend,
                enabled = canSend,
                modifier = Modifier.size(40.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (canSend) Brush.linearGradient(listOf(BrandIndigo, BrandBlue))
                            else Brush.linearGradient(
                                listOf(
                                    Color.Gray.copy(alpha = 0.3f),
                                    Color.Gray.copy(alpha = 0.3f),
                                ),
                            ),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = "Отправить",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

// ─── Conversations History Sheet ──────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationsSheet(
    conversations: List<com.subnetik.unlock.data.remote.dto.ai.AiConversationListItem>,
    isLoading: Boolean,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val bgColor = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bgColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "История диалогов",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                )
                TextButton(onClick = onDismiss) {
                    Text("Закрыть")
                }
            }

            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = BrandBlue)
                }
            } else if (conversations.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(
                        painter = painterResource(R.drawable.snow_leopard_sad),
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(20.dp)),
                    )
                    Text(
                        "Нет сохранённых диалогов",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(conversations, key = { it.id }) { conv ->
                        ConversationItem(
                            conversation = conv,
                            isDark = isDark,
                            onClick = { onSelect(conv.id) },
                            onDelete = { onDelete(conv.id) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationItem(
    conversation: com.subnetik.unlock.data.remote.dto.ai.AiConversationListItem,
    isDark: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val cardBg = if (isDark) Color(0xFF171E33).copy(alpha = 0.96f) else Color.White.copy(alpha = 0.98f)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BrandCoral, RoundedCornerShape(14.dp))
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.White)
            }
        },
        enableDismissFromStartToEnd = false,
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = cardBg,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.snow_leopard),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        conversation.title ?: "Без названия",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "${conversation.messageCount} сообщ.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            formatDate(conversation.updatedAt),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(iso: String): String {
    return try {
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()),
        )
        formats.forEach { fmt ->
            fmt.timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = formats.firstNotNullOfOrNull { fmt ->
            try { fmt.parse(iso) } catch (_: Exception) { null }
        } ?: return iso

        val cal = Calendar.getInstance()
        val dateCal = Calendar.getInstance().apply { time = date }
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

        when {
            cal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR) &&
                cal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) -> timeFormat.format(date)
            cal.get(Calendar.DAY_OF_YEAR) - dateCal.get(Calendar.DAY_OF_YEAR) == 1 &&
                cal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) -> "Вчера"
            else -> dateFormat.format(date)
        }
    } catch (_: Exception) {
        iso
    }
}
