package com.example.mealtracker.Activities

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
        val btnProtein = findViewById<Button>(R.id.btn_change_protein)
        val btnCarbs = findViewById<Button>(R.id.btn_change_carbs)
        val btnFats = findViewById<Button>(R.id.btn_change_fats)
        val btnVitA = findViewById<Button>(R.id.btn_change_vitamin_a)
        val btnVitC = findViewById<Button>(R.id.btn_change_vitamin_c)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        dbHelper.cargarObjetivosNutricionales()?.let {
            viewModel.updateMacros(it.protein, it.carbs, it.fats, it.vitaminA, it.vitaminC)
            viewModel.updateCalories(it.calories)
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
            showDialog("Proteínas (g)", current) { updateMacro(protein = it) }
        }

        btnCarbs.setOnClickListener {
            val current = viewModel.goals.value?.carbs ?: 0
            showDialog("Carbohidratos (g)", current) { updateMacro(carbs = it) }
        }

        btnFats.setOnClickListener {
            val current = viewModel.goals.value?.fats ?: 0
            showDialog("Grasas (g)", current) { updateMacro(fats = it) }
        }

        btnVitA.setOnClickListener {
            val current = viewModel.goals.value?.vitaminA ?: 0
            showDialog("Vitamina A (UI)", current) { updateMacro(vitA = it) }
        }

        btnVitC.setOnClickListener {
            val current = viewModel.goals.value?.vitaminC ?: 0
            showDialog("Vitamina C (mg)", current) { updateMacro(vitC = it) }
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
        val dialog = DialogInputFragment(title, current, onConfirm)
        dialog.show(supportFragmentManager, "DialogInput")
    }

    private fun updateMacro(
        protein: Int? = null,
        carbs: Int? = null,
        fats: Int? = null,
        vitA: Int? = null,
        vitC: Int? = null
    ) {
        val g = viewModel.goals.value ?: return
        val newProtein = protein ?: g.protein
        val newCarbs = carbs ?: g.carbs
        val newFats = fats ?: g.fats
        val newVitA = vitA ?: g.vitaminA
        val newVitC = vitC ?: g.vitaminC
        viewModel.updateMacros(newProtein, newCarbs, newFats, newVitA, newVitC)
        saveToDB()
    }

    private fun saveToDB() {
        val g = viewModel.goals.value ?: return
        dbHelper.guardarObjetivosNutricionales(
            g.calories, g.protein, g.carbs, g.fats, g.vitaminA, g.vitaminC
        )
    }
}
