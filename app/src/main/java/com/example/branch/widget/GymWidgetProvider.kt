package com.example.branch.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.RemoteViews
import com.example.branch.MainActivity
import com.example.branch.R
import com.example.branch.BranchApplication
import com.example.branch.domain.StreakCalculator
import com.example.branch.glyph.EmblemRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GymWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val app = context.applicationContext as BranchApplication
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = app.database
                val todayKey = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_DATE)
                
                val planDays = db.planDao().getAllSync()
                val gymDoneDates = db.doneDao().getDatesByCategorySync("gym")
                
                val gymStreak = StreakCalculator.deriveStreak("gym", planDays, gymDoneDates, todayKey)
                
                val planDay = db.planDao().getByDate(todayKey)
                val gymIsDone = db.doneDao().find("gym", todayKey) != null
                
                val gymPlanned = planDay?.hasGym == true && !gymIsDone
                
                // NothingEmblB (Gym)
                val gymBitmap = renderGlyphBitmap("gym", gymStreak, gymPlanned, 0xFF4E9C5C.toInt())
                
                appWidgetIds.forEach { widgetId ->
                    val views = RemoteViews(context.packageName, R.layout.single_glyph_widget)
                    views.setImageViewBitmap(R.id.glyph_image, gymBitmap)
                    views.setTextViewText(R.id.glyph_label, "GYM")
                    
                    val intent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context, 
                        1, 
                        intent, 
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                    
                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
    
    private fun renderGlyphBitmap(style: String, filledSections: Int, isPlannedToday: Boolean, activeColor: Int): Bitmap {
        val grid = EmblemRenderer.render(style, filledSections, pulsePhase = 1f, isPlannedToday = isPlannedToday)
        
        val width = 300
        val height = 300
        val cellSize = width / 13f
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.style = Paint.Style.FILL }
        
        val red = (activeColor shr 16) and 0xFF
        val green = (activeColor shr 8) and 0xFF
        val blue = activeColor and 0xFF
        
        for (i in 0 until 169) {
            val brightness = grid[i]
            if (brightness > 0) {
                val alpha = (brightness / 4095f * 255).toInt().coerceIn(0, 255)
                paint.color = android.graphics.Color.argb(alpha, red, green, blue)
                
                val col = i % 13
                val row = i / 13
                val cx = col * cellSize + cellSize / 2f
                val cy = row * cellSize + cellSize / 2f
                val radius = cellSize * 0.35f
                
                canvas.drawCircle(cx, cy, radius, paint)
            }
        }
        
        return bitmap
    }

    companion object {
        fun triggerUpdate(context: Context) {
            val intent = Intent(context, GymWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                android.content.ComponentName(context, GymWidgetProvider::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
