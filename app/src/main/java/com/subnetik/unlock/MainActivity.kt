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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
            val isLoggedIn by authDataStore.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
            val hasSeenOnboarding by settingsDataStore.hasSeenOnboarding.collectAsStateWithLifecycle(initialValue = true)
            val userRoleStr by authDataStore.userRole.collectAsStateWithLifecycle(initialValue = null)
            val userRole = AppUserRole.resolve(isLoggedIn, userRoleStr)
            val termsAccepted by authDataStore.termsAccepted.collectAsStateWithLifecycle(initialValue = true)
            val teacherTermsAccepted by authDataStore.teacherTermsAccepted.collectAsStateWithLifecycle(initialValue = true)
            val scope = rememberCoroutineScope()
            val themePreference by settingsDataStore.isDarkTheme.collectAsStateWithLifecycle(initialValue = null)
            val systemDark = isSystemInDarkTheme()
            val isDark = themePreference ?: true  // default dark theme

            // Check if terms need to be shown
            val needsStudentTerms = isLoggedIn && userRole == AppUserRole.STUDENT && !termsAccepted
            val needsTeacherGeneralTerms = isLoggedIn && userRole == AppUserRole.TEACHER && !termsAccepted
            val needsTeacherWorkTerms = isLoggedIn && userRole == AppUserRole.TEACHER && termsAccepted && !teacherTermsAccepted

            UnlockTheme(darkTheme = isDark) {
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
                            isLoggedIn = isLoggedIn,
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
