package com.example.mealtracker.Activities

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        // Only handle BOOT_COMPLETED
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        rescheduleReminder(context)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun rescheduleReminder(context: Context) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context,
            ReminderReceiver.REQUEST_CODE_REMINDER,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val next10am = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmMgr.setAlarmClock(
            AlarmManager.AlarmClockInfo(next10am.timeInMillis, pi),
            pi
        )
    }
}
