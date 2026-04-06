package com.subnetik.unlock

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subnetik.unlock.data.local.datastore.AuthDataStore
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.SessionManager
import com.subnetik.unlock.domain.model.AppUserRole
import com.subnetik.unlock.domain.repository.AuthRepository
import com.subnetik.unlock.presentation.navigation.AppNavigation
import com.subnetik.unlock.presentation.theme.UnlockTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authDataStore: AuthDataStore
    @Inject lateinit var settingsDataStore: SettingsDataStore
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var authApi: com.subnetik.unlock.data.remote.api.AuthApi
    @Inject lateinit var sessionManager: SessionManager

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not — FCM will still deliver silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        setContent {
            // Show toast when session expires
            LaunchedEffect(Unit) {
                sessionManager.sessionExpired.collect {
                    Toast.makeText(
                        this@MainActivity,
                        "Сессия истекла. Войдите снова.",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
            // Use null initial value so we can detect "still loading" vs real value
            val isLoggedIn by authDataStore.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)
            val hasSeenOnboarding by settingsDataStore.hasSeenOnboarding.collectAsStateWithLifecycle(initialValue = true)
            val userRoleStr by authDataStore.userRole.collectAsStateWithLifecycle(initialValue = null)
            val termsAccepted by authDataStore.termsAccepted.collectAsStateWithLifecycle(initialValue = true)
            val teacherTermsAccepted by authDataStore.teacherTermsAccepted.collectAsStateWithLifecycle(initialValue = true)
            val scope = rememberCoroutineScope()
            val themePreference by settingsDataStore.isDarkTheme.collectAsStateWithLifecycle(initialValue = null)
            val systemDark = isSystemInDarkTheme()
            val isDark = themePreference ?: true  // default dark theme

            UnlockTheme(darkTheme = isDark) {
                // While DataStore hasn't emitted real values, show branded loading screen
                val loggedIn = isLoggedIn
                if (loggedIn == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F1429)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.unlock_logo),
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                        )
                    }
                    return@UnlockTheme
                }

                val userRole = AppUserRole.resolve(loggedIn, userRoleStr)

                // Check if terms need to be shown
                val needsStudentTerms = loggedIn && userRole == AppUserRole.STUDENT && !termsAccepted
                val needsTeacherGeneralTerms = loggedIn && userRole == AppUserRole.TEACHER && !termsAccepted
                val needsTeacherWorkTerms = loggedIn && userRole == AppUserRole.TEACHER && termsAccepted && !teacherTermsAccepted

                when {
                    needsStudentTerms || needsTeacherGeneralTerms -> {
                        com.subnetik.unlock.presentation.screens.terms.StudentTermsScreen(
                            onAccept = {
                                scope.launch {
                                    authDataStore.saveTermsAccepted(true)
                                    try { authApi.acceptTerms() } catch (_: Exception) {}
                                }
                            },
                        )
                    }
                    needsTeacherWorkTerms -> {
                        com.subnetik.unlock.presentation.screens.terms.TeacherTermsScreen(
                            onAccept = {
                                scope.launch {
                                    authDataStore.saveTeacherTermsAccepted(true)
                                    try { authApi.acceptTeacherTerms() } catch (_: Exception) {}
                                }
                            },
                        )
                    }
                    else -> {
                        AppNavigation(
                            isLoggedIn = loggedIn,
                            hasSeenOnboarding = hasSeenOnboarding,
                            userRole = userRole,
                            onLogout = { scope.launch { authRepository.logout() } },
                            settingsDataStore = settingsDataStore,
                        )
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
