package com.subnetik.unlock.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.luminance
import com.subnetik.unlock.presentation.theme.BrandBlue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.rememberCoroutineScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.domain.model.AppUserRole
import com.subnetik.unlock.presentation.screens.auth.AuthScreen
import kotlinx.coroutines.launch
import com.subnetik.unlock.presentation.screens.auth.VerifyCodeScreen
import com.subnetik.unlock.presentation.screens.home.HomeScreen
import com.subnetik.unlock.presentation.screens.notifications.NotificationsScreen
import com.subnetik.unlock.presentation.screens.onboarding.OnboardingScreen
import com.subnetik.unlock.presentation.screens.profile.ProfileScreen
import com.subnetik.unlock.presentation.screens.profile.EditProfileScreen
import com.subnetik.unlock.presentation.screens.test.TestListScreen
import com.subnetik.unlock.presentation.screens.test.TestSessionScreen
import com.subnetik.unlock.presentation.screens.test.TestResultScreen
import com.subnetik.unlock.presentation.screens.vocabulary.VocabularyScreen
import com.subnetik.unlock.presentation.screens.vocabulary.VocabularyLevelScreen
import com.subnetik.unlock.presentation.screens.vocabulary.FlashcardScreen
import com.subnetik.unlock.presentation.screens.admin.AdminHomeScreen
import com.subnetik.unlock.presentation.screens.admin.AdminPanelScreen
import com.subnetik.unlock.presentation.screens.contact.ContactScreen
import com.subnetik.unlock.presentation.screens.market.MarketScreen
import com.subnetik.unlock.presentation.screens.payment.PaymentScreen
import com.subnetik.unlock.presentation.screens.payment.StudentPaymentsScreen
import com.subnetik.unlock.presentation.screens.schedule.StudentScheduleScreen
import com.subnetik.unlock.presentation.screens.shifu.ShiFuChatScreen
import com.subnetik.unlock.presentation.screens.teacher.TeacherHomeScreen
import com.subnetik.unlock.presentation.screens.teacher.TeacherGroupsScreen

