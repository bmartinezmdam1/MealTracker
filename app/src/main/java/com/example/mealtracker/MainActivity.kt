package com.example.mealtracker


import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import android.Manifest
class MainActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var editTextCorreo: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonEmpezar: Button
    private lateinit var buttonRegistro: Button
    private lateinit var verContraseña: ImageView
    private lateinit var localDb: SQLiteDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        // Pide permisos de notificación si es es necesario.
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permisos dados
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                askNotificationPermission()
            }
            else -> {
                // Pide permiso
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Inicializa la base de datos
        localDb = openOrCreateDatabase("OneDateDB", MODE_PRIVATE, null)
        // Crea la tabla de datos usuarios si no existe
        localDb.execSQL(
            "CREATE TABLE IF NOT EXISTS usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT, contrasena TEXT, token TEXT)"
        )

        // comprueba si el token existe
        val cursor = localDb.rawQuery("PRAGMA table_info(usuarios)", null)
        var tokenColumnExists = false
        while (cursor.moveToNext()) {
            val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            if (columnName == "token") {
                tokenColumnExists = true
                break
            }
        }
        cursor.close()

        // Agrega token si no existe
        if (!tokenColumnExists) {
            localDb.execSQL("ALTER TABLE usuarios ADD COLUMN token TEXT")
        }

        // comprueba si el usuario está loggeado
        verificarSesion()

        // Initialize UI components
        editTextCorreo = findViewById(R.id.correoElectronico)
        editTextPassword = findViewById(R.id.editContrasena)
        buttonEmpezar = findViewById(R.id.Button_Iniciar_Sesion)
        verContraseña = findViewById(R.id.imageViewPassword)
        buttonRegistro = findViewById(R.id.Button_Registrarse)

        // Set up the login button click listener
        buttonEmpezar.setOnClickListener {
            val email = editTextCorreo.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                comprobarUsuario(email, password)
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up the registration button click listener
        buttonRegistro.setOnClickListener {
            startActivity(Intent(this, RegistrarseActivity::class.java))
        }

        // Set up the show/hide password button click listener
        verContraseña.setOnClickListener {
            if (editTextPassword.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                verContraseña.setImageResource(R.drawable.ic_eye_close)
            } else {
                editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                verContraseña.setImageResource(R.drawable.ic_eye_open)
            }
            editTextPassword.setSelection(editTextPassword.text.length)
        }
    }


    fun actualizarTokenForzar() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Error al obtener el token", task.exception)
                return@addOnCompleteListener
            }
            val newToken = task.result
            Log.i("FCM", "Nuevo token obtenido: $newToken")

            // Actualiza la base de datos local (SQLite)
            localDb.execSQL("UPDATE usuarios SET token = '$newToken'")
            Log.i("FCM", "Token actualizado en SQLite: $newToken")

            // Obtenemos el email del usuario almacenado localmente (o el ID que uses en Firestore)
            val cursor = localDb.rawQuery("SELECT email FROM usuarios LIMIT 1", null)
            var storedEmail: String? = null
            if (cursor.moveToFirst()) {
                storedEmail = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            }
            cursor.close()

            // Actualiza el token en Firestore
            if (storedEmail != null) {
                db.collection("usuarios").document(storedEmail)
                    .update("token", newToken)
                    .addOnSuccessListener {
                        Log.i("FCM", "Token actualizado en Firestore: $newToken")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FCM", "Error actualizando token en Firestore", e)
                    }
            }
        }
    }


    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    // permiso aceptado
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Explica porqué el permiso es necesario y lo pide de nuevo
                    AlertDialog.Builder(this)
                        .setTitle("Permiso necesario")
                        .setMessage("Esta aplicación necesita permisos de notificación para funcionar correctamente.")
                        .setPositiveButton("Aceptar") { _, _ ->
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
                else -> {
                    // Directly ask for the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    // Registra el "request launcher"
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {

        } else {
            // si el persmiso no está aceptado...
            if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // El usuario denegó permanentemente el permiso, muestra un cuadro de diálogo para abrir la configuración de la aplicación.
                AlertDialog.Builder(this)
                    .setTitle("Permiso bloqueado")
                    .setMessage("Debes habilitar el permiso de notificaciones en la configuración de la aplicación.")
                    .setPositiveButton("Abrir ajustes") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", packageName, null)
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }


    private fun comprobarUsuario(email: String, password: String) {
        db.collection("usuarios")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.first()
                    val dbPassword = user.getString("contrasena")
                    val token = user.getString("token")

                    if (dbPassword == password) {
                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                        if (token != null) {
                            guardarCredencialesLocal(email, password, token)
                            actualizarTokenForzar()
                        }
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


    private fun guardarCredencialesLocal(email: String, password: String, token: String) {
        localDb.execSQL("DELETE FROM usuarios")
        localDb.execSQL("INSERT INTO usuarios (email, contrasena, token) VALUES ('$email', '$password', '$token')")
    }
}