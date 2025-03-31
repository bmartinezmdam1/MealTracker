package com.example.mealtracker.Activities

import com.example.mealtracker.R


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class PerfilActivity : AppCompatActivity() {

    private val viewModel: FoodViewModel by viewModels()
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        dbHelper = DBHelper(this)

        val etCalories = findViewById<EditText>(R.id.et_calories)
        val etProtein = findViewById<EditText>(R.id.et_protein)
        val etCarbs = findViewById<EditText>(R.id.et_carbs)
        val etFats = findViewById<EditText>(R.id.et_fats)
        val etVitaminA = findViewById<EditText>(R.id.et_vitamin_a)
        val etVitaminC = findViewById<EditText>(R.id.et_vitamin_c)
        val btnSave = findViewById<Button>(R.id.btn_save_goals)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Cargar datos guardados en la base de datos local
        dbHelper.cargarObjetivosNutricionales()?.let {
            viewModel.updateMacros(it.protein, it.carbs, it.fats, it.vitaminA, it.vitaminC)
            viewModel.updateCalories(it.calories)
        }

        viewModel.goals.observe(this) { goals ->
            etCalories.setText(goals.calories.toString())
            etProtein.setText(goals.protein.toString())
            etCarbs.setText(goals.carbs.toString())
            etFats.setText(goals.fats.toString())
            etVitaminA.setText(goals.vitaminA.toString())
            etVitaminC.setText(goals.vitaminC.toString())
        }

        btnSave.setOnClickListener {
            val protein = etProtein.text.toString().toIntOrNull()
            val carbs = etCarbs.text.toString().toIntOrNull()
            val fats = etFats.text.toString().toIntOrNull()
            val calories = etCalories.text.toString().toIntOrNull()
            val vitA = etVitaminA.text.toString().toIntOrNull() ?: 0
            val vitC = etVitaminC.text.toString().toIntOrNull() ?: 0

            if (protein != null && carbs != null && fats != null) {
                viewModel.updateMacros(protein, carbs, fats, vitA, vitC)
                val cal = protein * 4 + carbs * 4 + fats * 9
                dbHelper.guardarObjetivosNutricionales(cal, protein, carbs, fats, vitA, vitC)
                etCalories.setText(cal.toString())
            } else if (calories != null) {
                viewModel.updateCalories(calories)
                val g = viewModel.goals.value!!
                dbHelper.guardarObjetivosNutricionales(g.calories, g.protein, g.carbs, g.fats, g.vitaminA, g.vitaminC)
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_inicio -> {
                    startActivity(Intent(this, InicioActivity::class.java))
                    finish()
                    true
                }
                R.id.page_perfil -> true
                else -> false
            }
        }

        bottomNav.selectedItemId = R.id.page_perfil
    }
}