@Composable
fun AppNavigation(
    isLoggedIn: Boolean,
    hasSeenOnboarding: Boolean,
    userRole: AppUserRole,
    onLogout: () -> Unit,
    settingsDataStore: SettingsDataStore? = null,
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val startDestination = when {
        !hasSeenOnboarding -> Routes.Onboarding.route
        !isLoggedIn -> Routes.Auth.route
        else -> Routes.Home.route
    }

    val mainTabRoutes = setOf(
        Routes.Home.route,
        Routes.Homework.route,
        Routes.Test.route,
        Routes.Profile.route,
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val baseRoute = currentRoute?.substringBefore("?")
            if (isLoggedIn && baseRoute in mainTabRoutes) {
                val items = getBottomNavItems(userRole)
                UnlockBottomBar(
                    items = items,
                    currentRoute = baseRoute,
                    onItemClick = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Onboarding
            composable(Routes.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        scope.launch { settingsDataStore?.setOnboardingSeen() }
                        navController.navigate(Routes.Auth.route) {
                            popUpTo(Routes.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Auth
            composable(Routes.Auth.route) {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToVerifyCode = { email ->
                        navController.navigate(Routes.VerifyCode.createRoute(email))
                    }
                )
            }

            composable(
                Routes.VerifyCode.route,
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                VerifyCodeScreen(
                    email = email,
                    onVerified = {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // Main tabs
            composable(Routes.Home.route) {
                when (userRole) {
                    AppUserRole.ADMIN -> AdminHomeScreen(
                        onNavigateToNotifications = { navController.navigate(Routes.Notifications.route) },
                    )
                    AppUserRole.TEACHER -> TeacherHomeScreen(
                        onNavigateToNotifications = { navController.navigate(Routes.Notifications.route) },
                    )
                    else -> HomeScreen(
                        onNavigateToVocabulary = { navController.navigate(Routes.Vocabulary.route) },
                        onNavigateToTests = { navController.navigate(Routes.Test.route) },
                        onNavigateToNotifications = { navController.navigate(Routes.Notifications.route) },
                        onNavigateToSchedule = { navController.navigate(Routes.Schedule.route) },
                        onNavigateToSupportBooking = {
                            navController.navigate("${Routes.Homework.route}?tab=support") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToPayment = { navController.navigate(Routes.StudentPayments.route) },
                        onNavigateToMarket = { navController.navigate(Routes.Market.route) },
                        onNavigateToReferral = { navController.navigate(Routes.Referral.route) },
                    )
                }
            }

            composable(
                route = "${Routes.Homework.route}?tab={tab}",
                arguments = listOf(navArgument("tab") { defaultValue = ""; type = NavType.StringType }),
            ) { backStackEntry ->
                val tab = backStackEntry.arguments?.getString("tab") ?: ""
                when (userRole) {
                    AppUserRole.ADMIN -> AdminPanelScreen()
                    AppUserRole.TEACHER -> TeacherGroupsScreen()
                    else -> com.subnetik.unlock.presentation.screens.homework.StudentHomeworkScreen(
                        initialTab = tab,
                    )
                }
            }

            composable(Routes.Test.route) {
                TestListScreen(
                    onNavigateToTest = { level ->
                        navController.navigate(Routes.TestSession.createRoute(level))
                    }
                )
            }

            composable(Routes.Profile.route) {
                ProfileScreen(
                    onNavigateToEditProfile = { navController.navigate(Routes.EditProfile.route) },
                    onNavigateToNotifications = { navController.navigate(Routes.Notifications.route) },
                    onNavigateToContact = { navController.navigate(Routes.Contact.route) },
                    onNavigateToShiFu = { navController.navigate(Routes.ShiFuChat.route) },
                    onNavigateToPayment = { navController.navigate(Routes.Payment.route) },
                    onNavigateToPromocodes = { navController.navigate(Routes.Promocodes.route) },
                    onLogout = onLogout,
                )
            }

            // Vocabulary
            composable(Routes.Vocabulary.route) {
                VocabularyScreen(
                    onNavigateToLevel = { level ->
                        navController.navigate(Routes.VocabularyLevel.createRoute(level))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                Routes.VocabularyLevel.route,
                arguments = listOf(navArgument("level") { type = NavType.IntType })
            ) { backStackEntry ->
                val level = backStackEntry.arguments?.getInt("level") ?: 1
                VocabularyLevelScreen(
                    level = level,
                    onNavigateToFlashcards = {
                        navController.navigate(Routes.Flashcards.createRoute(level))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                Routes.Flashcards.route,
                arguments = listOf(navArgument("level") { type = NavType.IntType })
            ) { backStackEntry ->
                val level = backStackEntry.arguments?.getInt("level") ?: 1
                FlashcardScreen(
                    level = level,
                    onBack = { navController.popBackStack() }
                )
            }

            // Test
            composable(
                Routes.TestSession.route,
                arguments = listOf(navArgument("level") { type = NavType.IntType })
            ) { backStackEntry ->
                val level = backStackEntry.arguments?.getInt("level") ?: 1
                TestSessionScreen(
                    level = level,
                    onComplete = { score, total ->
                        navController.navigate(Routes.TestResult.createRoute(level, score, total)) {
                            popUpTo(Routes.Test.route)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                Routes.TestResult.route,
                arguments = listOf(
                    navArgument("level") { type = NavType.IntType },
                    navArgument("score") { type = NavType.IntType },
                    navArgument("total") { type = NavType.IntType },
                )
            ) { backStackEntry ->
                val level = backStackEntry.arguments?.getInt("level") ?: 1
                val score = backStackEntry.arguments?.getInt("score") ?: 0
                val total = backStackEntry.arguments?.getInt("total") ?: 10
                TestResultScreen(
                    level = level,
                    score = score,
                    total = total,
                    showTrialButton = userRole == AppUserRole.USER || userRole == AppUserRole.GUEST,
                    onRetry = {
                        navController.navigate(Routes.TestSession.createRoute(level)) {
                            popUpTo(Routes.Test.route)
                        }
                    },
                    onBack = {
                        navController.navigate(Routes.Test.route) {
                            popUpTo(Routes.Test.route) { inclusive = true }
                        }
                    }
                )
            }

            // Profile sub-screens
            composable(Routes.EditProfile.route) {
                EditProfileScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.Notifications.route) {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.Contact.route) {
                ContactScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.ShiFuChat.route) {
                ShiFuChatScreen(onBack = { navController.popBackStack() })
            }

            // Student screens
            composable(Routes.Schedule.route) {
                StudentScheduleScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SupportBooking.route) {
                com.subnetik.unlock.presentation.screens.schedule.SupportBookingScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.Payment.route) {
                PaymentScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToMyPayments = { navController.navigate(Routes.StudentPayments.route) },
                )
            }
            composable(Routes.StudentPayments.route) {
                StudentPaymentsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.Market.route) {
                MarketScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.Promocodes.route) {
                com.subnetik.unlock.presentation.screens.promocode.PromocodesScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.Referral.route) {
                com.subnetik.unlock.presentation.screens.referral.ReferralScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun UnlockBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (String) -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val barBg = if (isDark) Color(0xFF141929) else Color(0xFFF2F3F8)
    val selectedBg = if (isDark) BrandBlue.copy(alpha = 0.18f) else BrandBlue.copy(alpha = 0.12f)
    val selectedIconColor = BrandBlue
    val selectedTextColor = BrandBlue
    val unselectedColor = if (isDark) Color.White.copy(alpha = 0.40f) else Color(0xFF8E8E93)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
        shape = RoundedCornerShape(22.dp),
        color = barBg,
        shadowElevation = if (isDark) 0.dp else 4.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val iconColor by animateColorAsState(
                    targetValue = if (selected) selectedIconColor else unselectedColor,
                    label = "iconColor",
                )
                val textColor by animateColorAsState(
                    targetValue = if (selected) selectedTextColor else unselectedColor,
                    label = "textColor",
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .then(
                            if (selected) Modifier.background(selectedBg, RoundedCornerShape(16.dp))
                            else Modifier
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onItemClick(item.route) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = item.label,
                            fontSize = 9.sp,
                            lineHeight = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = textColor,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}
