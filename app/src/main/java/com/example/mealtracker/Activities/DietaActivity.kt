package com.example.mealtracker.Activities

import DBHelper
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mealtracker.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class DietaActivity : AppCompatActivity() {

    private val viewModel: FoodViewModel by viewModels()
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dieta)

        dbHelper = DBHelper(this)

        val btnCalories = findViewById<Button>(R.id.btn_change_calories)
        val btnProtein  = findViewById<Button>(R.id.btn_change_protein)
        val btnCarbs    = findViewById<Button>(R.id.btn_change_carbs)
        val btnFats     = findViewById<Button>(R.id.btn_change_fats)
        val btnVitA     = findViewById<Button>(R.id.btn_change_vitamin_a)
        val btnVitC     = findViewById<Button>(R.id.btn_change_vitamin_c)
        val bottomNav   = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Carga objetivos de la BD
        dbHelper.cargarObjetivosNutricionales()?.let { goals ->
            viewModel.updateMacros(
                protein  = goals.protein,
                carbs    = goals.carbs,
                fats     = goals.fats,
                vitaminA = goals.vitaminA,
                vitaminC = goals.vitaminC
            )
            viewModel.updateCalories(goals.calories)
        }

        btnCalories.setOnClickListener {
            val current = viewModel.goals.value?.calories ?: 2000
            showDialog("Calorías", current) { newCal ->
                viewModel.updateCalories(newCal)
                saveToDB()
            }
        }
        btnProtein.setOnClickListener {
            val current = viewModel.goals.value?.protein ?: 0
            showDialog("Proteínas (g)", current) { newVal -> updateMacro(protein = newVal) }
        }
        btnCarbs.setOnClickListener {
            val current = viewModel.goals.value?.carbs ?: 0
            showDialog("Carbohidratos (g)", current) { newVal -> updateMacro(carbs = newVal) }
        }
        btnFats.setOnClickListener {
            val current = viewModel.goals.value?.fats ?: 0
            showDialog("Grasas (g)", current) { newVal -> updateMacro(fats = newVal) }
        }
        btnVitA.setOnClickListener {
            val current = viewModel.goals.value?.vitaminA ?: 0
            showDialog("Vitamina A (UI)", current) { newVal -> updateMacro(vitA = newVal) }
        }
        btnVitC.setOnClickListener {
            val current = viewModel.goals.value?.vitaminC ?: 0
            showDialog("Vitamina C (mg)", current) { newVal -> updateMacro(vitC = newVal) }
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_inicio -> {
                    startActivity(Intent(this, InicioActivity::class.java))
                    finish()
                    true
                }
                R.id.page_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    finish()
                    true
                }
                R.id.cambiar_dieta -> true
                else -> false
            }
        }
            bottomNav.selectedItemId = R.id.cambiar_dieta
    }

    private fun showDialog(title: String, current: Int, onConfirm: (Int) -> Unit) {
        DialogInputFragment(title, current, onConfirm)
            .show(supportFragmentManager, "DialogInput")
    }

    private fun updateMacro(
        protein: Int? = null,
        carbs:   Int? = null,
        fats:    Int? = null,
        vitA:    Int? = null,
        vitC:    Int? = null
    ) {
        val g = viewModel.goals.value ?: return
        viewModel.updateMacros(
            protein  = protein ?: g.protein,
            carbs    = carbs   ?: g.carbs,
            fats     = fats    ?: g.fats,
            vitaminA = vitA    ?: g.vitaminA,
            vitaminC = vitC    ?: g.vitaminC
        )
        saveToDB()
    }

    private fun saveToDB() {
        viewModel.goals.value?.let { g ->
            dbHelper.guardarObjetivosNutricionales(
                calorias     = g.calories,
                proteinas    = g.protein,
                carbohidratos= g.carbs,
                grasas       = g.fats,
                vitaminaA    = g.vitaminA,
                vitaminaC    = g.vitaminC
            )
        }
    }

    private fun navigateTo(cls: Class<*>) =
        Intent(this, cls).also { startActivity(it); finish() }

}