package com.example.mealtracker.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.mealtracker.R
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences("app_usage", Context.MODE_PRIVATE)
        val lastInteraction = prefs.getLong("last_interaction", 0L)

        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (lastInteraction >= startOfToday) return

        createNotificationChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Record Your Meals!")
            .setContentText("Don't forget to track your meals today!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            // FIXED: Use context instead of 'this'
            if (ContextCompat.checkSelfPermission(
                    context,  // Changed from 'this' to 'context'
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }

        rescheduleAlarm(context)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                CHANNEL_ID,
                "Recuerda contar tus calorías!",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {

                description = "No olvides contar tus calorías"
                context.getSystemService(NotificationManager::class.java)
                    ?.createNotificationChannel(this)
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun rescheduleAlarm(context: Context) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context,  // Fixed: Use context parameter instead of 'this'
            REQUEST_CODE_REMINDER,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val next10am = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmMgr.setAlarmClock(
            AlarmManager.AlarmClockInfo(next10am.timeInMillis, pi),
            pi
        )
    }

    companion object {
        const val CHANNEL_ID = "meal_reminder_channel"
        const val NOTIFICATION_ID = 1001
        const val REQUEST_CODE_REMINDER = 0
    }
}