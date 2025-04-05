package com.example.mealtracker.Activities

import FoodItem
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealtracker.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class InicioActivity : AppCompatActivity() {

    private lateinit var foodAdapter: FoodAdapter
    private lateinit var dbHelper: DBHelper

    private lateinit var rvFoodList: RecyclerView
    private lateinit var btnAddFood: Button
    private lateinit var tvCaloriesSummary: TextView

    private val foodList = mutableListOf<FoodItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inicio_activity)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        rvFoodList = findViewById(R.id.rv_food_list)
        btnAddFood = findViewById(R.id.btn_add_food)
        tvCaloriesSummary = findViewById(R.id.tv_calories_summary)

        dbHelper = DBHelper(this)

        foodAdapter = FoodAdapter(emptyList())
        rvFoodList.layoutManager = LinearLayoutManager(this)
        rvFoodList.adapter = foodAdapter

        updateCalorieSummary()

        btnAddFood.setOnClickListener {
            val intent = Intent(this, AnadirComida::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_FOOD)
        }

        tvCaloriesSummary.setOnClickListener {
            val intent = Intent(this, NutrientesDiarios::class.java)
            intent.putParcelableArrayListExtra("food_list", ArrayList(foodList))
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
                else -> false
            }
        }

        scheduleMidnightReset()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_FOOD && resultCode == Activity.RESULT_OK) {
            data?.let {
                val food = FoodItem(
                    name = it.getStringExtra("nombre") ?: "",
                    calories = it.getStringExtra("calorias")?.toIntOrNull() ?: 0,
                    protein = it.getStringExtra("proteinas")?.toIntOrNull() ?: 0,
                    carbs = it.getStringExtra("carbohidratos")?.toIntOrNull() ?: 0,
                    fats = it.getStringExtra("grasas")?.toIntOrNull() ?: 0,
                    vitaminA = it.getStringExtra("vitamina_a")?.toIntOrNull() ?: 0,
                    vitaminC = it.getStringExtra("vitamina_c")?.toIntOrNull() ?: 0
                )
                foodList.add(food)
                foodAdapter.updateData(foodList)
                updateCalorieSummary()
            }
        }
    }

    private fun updateCalorieSummary() {
        val totalCalories = foodList.sumOf { it.calories }
        val goal = dbHelper.cargarObjetivosNutricionales()
        val calorieGoal = goal?.calories ?: 2000
        val remainingCalories = calorieGoal - totalCalories
        tvCaloriesSummary.text = "Calorías: $totalCalories / $calorieGoal (Restantes: $remainingCalories)"
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

    companion object {
        private const val REQUEST_CODE_ADD_FOOD = 1
    }
}