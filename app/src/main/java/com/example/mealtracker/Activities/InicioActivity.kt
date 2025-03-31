package com.example.mealtracker.Activities

import FoodItem
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealtracker.R

class InicioActivity : AppCompatActivity() {

    private val foodViewModel: FoodViewModel by viewModels()
    private lateinit var foodAdapter: FoodAdapter

    // Se agregan las vistas necesarias
    private lateinit var rvFoodList: RecyclerView
    private lateinit var btnAddFood: Button
    private lateinit var tvCaloriesSummary: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inicio_activity)

        // Referencias a vistas
        rvFoodList = findViewById(R.id.rv_food_list)
        btnAddFood = findViewById(R.id.btn_add_food)
        tvCaloriesSummary = findViewById(R.id.tv_calories_summary)

        // Inicializar RecyclerView y Adapter
        foodAdapter = FoodAdapter(emptyList())
        rvFoodList.layoutManager = LinearLayoutManager(this)
        rvFoodList.adapter = foodAdapter

        // 🔥 Observar cambios en la lista de comidas para actualizar el RecyclerView y las calorías
        foodViewModel.foodList.observe(this) { foodItems ->
            foodAdapter.updateData(foodItems)
            updateCalorieSummary(foodItems)
        }

        // Botón para abrir la pantalla de añadir comida
        btnAddFood.setOnClickListener {
            val intent = Intent(this, AnadirComida::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_FOOD)
        }

        // (Opcional) Mostrar detalle al tocar resumen
        tvCaloriesSummary.setOnClickListener {
            val intent = Intent(this, NutrientesDiarios::class.java)
            val foodList = foodViewModel.foodList.value ?: arrayListOf()
            intent.putParcelableArrayListExtra("food_list", ArrayList(foodList))
            startActivity(intent)
        }
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
                foodViewModel.addFood(food)
            }
        }
    }

    private fun updateCalorieSummary(foodItems: List<FoodItem>) {
        val totalCalories = foodItems.sumOf { it.calories }
        val calorieGoal = 2000 // Se puede hacer dinámico en el futuro
        val remainingCalories = calorieGoal - totalCalories
        tvCaloriesSummary.text = "Calorías: $totalCalories / $calorieGoal (Restantes: $remainingCalories)"
    }

    companion object {
        private const val REQUEST_CODE_ADD_FOOD = 1
    }
}

