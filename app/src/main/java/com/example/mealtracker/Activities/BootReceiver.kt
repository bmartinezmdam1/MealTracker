package com.example.mealtracker.Activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

abstract class BootReceiver : BroadcastReceiver() {
    class BootReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
            rescheduleReminder(context)
        }

        private fun rescheduleReminder(context: Context) {
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmMgr.canScheduleExactAlarms()) {
            return
        }

        val pi = PendingIntent.getBroadcast(
            context,
            ReminderReceiver.REQUEST_CODE_REMINDER,
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
}
}