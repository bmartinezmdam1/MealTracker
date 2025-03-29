package com.example.mealtracker.Activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.mealtracker.R


class AnadirComida: AppCompatActivity() {
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
            val resultIntent = Intent().apply {
                putExtra("nombre", etFoodName.text.toString())
                putExtra("calorias", etCalories.text.toString())
                putExtra("proteina", etProtein.text.toString())
                putExtra("carbohidratos", etCarbs.text.toString())
                putExtra("grasas", etFats.text.toString())
                putExtra("vitamina_a", etVitaminA.text.toString())
                putExtra("vitamina_c", etVitaminC.text.toString())
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
    }
