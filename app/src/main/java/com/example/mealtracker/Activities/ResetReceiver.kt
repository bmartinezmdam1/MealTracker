package com.example.mealtracker.Activities


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

class ResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val sharedPrefs = context.getSharedPreferences("meal_tracker_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().remove("comidas_guardadas").apply()

    }
}
