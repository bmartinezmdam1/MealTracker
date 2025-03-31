package com.example.mealtracker.Activities

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mealtracker.R

class MainActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var editTextCorreo: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonEmpezar: Button
    private lateinit var buttonRegistro: Button
    private lateinit var dbHelper: DBHelper
    private lateinit var localDb: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa la base de datos
        dbHelper = DBHelper(this)
        localDb = dbHelper.getWritableDb()

        // Verifica si el usuario está logueado
        verificarSesion()

        // Inicializa los elementos de la UI
        editTextCorreo = findViewById(R.id.correoElectronico)
        editTextPassword = findViewById(R.id.editContrasena)
        buttonEmpezar = findViewById(R.id.Button_Iniciar_Sesion)
        buttonRegistro = findViewById(R.id.Button_Registrarse)

        // Botón de inicio de sesión
        buttonEmpezar.setOnClickListener {
            val email = editTextCorreo.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                comprobarUsuario(email, password)
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón de registro
        buttonRegistro.setOnClickListener {
            startActivity(Intent(this, RegistrarseActivity::class.java))
        }
    }

    private fun comprobarUsuario(email: String, password: String) {
        db.collection("users").document(email).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val dbPassword = document.getString("password")
                    if (dbPassword == password) {
                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                        guardarCredencialesLocal(email, password)
                        startActivity(Intent(this, InicioActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Usuario no registrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al conectar con la base de datos", Toast.LENGTH_SHORT).show()
            }
    }


        private fun verificarSesion() {
        val cursor = localDb.rawQuery("SELECT email, contrasena FROM usuarios", null)
        if (cursor.moveToFirst()) {
            val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            val password = cursor.getString(cursor.getColumnIndexOrThrow("contrasena"))
            cursor.close()
            comprobarUsuario(email, password)
        } else {
            cursor.close()
        }
    }

    private fun guardarCredencialesLocal(email: String, password: String) {
        // Usar transacciones
        localDb.beginTransaction()
        try {
            localDb.execSQL("DELETE FROM usuarios") // Elimina los datos previos
            val insertSQL = "INSERT INTO usuarios (email, contrasena) VALUES (?, ?)"
            val statement = localDb.compileStatement(insertSQL)
            statement.bindString(1, email)
            statement.bindString(2, password)
            statement.executeInsert()
            localDb.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            localDb.endTransaction()
        }
    }
}
