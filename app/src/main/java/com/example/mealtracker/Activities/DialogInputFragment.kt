package com.example.mealtracker.Activities

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.mealtracker.R
import com.google.firebase.firestore.FirebaseFirestore

class DialogInputFragment(
    private val title: String,
    private val current: Int,
    private val onConfirm: (Int) -> Unit
) : DialogFragment() {

    constructor() : this("", 0, {}) // Required empty constructor

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return if (title.lowercase().contains("correo")) {
            createEmailDialog()
        } else if (title.lowercase().contains("contraseña")) {
            createPasswordDialog()
        } else {
            createNumericDialog()
        }
    }

    private fun createNumericDialog(): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null)
        val editText = view.findViewById<EditText>(R.id.et_input)
        editText.setText(current.toString())

        builder.setView(view)
            .setTitle("Cambiar $title")
            .setPositiveButton("Guardar") { _, _ ->
                val newValue = editText.text.toString().toIntOrNull()
                if (newValue != null) {
                    onConfirm(newValue)
                } else {
                    Toast.makeText(context, "Valor inválido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)

        return builder.create()
    }

    private fun createEmailDialog(): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_change_email, null)
        val etOldEmail = view.findViewById<EditText>(R.id.et_old_email)
        val etPassword = view.findViewById<EditText>(R.id.et_password)
        val etNewEmail = view.findViewById<EditText>(R.id.et_new_email)

        builder.setView(view)
            .setTitle("Cambiar correo")
            .setPositiveButton("Guardar") { _, _ ->
                val oldEmail = etOldEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val newEmail = etNewEmail.text.toString().trim()

                if (oldEmail.isNotEmpty() && password.isNotEmpty() && newEmail.isNotEmpty()) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(oldEmail).get().addOnSuccessListener { doc ->
                        if (doc.exists() && doc.getString("password") == password) {
                            val data = doc.data
                            if (data != null) {
                                db.collection("users").document(newEmail).set(data).addOnSuccessListener {
                                    db.collection("users").document(oldEmail).delete()
                                    val helper = DBHelper(requireContext())
                                    val localDb = helper.getWritableDatabase()
                                    localDb.execSQL("UPDATE usuarios SET email = ? WHERE email = ? AND contrasena = ?",
                                        arrayOf(newEmail, oldEmail, password))
                                    localDb.close()
                                    Toast.makeText(context, "Correo actualizado con éxito", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)

        return builder.create()
    }

    private fun createPasswordDialog(): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_change_password, null)
        val etEmail = view.findViewById<EditText>(R.id.et_email)
        val etOldPassword = view.findViewById<EditText>(R.id.et_old_password)
        val etNewPassword = view.findViewById<EditText>(R.id.et_new_password)

        builder.setView(view)
            .setTitle("Cambiar contraseña")
            .setPositiveButton("Guardar") { _, _ ->
                val email = etEmail.text.toString().trim()
                val oldPass = etOldPassword.text.toString().trim()
                val newPass = etNewPassword.text.toString().trim()

                if (email.isNotEmpty() && oldPass.isNotEmpty() && newPass.isNotEmpty()) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(email).get().addOnSuccessListener { doc ->
                        if (doc.exists() && doc.getString("password") == oldPass) {
                            db.collection("users").document(email).update("password", newPass).addOnSuccessListener {
                                val helper = DBHelper(requireContext())
                                val localDb = helper.getWritableDatabase()
                                localDb.execSQL("UPDATE usuarios SET contrasena = ? WHERE email = ? AND contrasena = ?",
                                    arrayOf(newPass, email, oldPass))
                                localDb.close()
                                Toast.makeText(context, "Contraseña actualizada con éxito", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)

        return builder.create()
    }
}
