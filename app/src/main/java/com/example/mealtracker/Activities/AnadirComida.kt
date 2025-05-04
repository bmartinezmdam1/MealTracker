package com.example.mealtracker.Activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mealtracker.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnadirComida : AppCompatActivity() {

    private lateinit var etFoodName: EditText
    private lateinit var etProtein: EditText
    private lateinit var etCarbs: EditText
    private lateinit var etFats: EditText
    private lateinit var etVitaminA: EditText
    private lateinit var etVitaminC: EditText
    private lateinit var etTotalGrams: EditText
    private lateinit var btnSave: Button
    private lateinit var tvCalories: TextView

    private lateinit var dbHelper: DBHelper

    private var totalGrams = 0
    private var calculatedCalories = 0

    private var proteinPer100g = 0
    private var carbsPer100g = 0
    private var fatsPer100g = 0
    private var vitaminAPer100g = 0
    private var vitaminCPer100g = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.anadir_comida)

        dbHelper = DBHelper(this)

        // Enlazar vistas
        etFoodName = findViewById(R.id.et_food_name)
        etProtein = findViewById(R.id.et_protein)
        etCarbs = findViewById(R.id.et_carbs)
        etFats = findViewById(R.id.et_fats)
        etVitaminA = findViewById(R.id.et_vitamin_a)
        etVitaminC = findViewById(R.id.et_vitamin_c)
        etTotalGrams = findViewById(R.id.et_total_grams)
        btnSave = findViewById(R.id.btn_save)
        tvCalories = findViewById(R.id.tv_calories)

        // Autocompletar si el alimento ya existe
        etFoodName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val nombre = etFoodName.text.toString()
                val alimento = dbHelper.obtenerAlimentoPorNombre(nombre)
                alimento?.let {
                    proteinPer100g = it.proteinas
                    carbsPer100g = it.carbohidratos
                    fatsPer100g = it.grasas
                    vitaminAPer100g = it.vitaminaA
                    vitaminCPer100g = it.vitaminaC
                    updateValues()
                }
            }
        }

        // Escuchar cambios de gramos
        etTotalGrams.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = updateValues()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnSave.setOnClickListener {
            val name = etFoodName.text.toString().trim()
            totalGrams = etTotalGrams.text.toString().toIntOrNull() ?: 0

            if (name.isBlank()) {
                Toast.makeText(this, "Ingresa un nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (totalGrams == 0) {
                Toast.makeText(this, "Ingresa una cantidad válida de gramos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guardar alimento base si no existe
            dbHelper.guardarAlimentoSiNoExiste(
                name,
                proteinPer100g,
                carbsPer100g,
                fatsPer100g,
                vitaminAPer100g,
                vitaminCPer100g
            )

            val protein = etProtein.text.toString().toIntOrNull() ?: 0
            val carbs = etCarbs.text.toString().toIntOrNull() ?: 0
            val fats = etFats.text.toString().toIntOrNull() ?: 0
            val vitaminA = etVitaminA.text.toString().toIntOrNull() ?: 0
            val vitaminC = etVitaminC.text.toString().toIntOrNull() ?: 0

            calculatedCalories = (protein * 4) + (carbs * 4) + (fats * 9)

            // Insertar alimento consumido en la base de datos
            val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            dbHelper.insertarAlimentoConsumido(
                name,
                calculatedCalories,
                protein,
                carbs,
                fats,
                vitaminA,
                vitaminC,
                totalGrams,
                fechaActual
            )

            val resultIntent = Intent().apply {
                putExtra("nombre", name)
                putExtra("calorias", calculatedCalories.toString())
                putExtra("proteinas", protein.toString())
                putExtra("carbohidratos", carbs.toString())
                putExtra("grasas", fats.toString())
                putExtra("vitamina_a", vitaminA.toString())
                putExtra("vitamina_c", vitaminC.toString())
                putExtra("gramos", totalGrams.toString())
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun updateValues() {
        totalGrams = etTotalGrams.text.toString().toIntOrNull() ?: 0
        if (totalGrams == 0) return

        val protein = (proteinPer100g * totalGrams) / 100
        val carbs = (carbsPer100g * totalGrams) / 100
        val fats = (fatsPer100g * totalGrams) / 100
        val vitaminA = (vitaminAPer100g * totalGrams) / 100
        val vitaminC = (vitaminCPer100g * totalGrams) / 100

        calculatedCalories = (protein * 4) + (carbs * 4) + (fats * 9)

        etProtein.setText(protein.toString())
        etCarbs.setText(carbs.toString())
        etFats.setText(fats.toString())
        etVitaminA.setText(vitaminA.toString())
        etVitaminC.setText(vitaminC.toString())

        tvCalories.text = "Calorías: $calculatedCalories kcal"
    }
}
