package com.example.branch.ui.hub

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.branch.data.prefs.BranchPrefs
import com.example.branch.theme.*
import com.example.branch.ui.emblem.EmblemStyle
import com.example.branch.ui.emblem.EmblemView
import kotlinx.coroutines.launch

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

    Scaffold(
        
        containerColor = NothingBg
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Hub", 
                    style = MaterialTheme.typography.displayMedium, 
                    color = NothingText
                )
                Text(
                    text = "GLYPH MIRROR", 
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 4.sp), 
                    color = NothingMuted
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Gym Emblem
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Text("GYM", style = MaterialTheme.typography.labelMedium, color = GymPurple)
                Spacer(Modifier.width(8.dp))
                Text("$gymStreak/6", style = MaterialTheme.typography.labelMedium, color = NothingMuted)
            }
            EmblemView(filledSections = gymStreak, style = EmblemStyle.GYM, isPlannedToday = gymPlanned, size = 150.dp)
            
            Spacer(Modifier.height(48.dp))
            
            // Flow Emblem
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Text("FLOW", style = MaterialTheme.typography.labelMedium, color = FlowBlue)
                Spacer(Modifier.width(8.dp))
                Text("$flowStreak/6", style = MaterialTheme.typography.labelMedium, color = NothingMuted)
            }
            EmblemView(filledSections = flowStreak, style = EmblemStyle.FLOW, isPlannedToday = flowPlanned, size = 150.dp)
            
            Spacer(modifier = Modifier.weight(1f))

            val reminderEnabled by prefs.reminderEnabled.collectAsState(initial = false)
            val reminderTime by prefs.reminderTime.collectAsState(initial = "17:00")
            val coroutineScope = rememberCoroutineScope()

            val timePickerDialog = remember {
                android.app.TimePickerDialog(
                    context,
                    { _, hour, min ->
                        val formattedTime = String.format("%02d:%02d", hour, min)
                        coroutineScope.launch {
                            prefs.setReminderTime(formattedTime)
                            if (reminderEnabled) {
                                com.example.branch.reminder.AlarmScheduler.scheduleAlarm(context, true, formattedTime)
                            }
                        }
                    },
                    reminderTime.split(":").getOrNull(0)?.toIntOrNull() ?: 17,
                    reminderTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0,
                    true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.clickable { timePickerDialog.show() }) {
                    Text("Daily Glyph Reminder", style = MaterialTheme.typography.labelMedium, color = NothingText)
                    Text(reminderTime, style = MaterialTheme.typography.bodySmall, color = NothingMuted)
                }
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { enabled ->
                        coroutineScope.launch {
                            prefs.setReminderEnabled(enabled)
                            com.example.branch.reminder.AlarmScheduler.scheduleAlarm(context, enabled, reminderTime)
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = NothingText, checkedTrackColor = Color.DarkGray)
                )
            }
        }
    }
}
