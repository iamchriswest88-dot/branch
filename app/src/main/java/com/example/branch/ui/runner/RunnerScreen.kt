package com.example.branch.ui.runner

import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.branch.theme.*
import com.example.branch.ui.emblem.EmblemStyle
import com.example.branch.ui.emblem.EmblemView

@Composable
fun RunnerScreen(
    workoutId: String,
    onBack:    () -> Unit,
    vm: RunnerViewModel = viewModel(
        key     = "runner_$workoutId",
        factory = RunnerViewModel.factory(workoutId)
    )
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var muted by remember { mutableStateOf(false) }

    // Keep screen on for the duration of the session
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    when {
        state.isLoading  -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NothingRed)
        }
        state.isFinished -> CompletionScreen(
            category   = state.category,
            gymStreak  = state.gymStreak,
            flowStreak = state.flowStreak,
            onDone     = onBack
        )
        else -> ActiveRunnerScreen(state, muted,
            onPauseResume = { if (state.isPaused) vm.resume() else vm.pause() },
            onSkip        = vm::skip,
            onMute        = { muted = !muted; vm.audioMuted = muted },
            onExit        = onBack
        )
    }
}

@Composable
fun ActiveRunnerScreen(
    state:        RunnerState,
    muted:        Boolean,
    onPauseResume: () -> Unit,
    onSkip:       () -> Unit,
    onMute:       () -> Unit,
    onExit:       () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = NothingBg) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
        ) {
            val phaseColor = when (state.phaseLabel) {
                "WORK" -> NothingLeaf
                "REST" -> NothingRed
                else   -> NothingMuted
            }

            // Phase label + close
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    state.phaseLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = phaseColor
                )
                IconButton(onClick = onExit) {
                    Icon(Icons.Default.Close, "Exit", tint = NothingFaint)
                }
            }

            // Central clustered content
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Big countdown
                val countdownGrid = com.example.branch.glyph.CountdownRenderer.render(state.secondsLeft)
                com.example.branch.ui.emblem.GlyphMatrixView(
                    grid = countdownGrid,
                    activeColor = phaseColor,
                    size = 320.dp
                )
                
                Spacer(Modifier.height(24.dp))

                // Exercise name + side
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        state.exerciseName.uppercase(),
                        style     = MaterialTheme.typography.headlineSmall,
                        color     = NothingText,
                        textAlign = TextAlign.Center
                    )
                    if (state.sideLabel.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(state.sideLabel, style = MaterialTheme.typography.titleLarge, color = NothingLeaf)
                    }
                }

                Spacer(Modifier.height(40.dp))

                // Bottom grouped controls and progress
                Column(verticalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
                    // Progress
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val progress = if (state.totalSeconds > 0)
                            1f - state.secondsLeft.toFloat() / state.totalSeconds else 0f
                        LinearProgressIndicator(
                            progress   = { progress },
                            modifier   = Modifier.fillMaxWidth().height(3.dp),
                            color      = phaseColor,
                            trackColor = NothingLine
                        )
                        Text(
                            "SET ${state.currentSet} / ${state.totalSets}",
                            style     = MaterialTheme.typography.labelMedium,
                            color     = NothingMuted,
                            modifier  = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onMute) {
                            Icon(
                                if (muted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                "Mute",
                                tint = if (muted) NothingFaint else NothingMuted
                            )
                        }
                        FilledIconButton(
                            onClick = onPauseResume,
                            colors  = IconButtonDefaults.filledIconButtonColors(containerColor = NothingSurface2)
                        ) {
                            Icon(
                                if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                if (state.isPaused) "Resume" else "Pause",
                                tint = NothingText
                            )
                        }
                        IconButton(onClick = onSkip) {
                            Icon(Icons.Default.SkipNext, "Skip", tint = NothingMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompletionScreen(
    category:   String,
    gymStreak:  Int,
    flowStreak: Int,
    onDone:     () -> Unit
) {
    val style  = if (category == "gym") EmblemStyle.GYM else EmblemStyle.FLOW
    val streak = if (category == "gym") gymStreak else flowStreak
    val label  = if (category == "gym") "GYM" else "FLOW"

    Surface(modifier = Modifier.fillMaxSize(), color = NothingBg) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Text("Session Complete", style = MaterialTheme.typography.titleLarge, color = NothingRed)
            Spacer(Modifier.height(32.dp))
            EmblemView(filledSections = streak, style = style)
            Spacer(Modifier.height(24.dp))
            Text("$label • $streak/6 Sections", style = MaterialTheme.typography.headlineSmall, color = NothingText)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text("GYM  $gymStreak/6",  style = MaterialTheme.typography.bodyMedium, color = NothingMuted)
                Text("FLOW $flowStreak/6", style = MaterialTheme.typography.bodyMedium, color = NothingMuted)
            }
            Spacer(Modifier.height(40.dp))
            Button(onClick = onDone, colors = ButtonDefaults.buttonColors(containerColor = NothingRed)) {
                Text("Done", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
