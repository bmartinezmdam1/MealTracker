package com.example.mealtracker.Activities

import FoodItem
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealtracker.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class InicioActivity : AppCompatActivity() {

    private lateinit var foodAdapter: FoodAdapter
    private lateinit var dbHelper: DBHelper
    private val viewModel: FoodViewModel by viewModels()

    private lateinit var rvFoodList: RecyclerView
    private lateinit var btnAddFood: Button
    private lateinit var tvCaloriesSummary: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inicio_activity)

        // Pedir permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        rvFoodList = findViewById(R.id.rv_food_list)
        btnAddFood = findViewById(R.id.btn_add_food)
        tvCaloriesSummary = findViewById(R.id.tv_calories_summary)

        dbHelper = DBHelper(this)

        foodAdapter = FoodAdapter(emptyList()) { selectedFood ->
            mostrarDialogoBorrar(selectedFood)
        }
        rvFoodList.layoutManager = LinearLayoutManager(this)
        rvFoodList.adapter = foodAdapter

        viewModel.foodList.observe(this) {
            foodAdapter.updateData(it)
        }

        viewModel.totalCalories.observe(this) {
            updateCalorieSummary()
        }

        btnAddFood.setOnClickListener {
            val intent = Intent(this, AnadirComida::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_FOOD)
        }

        tvCaloriesSummary.setOnClickListener {
            val intent = Intent(this, NutrientesDiarios::class.java)
            intent.putParcelableArrayListExtra("food_list", ArrayList(viewModel.foodList.value ?: listOf()))
            startActivity(intent)
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_inicio -> true
                R.id.page_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    finish()
                    true
                }
                R.id.cambiar_dieta -> {
                    startActivity(Intent(this, DietaActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        scheduleMidnightReset()
        scheduleDailyReminder()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_FOOD && resultCode == Activity.RESULT_OK) {
            data?.let {
                val name = it.getStringExtra("nombre") ?: ""
                val calories = it.getStringExtra("calorias")?.toIntOrNull() ?: 0
                val protein = it.getStringExtra("proteinas")?.toIntOrNull() ?: 0
                val carbs = it.getStringExtra("carbohidratos")?.toIntOrNull() ?: 0
                val fats = it.getStringExtra("grasas")?.toIntOrNull() ?: 0
                val vitaminA = it.getStringExtra("vitamina_a")?.toIntOrNull() ?: 0
                val vitaminC = it.getStringExtra("vitamina_c")?.toIntOrNull() ?: 0
                val gramos = it.getStringExtra("gramos")?.toIntOrNull() ?: 0

                val foodItem = FoodItem(
                    name = name,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fats = fats,
                    vitaminA = vitaminA,
                    vitaminC = vitaminC,
                    totalGrams = gramos
                )

                viewModel.addFood(foodItem)
            }
        }
    }

    private fun mostrarDialogoBorrar(food: FoodItem) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("¿Borrar entrada?")
            .setMessage("¿Deseas borrar \"${food.name}\"?")
            .setPositiveButton("Borrar") { dialog, _ ->
                viewModel.removeFood(food)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    private fun updateCalorieSummary() {
        tvCaloriesSummary.text = viewModel.getCaloriesSummary()
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleMidnightReset() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ResetReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val midnight = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DATE, 1)
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, midnight.timeInMillis, pendingIntent)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleDailyReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check for exact alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return // Exit if permission not granted
            }
        }

        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }


    companion object {
        private const val REQUEST_CODE_ADD_FOOD = 1
    }
}
