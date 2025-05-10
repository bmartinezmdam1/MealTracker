package com.example.mealtracker.Activities

import DBHelper
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Clear stored meals
        context.getSharedPreferences("meal_tracker_prefs", Context.MODE_PRIVATE)
            .edit().remove("comidas_guardadas").apply()

        // Delete yesterday’s records from DB
        val db = DBHelper(context)
        val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        db.eliminarAlimentosConsumidosPorFecha(fmt.format(yesterday.time))
    }
}
