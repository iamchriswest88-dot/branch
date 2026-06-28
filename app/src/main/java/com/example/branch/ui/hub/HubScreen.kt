package com.example.branch.ui.hub

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.branch.data.prefs.BranchPrefs
import com.example.branch.theme.*
import com.example.branch.ui.emblem.EmblemStyle
import com.example.branch.ui.emblem.EmblemView

@Composable
fun HubScreen() {
    val context = LocalContext.current
    val prefs = remember { BranchPrefs(context) }
    
    val app = context.applicationContext as com.example.branch.BranchApplication
    val db = remember { app.database }
    val todayKey = remember { java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_DATE) }
    val planDay by db.planDao().getByDateFlow(todayKey).collectAsState(initial = null)
    
    val gymDone by db.doneDao().isDoneFlow("gym", todayKey).collectAsState(initial = false)
    val flowDone by db.doneDao().isDoneFlow("flow", todayKey).collectAsState(initial = false)
    
    val gymPlanned = planDay?.hasGym == true && !gymDone
    val flowPlanned = planDay?.hasFlow == true && !flowDone

    val planDays by db.planDao().getAll().collectAsState(initial = emptyList())
    val gymDoneDates by db.doneDao().getDatesByCategory("gym").collectAsState(initial = emptyList())
    val flowDoneDates by db.doneDao().getDatesByCategory("flow").collectAsState(initial = emptyList())

    val gymStreak = remember(planDays, gymDoneDates) {
        com.example.branch.domain.StreakCalculator.deriveStreak("gym", planDays, gymDoneDates, todayKey)
    }
    val flowStreak = remember(planDays, flowDoneDates) {
        com.example.branch.domain.StreakCalculator.deriveStreak("flow", planDays, flowDoneDates, todayKey)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = NothingBg) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gym Emblem
            Text("Gym", style = MaterialTheme.typography.titleLarge, color = NothingText)
            Text("$gymStreak/6 Sections", style = MaterialTheme.typography.bodyMedium, color = NothingMuted)
            Spacer(Modifier.height(16.dp))
            EmblemView(filledSections = gymStreak, style = EmblemStyle.GYM, isPlannedToday = gymPlanned, size = 180.dp)
            
            Spacer(Modifier.height(48.dp))
            
            // Flow Emblem
            Text("Flow", style = MaterialTheme.typography.titleLarge, color = NothingText)
            Text("$flowStreak/6 Sections", style = MaterialTheme.typography.bodyMedium, color = NothingMuted)
            Spacer(Modifier.height(16.dp))
            EmblemView(filledSections = flowStreak, style = EmblemStyle.FLOW, isPlannedToday = flowPlanned, size = 180.dp)
        }
    }
}
