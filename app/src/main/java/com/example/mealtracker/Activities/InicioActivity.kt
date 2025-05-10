package com.example.mealtracker.Activities

import DBHelper
import FoodItem
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealtracker.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.provider.Settings

class InicioActivity : AppCompatActivity() {

    private lateinit var foodAdapter: FoodAdapter
    private lateinit var dbHelper: DBHelper
    private val viewModel: FoodViewModel by viewModels()

    private lateinit var rvFoodList: RecyclerView
    private lateinit var btnAddFood: Button
    private lateinit var tvCaloriesSummary: TextView

    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) scheduleDailyReminder()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inicio_activity)

        // Initialize UI components
        rvFoodList = findViewById(R.id.rv_food_list)
        btnAddFood = findViewById(R.id.btn_add_food)
        tvCaloriesSummary = findViewById(R.id.tv_calories_summary)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Database initialization
        dbHelper = DBHelper(this)

        // Setup RecyclerView
        foodAdapter = FoodAdapter(emptyList()) { food -> showDeleteDialog(food) }
        rvFoodList.layoutManager = LinearLayoutManager(this)
        rvFoodList.adapter = foodAdapter

        // ViewModel observers
        viewModel.foodList.observe(this) { foodList ->
            foodAdapter.updateData(foodList)
            updateCalorieSummary()
        }

        // Notification permission handling
        handleNotificationPermission()

        // Button click listeners
        btnAddFood.setOnClickListener {
            startActivityForResult(
                Intent(this, AnadirComida::class.java),
                REQUEST_CODE_ADD_FOOD
            )
        }

        tvCaloriesSummary.setOnClickListener {
            startActivity(
                Intent(this, NutrientesDiarios::class.java).apply {
                    putParcelableArrayListExtra(
                        "food_list",
                        ArrayList(viewModel.foodList.value ?: emptyList())
                    )
                }
            )
        }

        // Bottom navigation setup
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

        // Initial data load
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        dbHelper.cargarObjetivosNutricionales()?.let { g ->
            viewModel.updateMacros(
                protein  = g.protein,
                carbs    = g.carbs,
                fats     = g.fats,
                vitaminA = g.vitaminA,
                vitaminC = g.vitaminC
            )
            viewModel.updateCalories(g.calories)
        }
        viewModel.setFoodList(dbHelper.obtenerAlimentosConsumidosPorFecha(todayStr))
        viewModel.foodList.observe(this) { list ->
            foodAdapter.updateData(list)
            updateCalorieSummary()  //
        }
        // Schedule alarms
        scheduleMidnightReset()
        scheduleDailyReminder()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_FOOD && resultCode == Activity.RESULT_OK) {
            data?.let {
                val item = FoodItem(
                    name = it.getStringExtra("nombre") ?: "",
                    calories = it.getStringExtra("calorias")?.toIntOrNull() ?: 0,
                    protein = it.getStringExtra("proteinas")?.toIntOrNull() ?: 0,
                    carbs = it.getStringExtra("carbohidratos")?.toIntOrNull() ?: 0,
                    fats = it.getStringExtra("grasas")?.toIntOrNull() ?: 0,
                    vitaminA = it.getStringExtra("vitamina_a")?.toIntOrNull() ?: 0,
                    vitaminC = it.getStringExtra("vitamina_c")?.toIntOrNull() ?: 0,
                    totalGrams = it.getStringExtra("gramos")?.toIntOrNull() ?: 0
                )
                viewModel.addFood(item)
            }
        }
    }

    private fun showDeleteDialog(food: FoodItem) {
        AlertDialog.Builder(this)
            .setTitle("¿Borrar entrada?")
            .setMessage("¿Deseas borrar \"${food.name}\"?")
            .setPositiveButton("Borrar") { _, _ ->
                viewModel.removeFood(food)
                dbHelper.eliminarAlimentoConsumidoPorId(food.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun updateCalorieSummary() {
        tvCaloriesSummary.text = viewModel.getCaloriesSummary()
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleMidnightReset() {
        val mgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            this, 1, Intent(this, ResetReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DATE, 1)
        }
        mgr.setExact(AlarmManager.RTC_WAKEUP, midnight.timeInMillis, pi)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleDailyReminder() {
        val mgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !mgr.canScheduleExactAlarms()) {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            return
        }
        val pi = PendingIntent.getBroadcast(
            this, ReminderReceiver.REQUEST_CODE_REMINDER,
            Intent(this, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }
        mgr.setAlarmClock(AlarmManager.AlarmClockInfo(cal.timeInMillis, pi), pi)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        getSharedPreferences("app_usage", MODE_PRIVATE).edit()
            .putLong("last_interaction", System.currentTimeMillis())
            .apply()
    }

    private fun handleNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {}
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_ADD_FOOD = 1
    }
}