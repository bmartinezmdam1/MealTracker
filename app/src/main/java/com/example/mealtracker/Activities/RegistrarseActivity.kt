package com.example.mealtracker.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mealtracker.Activities.MainActivity
import com.example.mealtracker.R
import com.google.firebase.firestore.FirebaseFirestore

class RegistrarseActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var editTextNombre: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextContrasena: EditText
    private lateinit var editTextConfirmarContrasena: EditText
    private lateinit var btnRegistrarse: Button
    private lateinit var btnIniciarSesion: Button
    private lateinit var checkBoxTerminos: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrarse)

        // Inicialización de vistas
        editTextNombre = findViewById(R.id.name)
        editTextEmail = findViewById(R.id.editTextTextEmailAddress)
        editTextContrasena = findViewById(R.id.password)
        editTextConfirmarContrasena = findViewById(R.id.repeatPassword)
        btnRegistrarse = findViewById(R.id.btn_registrarse_registro)
        btnIniciarSesion = findViewById(R.id.button_iniciar_sesion_registro)
        checkBoxTerminos = findViewById(R.id.checkbox_meat)

        btnRegistrarse.setOnClickListener {
            // Verifica que el checkbox esté marcado
            if (!checkBoxTerminos.isChecked) {
                Toast.makeText(this, "Debe aceptar los términos y condiciones", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validaciones existentes
            if (!isUsernameValid()) return@setOnClickListener
            if (!isPasswordValid()) return@setOnClickListener

            val email = editTextEmail.text.toString()
            if (!isEmailValid(email)) return@setOnClickListener

            val nombre = editTextNombre.text.toString()

            db.collection("users").document(email).get()
                .addOnFailureListener {
                    Toast.makeText(this, "Error al comprobar el usuario.", Toast.LENGTH_SHORT).show()
                }
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Toast.makeText(this, "Ya existe una cuenta con este correo.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val data = hashMapOf(
                        "name" to nombre,
                        "email" to email,
                        "password" to editTextContrasena.text.toString()
                    )

                    db.collection("users").document(email).set(data)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registro exitoso.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al guardar el registro. Contacte con soporte.", Toast.LENGTH_SHORT).show()
                        }
                }
        }

        btnIniciarSesion.setOnClickListener {
            finish()
        }
    }

    private fun isUsernameValid(): Boolean {
        val username = editTextNombre.text.toString()
        if (username.length !in 6..20) {
            Toast.makeText(this, "El nombre de usuario debe tener entre 6 y 20 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun isEmailValid(email: String): Boolean {
        // Verifica si el correo electrónico tiene un formato válido o termina con "@gmail.com"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() || !email.endsWith("@gmail.com")) {
            Toast.makeText(this, "El email no es válido o no es de tipo @gmail.com", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }


    private fun isPasswordValid(): Boolean {
        val password = editTextContrasena.text.toString()
        val passwordConfirmar = editTextConfirmarContrasena.text.toString()

        if (password.length !in 6..20) {
            Toast.makeText(this, "La contraseña debe tener entre 6 y 20 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != passwordConfirmar) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
