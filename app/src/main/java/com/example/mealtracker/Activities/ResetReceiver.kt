package com.example.mealtracker.Activities


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

class ResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // ⚠️ No se puede acceder directamente al ViewModel desde aquí, así que usa SharedPreferences o una base de datos
        // Aquí ejemplo básico: borra las comidas almacenadas en base de datos o preferencias

        // Si usas un repositorio o base de datos, puedes llamarlo aquí.
        val sharedPrefs = context.getSharedPreferences("meal_tracker_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().remove("comidas_guardadas").apply()

        // Si tienes lógica para resetear el ViewModel, deberías llamarla desde una capa superior al abrir la app.
        // Por ejemplo: puedes marcar un "flag" y que el ViewModel lo verifique al cargar.
    }
}
