package com.example.branch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.branch.theme.BranchTheme

import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.RESUMED) {
        while (isActive) {
          withContext(Dispatchers.IO) {
            try {
              val app = application as BranchApplication
              app.syncManager.syncToCloud()
              app.syncManager.syncFromCloud()
            } catch (e: Exception) { e.printStackTrace() }
          }
          delay(60_000)
        }
      }
    }

    setContent {
      BranchTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MainNavigation()
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    val intent = android.content.Intent(this, com.example.branch.reminder.ReminderService::class.java)
    intent.action = "STOP_REMINDER"
    startService(intent)
  }

  override fun onPause() {
    super.onPause()
    com.example.branch.widget.BranchWidgetProvider.triggerUpdate(this)
    com.example.branch.widget.GymWidgetProvider.triggerUpdate(this)
    com.example.branch.widget.FlowWidgetProvider.triggerUpdate(this)
  }

  override fun onDestroy() {
    super.onDestroy()
    com.example.branch.glyph.GlyphAppController.turnOff()
  }
}

