package com.example.mealtracker.Activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mealtracker.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class PerfilActivity : AppCompatActivity() {

    private val viewModel: FoodViewModel by viewModels()
    private lateinit var dbHelper: DBHelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        dbHelper = DBHelper(this)

        val tvUserInfo = findViewById<TextView>(R.id.tv_user_info)
        val btnChangeEmail = findViewById<Button>(R.id.btn_change_email)
        val btnChangePassword = findViewById<Button>(R.id.btn_change_password)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Cargar y mostrar información del usuario
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT email FROM usuarios LIMIT 1", null)
        var currentEmail: String? = null
        if (cursor.moveToFirst()) {
            currentEmail = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            tvUserInfo.text = "Correo actual: $currentEmail"
        } else {
            tvUserInfo.text = "No se ha iniciado sesión"
        }
        cursor.close()
        db.close()

        // Botones de cambio
        btnChangeEmail.setOnClickListener {
            val dialog = DialogInputFragment("Cambiar correo", 0) {}
            dialog.show(supportFragmentManager, "ChangeEmail")
        }

        btnChangePassword.setOnClickListener {
            val dialog = DialogInputFragment("Cambiar contraseña", 0) {}
            dialog.show(supportFragmentManager, "ChangePassword")
        }

        // Navegación inferior
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_inicio -> {
                    startActivity(Intent(this, InicioActivity::class.java))
                    finish()
                    true
                }
                R.id.cambiar_dieta -> {
                    startActivity(Intent(this, DietaActivity::class.java))
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
