package com.subnetik.unlock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subnetik.unlock.data.local.datastore.AuthDataStore
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isLoggedIn by authDataStore.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
            val hasSeenOnboarding by settingsDataStore.hasSeenOnboarding.collectAsStateWithLifecycle(initialValue = true)
            val userRoleStr by authDataStore.userRole.collectAsStateWithLifecycle(initialValue = null)
            val userRole = AppUserRole.resolve(isLoggedIn, userRoleStr)
            val scope = rememberCoroutineScope()
            val themePreference by settingsDataStore.isDarkTheme.collectAsStateWithLifecycle(initialValue = null)
            val systemDark = isSystemInDarkTheme()
            val isDark = themePreference ?: systemDark

            UnlockTheme(darkTheme = isDark) {
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
