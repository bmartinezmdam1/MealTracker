package com.example.mealtracker.Activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mealtracker.R

class AnadirComida : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.anadir_comida)

        val etFoodName: EditText = findViewById(R.id.et_food_name)
        val etCalories: EditText = findViewById(R.id.et_calories)
        val etProtein: EditText = findViewById(R.id.et_protein)
        val etCarbs: EditText = findViewById(R.id.et_carbs)
        val etFats: EditText = findViewById(R.id.et_fats)
        val etVitaminA: EditText = findViewById(R.id.et_vitamin_a)
        val etVitaminC: EditText = findViewById(R.id.et_vitamin_c)
        val btnSave: Button = findViewById(R.id.btn_save)

        btnSave.setOnClickListener {
            val name = etFoodName.text.toString()
            val caloriesStr = etCalories.text.toString()
            val proteinStr = etProtein.text.toString()
            val carbsStr = etCarbs.text.toString()
            val fatsStr = etFats.text.toString()
            val vitaminAStr = etVitaminA.text.toString()
            val vitaminCStr = etVitaminC.text.toString()

            // Parse values or default to 0
            val calories = caloriesStr.toIntOrNull() ?: 0
            val protein = proteinStr.toIntOrNull() ?: 0
            val carbs = carbsStr.toIntOrNull() ?: 0
            val fats = fatsStr.toIntOrNull() ?: 0
            val vitaminA = vitaminAStr.toIntOrNull() ?: 0
            val vitaminC = vitaminCStr.toIntOrNull() ?: 0

            val realCalories = (protein * 4) + (carbs * 4) + (fats * 9)

            val tolerance = 0.05
            val difference = kotlin.math.abs(calories - realCalories)
            if (realCalories > 0 && difference > realCalories * tolerance) {
                Toast.makeText(
                    this,
                    "Las calorías no coinciden con los macros. Revisa los datos.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val resultIntent = Intent().apply {
                putExtra("nombre", name)
                putExtra("calorias", calories.toString())
                putExtra("proteinas", protein.toString())
                putExtra("carbohidratos", carbs.toString())
                putExtra("grasas", fats.toString())
                putExtra("vitamina_a", vitaminA.toString())
                putExtra("vitamina_c", vitaminC.toString())
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
