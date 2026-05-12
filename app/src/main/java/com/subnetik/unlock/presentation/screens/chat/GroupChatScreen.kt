package com.subnetik.unlock.presentation.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.subnetik.unlock.data.remote.dto.chat.ChatMessageDto
import com.subnetik.unlock.data.remote.dto.chat.GroupMemberDto
import com.subnetik.unlock.data.remote.dto.chat.PollInfoDto
import com.subnetik.unlock.presentation.components.ErrorInline
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.Brand
import com.subnetik.unlock.presentation.theme.BrandBlue
import com.subnetik.unlock.presentation.theme.BrandCoral
import com.subnetik.unlock.presentation.theme.BrandGold
import com.subnetik.unlock.presentation.theme.BrandIndigo
import com.subnetik.unlock.presentation.theme.BrandTeal
import com.subnetik.unlock.presentation.theme.softShadow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    onBack: () -> Unit,
    viewModel: GroupChatViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDark = state.isDarkTheme ?: isSystemInDarkTheme()
    var inputText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var searchInput by remember { mutableStateOf("") }
    var currentSearchIndex by remember { mutableStateOf(0) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll to highlighted result when index changes
    LaunchedEffect(currentSearchIndex, state.searchResults) {
        val results = state.searchResults
        if (results.isNotEmpty() && currentSearchIndex in results.indices) {
            val msgId = results[currentSearchIndex].id
            val index = state.messages.indexOfFirst { it.id == msgId }
            if (index >= 0) {
                scope.launch { listState.animateScrollToItem(index) }
            }
        }
    }

    // Auto-scroll to last message when new messages arrive
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(state.messages.size - 1) }
        }
    }

    // Scroll to specific message (e.g. when tapping pinned banner)
    LaunchedEffect(state.scrollToMessageId) {
        val targetId = state.scrollToMessageId ?: return@LaunchedEffect
        val index = state.messages.indexOfFirst { it.id == targetId }
        if (index >= 0) {
            scope.launch { listState.animateScrollToItem(index) }
        }
        viewModel.consumeScrollRequest()
    }

    val canModerate = state.currentUserRole in listOf("teacher", "admin", "manager")

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            if (isSearching) {
                SearchTopBar(
                    query = searchInput,
                    onQueryChange = { v ->
                        searchInput = v
                        viewModel.search(v)
                        currentSearchIndex = 0
                    },
                    results = state.searchResults.size,
                    currentIndex = currentSearchIndex,
                    onClose = {
                        isSearching = false
                        searchInput = ""
                        viewModel.clearSearch()
                    },
                    onPrev = { if (currentSearchIndex > 0) currentSearchIndex-- },
                    onNext = { if (currentSearchIndex < state.searchResults.size - 1) currentSearchIndex++ },
                    onClear = {
                        searchInput = ""
                        viewModel.clearSearch()
                    },
                    isDark = isDark,
                )
            } else {
                com.subnetik.unlock.presentation.components.IosTopAppBar(
                    isDark = isDark,
                    onBack = onBack,
                    barHeight = 56.dp,
                    actions = {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Поиск")
                        }
                        IconButton(onClick = { viewModel.loadMembers() }) {
                            Icon(Icons.Default.Group, contentDescription = "Участники")
                        }
                        if (canModerate) {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Outlined.MoreVert, contentDescription = "Меню")
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Очистить чат") },
                                    leadingIcon = { Icon(Icons.Default.DeleteSweep, null, tint = BrandCoral) },
                                    onClick = { showMenu = false; viewModel.requestClearChat() },
                                )
                            }
                        }
                    },
                    titleContent = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                state.groupName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                            )
                            if (state.typingUsers.isNotEmpty()) {
                                Text(
                                    state.typingUsers.joinToString(", ") { it.name } + " печатает…",
                                    fontSize = 11.sp,
                                    color = BrandBlue,
                                )
                            } else if (state.members.isNotEmpty()) {
                                Text(
                                    "${state.members.size} участников",
                                    fontSize = 11.sp,
                                    color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                                )
                            }
                        }
                    },
                )
            }
        },
        bottomBar = {
            MessageInputBar(
                text = inputText,
                onTextChange = { inputText = it; viewModel.notifyTyping() },
                replyingTo = state.replyingTo,
                onCancelReply = { viewModel.setReplyingTo(null) },
                isSending = state.isSending,
                onSend = {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                },
                isDark = isDark,
                canCreatePoll = canModerate,
                onCreatePoll = { viewModel.openPollCreator() },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            AdminBackground(isDark = isDark)
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Pinned messages banner
                if (state.pinnedMessages.isNotEmpty()) {
                    PinnedBanner(
                        pinned = state.pinnedMessages,
                        isDark = isDark,
                        onClick = {
                            val first = state.pinnedMessages.firstOrNull() ?: return@PinnedBanner
                            viewModel.jumpToMessage(first.id)
                        },
                    )
                }

                if (state.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(state.messages, key = { it.id }) { msg ->
                            val isSearchHighlight = isSearching && state.searchResults.isNotEmpty() &&
                                state.searchResults.getOrNull(currentSearchIndex)?.id == msg.id
                            val isJumpHighlight = state.highlightedMessageId == msg.id
                            MessageBubble(
                                message = msg,
                                isOwn = msg.senderId == state.currentUserId,
                                canModerate = canModerate,
                                isDark = isDark,
                                searchQuery = if (isSearching && searchInput.length >= 3) searchInput else "",
                                isHighlighted = isSearchHighlight || isJumpHighlight,
                                onLongPress = { viewModel.openMessageMenu(msg) },
                                onVotePoll = { optionIndex ->
                                    msg.poll?.let { viewModel.votePoll(msg.id, it.id, optionIndex) }
                                },
                            )
                        }
                    }
                }

                if (state.error != null) {
                    ErrorInline(
                        message = state.error!!,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }

            // Message action menu
            state.activeMessage?.let { msg ->
                MessageActionDialog(
                    message = msg,
                    isOwn = msg.senderId == state.currentUserId,
                    canModerate = canModerate,
                    onDismiss = { viewModel.openMessageMenu(null) },
                    onReply = { viewModel.setReplyingTo(msg); viewModel.openMessageMenu(null) },
                    onReact = { emoji -> viewModel.react(msg.id, emoji) },
                    onPin = { viewModel.togglePin(msg.id) },
                    onDelete = { viewModel.deleteMessage(msg.id) },
                    onReport = { viewModel.requestReport(msg.id) },
                    onViewReads = { viewModel.loadReads(msg.id) },
                )
            }

            // Clear chat confirmation
            if (state.showClearConfirm) {
                AlertDialog(
                    onDismissRequest = { viewModel.cancelClearChat() },
                    title = { Text("Очистить чат?") },
                    text = { Text("Все сообщения будут удалены. Это действие нельзя отменить.") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearChat() }) {
                            Text("Очистить", color = BrandCoral)
                        }
                    },
                    dismissButton = { TextButton(onClick = { viewModel.cancelClearChat() }) { Text("Отмена") } },
                )
            }

            // Report confirmation
            state.reportTargetId?.let { id ->
                AlertDialog(
                    onDismissRequest = { viewModel.cancelReport() },
                    title = { Text("Пожаловаться на сообщение?") },
                    text = { Text("Жалоба будет отправлена администратору для рассмотрения.") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.reportMessage(id) }) {
                            Text("Пожаловаться", color = BrandCoral)
                        }
                    },
                    dismissButton = { TextButton(onClick = { viewModel.cancelReport() }) { Text("Отмена") } },
                )
            }

            if (state.showReportSuccess) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissReportSuccess() },
                    title = { Text("Жалоба отправлена") },
                    text = { Text("Спасибо! Администратор рассмотрит вашу жалобу.") },
                    confirmButton = { TextButton(onClick = { viewModel.dismissReportSuccess() }) { Text("OK") } },
                )
            }

            if (state.showPollCreator) {
                PollCreatorDialog(
                    onDismiss = { viewModel.closePollCreator() },
                    onCreate = { q, opts, anon, multi -> viewModel.createPoll(q, opts, anon, multi) },
                )
            }

            if (state.showMembersSheet) {
                MembersBottomSheet(
                    members = state.members,
                    onDismiss = { viewModel.closeMembersSheet() },
                    isDark = isDark,
                )
            }
        }
    }
}

