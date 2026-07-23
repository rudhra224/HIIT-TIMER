package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.HiitTimerTheme
import com.example.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val settingsViewModel: SettingsViewModel = viewModel()
      val userSettings by settingsViewModel.userSettingsState.collectAsStateWithLifecycle()
      val settings = userSettings ?: com.example.data.UserSettings()

      HiitTimerTheme(
        themeMode = settings.themeMode,
        accentHex = settings.accentColorHex
      ) {
        Surface(modifier = Modifier.fillMaxSize()) {
          AppNavigation(settingsViewModel = settingsViewModel)
        }
      }
    }
  }
}
