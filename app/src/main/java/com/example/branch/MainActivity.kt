package com.example.branch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.branch.theme.BranchTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      BranchTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation() } }
    }
  }

  override fun onPause() {
    super.onPause()
    com.example.branch.widget.BranchWidgetProvider.triggerUpdate(this)
    com.example.branch.widget.GymWidgetProvider.triggerUpdate(this)
    com.example.branch.widget.FlowWidgetProvider.triggerUpdate(this)
  }
}