@Composable
private fun PinnedBanner(
    pinned: List<ChatMessageDto>,
    isDark: Boolean,
    onClick: () -> Unit = {},
) {
    // iOS-style: gold accent bar on the left + subtle card bg
    val bg = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.92f)
    val first = pinned.first()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // iOS-style gold accent bar on the left
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(BrandGold),
        )
        Spacer(Modifier.width(10.dp))
        Icon(Icons.Default.PushPin, null, tint = BrandGold, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (pinned.size == 1) "Закреплённое сообщение" else "Закреплено · ${pinned.size}",
                style = MaterialTheme.typography.labelSmall,
                color = BrandGold,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                first.text,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.82f),
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: ChatMessageDto,
    isOwn: Boolean,
    canModerate: Boolean,
    isDark: Boolean,
    searchQuery: String = "",
    isHighlighted: Boolean = false,
    onLongPress: () -> Unit,
    onVotePoll: (Int) -> Unit,
) {
    // iOS-aligned: Blue → Indigo gradient for own messages (matches GroupChatView.swift)
    val ownGradient = Brush.linearGradient(listOf(BrandBlue, BrandIndigo))
    val otherBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.95f)
    val deletedBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)

    val textColor = when {
        message.isDeleted -> if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)
        isOwn -> Color.White
        else -> if (isDark) Color.White else Color.Black.copy(alpha = 0.9f)
    }

    // iOS uses symmetric 16dp corners (continuous style) — no Telegram-tail
    val bubbleShape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isOwn) {
            // Avatar — iOS size 30dp, teal tint for teachers
            Box(
                modifier = Modifier.size(30.dp).clip(CircleShape).background(BrandTeal.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                val avatar = message.senderAvatar?.let {
                    if (it.startsWith("http")) it else "https://unlocklingua.com$it"
                }
                if (!avatar.isNullOrEmpty()) {
                    AsyncImage(avatar, null, Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Text(
                        message.senderName.firstOrNull()?.uppercase() ?: "?",
                        color = BrandTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                // Subtle elevation on non-own bubbles for iOS-like "floating" look
                .then(
                    if (!isOwn && !message.isDeleted) {
                        Modifier.softShadow(elevation = 4.dp, shape = bubbleShape, alpha = if (isDark) 0.2f else 0.05f)
                    } else Modifier
                )
                .clip(bubbleShape)
                .then(
                    when {
                        message.isDeleted -> Modifier.background(deletedBg)
                        isOwn -> Modifier.background(brush = ownGradient)
                        else -> Modifier.background(otherBg)
                    }
                )
                .then(
                    if (isHighlighted)
                        Modifier.border(2.dp, BrandGold, bubbleShape)
                    else Modifier
                )
                .combinedClickable(
                    enabled = !message.isDeleted,
                    onClick = { /* ignore simple tap */ },
                    onLongClick = { onLongPress() },
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            if (!isOwn) {
                Text(
                    message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) BrandTeal else BrandBlue,
                )
            }
            if (message.isPinned && !message.isDeleted) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(Icons.Default.PushPin, null, tint = BrandGold, modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Закреплено", fontSize = 10.sp, color = BrandGold)
                }
            }
            if (message.replyToId != null && !message.replyToText.isNullOrEmpty()) {
                val replyBg = if (isOwn) Color.White.copy(alpha = 0.15f) else BrandBlue.copy(alpha = 0.1f)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(8.dp)).background(replyBg).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.width(3.dp).height(28.dp).clip(RoundedCornerShape(2.dp))
                            .background(if (isOwn) Color.White else BrandBlue),
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            message.replyToSender ?: "",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                        )
                        Text(
                            message.replyToText,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = textColor.copy(alpha = 0.75f),
                        )
                    }
                }
            }
            if (message.poll != null) {
                PollView(poll = message.poll, textColor = textColor, onVote = onVotePoll)
            } else if (message.isDeleted) {
                Text(
                    "Сообщение удалено",
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                )
            } else {
                Text(
                    text = highlightMatches(message.text, searchQuery, isOwn, isDark),
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (message.reactions.isNotEmpty() && !message.isDeleted) {
                Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    message.reactions.forEach { r ->
                        val reactionBg = when {
                            r.reacted && isOwn -> Color.White.copy(alpha = 0.28f)
                            r.reacted -> BrandBlue.copy(alpha = 0.22f)
                            isOwn -> Color.White.copy(alpha = 0.18f)
                            else -> if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f)
                        }
                        Surface(
                            shape = Brand.Shapes.full,
                            color = reactionBg,
                        ) {
                            Text(
                                "${r.emoji} ${r.count}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    formatTime(message.createdAt),
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.65f),
                )
                if (isOwn && !message.isDeleted) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        if (message.readByCount > 0) Icons.Default.DoneAll else Icons.Default.Done,
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.75f),
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PollView(poll: PollInfoDto, textColor: Color, onVote: (Int) -> Unit) {
    Column {
        Text(poll.question, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textColor)
        Spacer(Modifier.height(6.dp))
        poll.options.forEachIndexed { index, opt ->
            val percent = if (poll.totalVotes > 0) (opt.votes * 100f / poll.totalVotes).toInt() else 0
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable { if (!opt.voted || poll.isMultiple) onVote(index) },
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.15f),
            ) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (opt.voted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        null,
                        tint = textColor,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(opt.text, fontSize = 12.sp, color = textColor, modifier = Modifier.weight(1f))
                    Text("$percent%", fontSize = 11.sp, color = textColor.copy(alpha = 0.7f))
                }
            }
        }
        Text(
            "Голосов: ${poll.totalVotes}${if (poll.isAnonymous) " · анонимно" else ""}",
            fontSize = 10.sp,
            color = textColor.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    replyingTo: ChatMessageDto?,
    onCancelReply: () -> Unit,
    isSending: Boolean,
    onSend: () -> Unit,
    isDark: Boolean,
    canCreatePoll: Boolean = false,
    onCreatePoll: () -> Unit = {},
) {
    val hintColor = if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.35f)
    val textColor = if (isDark) Color.White else Color.Black.copy(alpha = 0.9f)
    // iOS-style: field bg with soft tint
    val capsuleBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.96f)
    val sendActive = text.isNotBlank()
    val sendBg = Brush.linearGradient(
        if (sendActive) listOf(BrandBlue, BrandIndigo)
        else listOf(Color.Gray.copy(alpha = 0.28f), Color.Gray.copy(alpha = 0.28f))
    )
    val pollBg = Brush.linearGradient(listOf(BrandTeal, BrandBlue))

    // Transparent bottom container — no solid background, just floats over chat content
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        if (replyingTo != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .softShadow(elevation = 4.dp, shape = Brand.Shapes.medium, alpha = if (isDark) 0.2f else 0.05f)
                    .clip(Brand.Shapes.medium)
                    .background(capsuleBg)
                    .padding(Brand.Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.width(3.dp).height(28.dp).clip(RoundedCornerShape(2.dp)).background(BrandBlue),
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        replyingTo.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandBlue,
                    )
                    Text(
                        replyingTo.text,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = textColor.copy(alpha = 0.7f),
                    )
                }
                IconButton(onClick = onCancelReply, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = textColor.copy(alpha = 0.6f))
                }
            }
        }
        Row(verticalAlignment = Alignment.Bottom) {
            if (canCreatePoll) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(brush = pollBg)
                        .clickable(onClick = onCreatePoll),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Poll, contentDescription = "Создать опрос", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
            }
            // Floating capsule text field
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                color = capsuleBg,
                shadowElevation = 4.dp,
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (text.isEmpty()) {
                        Text("Написать сообщение", color = hintColor, fontSize = 15.sp)
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(BrandBlue),
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(brush = sendBg)
                    .clickable(enabled = sendActive && !isSending, onClick = onSend),
                contentAlignment = Alignment.Center,
            ) {
                if (isSending) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun MessageActionDialog(
    message: ChatMessageDto,
    isOwn: Boolean,
    canModerate: Boolean,
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onReact: (String) -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit,
    onViewReads: () -> Unit,
) {
    val canDelete = isOwn || canModerate
    // All participants can pin messages (max 5 pinned per chat). Unpin requires teacher/admin or owner.
    val canPin = if (message.isPinned) (canModerate || isOwn) else true
    // Must match backend ALLOWED_REACTIONS whitelist
    val emojis = listOf("👍", "❤️", "😂", "😮", "😢", "🔥")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Действия") },
        text = {
            Column {
                Text(message.senderName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(message.text, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color.Gray)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                    emojis.forEach { emoji ->
                        Text(
                            emoji,
                            fontSize = 22.sp,
                            modifier = Modifier.clickable { onReact(emoji); onDismiss() }.padding(4.dp),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                TextButton(onClick = onReply, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Filled.Reply, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ответить", modifier = Modifier.weight(1f))
                }
                // "Просмотрено" видит только автор сообщения и модераторы
                // (учителя/админы). Иначе любой ученик мог бы посмотреть, кто
                // прочитал чужие сообщения — это нарушает приватность остальных.
                // Backend тоже возвращает 403 для не-автора и не-staff, но
                // прячем кнопку в UI, чтобы вообще не вводить в заблуждение.
                if (!message.isDeleted && (isOwn || canModerate)) {
                    TextButton(onClick = { onViewReads(); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Просмотрено · ${message.readByCount}", modifier = Modifier.weight(1f))
                    }
                }
                if (canPin) {
                    TextButton(onClick = onPin, modifier = Modifier.fillMaxWidth()) {
                        Icon(if (message.isPinned) Icons.Default.Close else Icons.Default.PushPin, null, tint = BrandGold, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (message.isPinned) "Открепить" else "Закрепить", modifier = Modifier.weight(1f))
                    }
                }
                if (canDelete) {
                    TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Delete, null, tint = BrandCoral, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Удалить", color = BrandCoral, modifier = Modifier.weight(1f))
                    }
                }
                if (!isOwn && !message.isDeleted) {
                    TextButton(onClick = onReport, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Flag, null, tint = Color(0xFFFF9800), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Пожаловаться", color = Color(0xFFFF9800), modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MembersBottomSheet(
    members: List<GroupMemberDto>,
    onDismiss: () -> Unit,
    isDark: Boolean,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Участники · ${members.size}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(members) { member ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(BrandTeal.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            val avatar = member.avatarUrl?.let {
                                if (it.startsWith("http")) it else "https://unlocklingua.com$it"
                            }
                            if (!avatar.isNullOrEmpty()) {
                                AsyncImage(avatar, null, Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                            } else {
                                Text(member.displayName.firstOrNull()?.uppercase() ?: "?", color = BrandTeal, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(member.displayName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            val roleTag = when (member.role) {
                                "teacher" -> "Преподаватель"
                                "admin", "manager" -> "Администрация"
                                "student" -> "Учащийся"
                                else -> member.role
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (member.role) {
                                    "teacher" -> BrandTeal.copy(alpha = 0.2f)
                                    "admin", "manager" -> Color(0xFF5E35B1).copy(alpha = 0.2f)
                                    else -> BrandBlue.copy(alpha = 0.2f)
                                },
                            ) {
                                Text(
                                    roleTag,
                                    fontSize = 10.sp,
                                    color = when (member.role) {
                                        "teacher" -> BrandTeal
                                        "admin", "manager" -> Color(0xFF5E35B1)
                                        else -> BrandBlue
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PollCreatorDialog(
    onDismiss: () -> Unit,
    onCreate: (question: String, options: List<String>, isAnonymous: Boolean, isMultiple: Boolean) -> Unit,
) {
    var question by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "") }
    var isAnonymous by remember { mutableStateOf(false) }
    var isMultiple by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать опрос") },
        text = {
            Column {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Вопрос") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                options.forEachIndexed { index, opt ->
                    OutlinedTextField(
                        value = opt,
                        onValueChange = { options[index] = it },
                        label = { Text("Вариант ${index + 1}") },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    )
                }
                if (options.size < 10) {
                    TextButton(onClick = { options.add("") }) { Text("+ Добавить вариант") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isAnonymous, onCheckedChange = { isAnonymous = it })
                    Text("Анонимно")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isMultiple, onCheckedChange = { isMultiple = it })
                    Text("Несколько ответов")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(question, options, isAnonymous, isMultiple) }) { Text("Создать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

private fun formatTime(iso: String): String {
    // Extract HH:mm from ISO timestamp
    return iso.substringAfter('T', "").take(5)
}

@Composable
private fun highlightMatches(text: String, query: String, isOwn: Boolean, isDark: Boolean): AnnotatedString {
    if (query.length < 3) return AnnotatedString(text)
    // iOS uses yellow.opacity(0.7) + black text for all messages
    val highlightBg = Color.Yellow.copy(alpha = 0.7f)
    val highlightFg = Color.Black
    return buildAnnotatedString {
        var index = 0
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        while (index < text.length) {
            val matchStart = lowerText.indexOf(lowerQuery, index)
            if (matchStart < 0) {
                append(text.substring(index))
                break
            }
            if (matchStart > index) append(text.substring(index, matchStart))
            withStyle(SpanStyle(background = highlightBg, color = highlightFg, fontWeight = FontWeight.SemiBold)) {
                append(text.substring(matchStart, matchStart + query.length))
            }
            index = matchStart + query.length
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    results: Int,
    currentIndex: Int,
    onClose: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onClear: () -> Unit,
    isDark: Boolean,
) {
    val textColor = if (isDark) Color.White else Color.Black.copy(alpha = 0.85f)
    val hintColor = if (isDark) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.4f)
    val iconColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
    val capsuleBg = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White

    // Transparent container — floating capsule that overlays chat content
    Column(
        modifier = Modifier.fillMaxWidth().statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(capsuleBg)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Закрыть", tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            // Floating capsule search field
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                color = capsuleBg,
                shadowElevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Search, null, tint = iconColor, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text("Поиск по чату", color = hintColor, fontSize = 15.sp)
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(BrandBlue),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (query.isNotEmpty()) {
                        IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, tint = iconColor, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
        // Floating pill with results counter + nav buttons
        if (results > 0) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp).padding(bottom = 4.dp),
                shape = RoundedCornerShape(18.dp),
                color = capsuleBg,
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Найдено: ${currentIndex + 1} из $results",
                        fontSize = 12.sp,
                        color = iconColor,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onPrev, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.KeyboardArrowUp, null, tint = iconColor, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onNext, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = iconColor, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
