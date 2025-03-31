package com.example.mealtracker.Activities

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Modelo para los objetivos nutricionales
class DBHelper(context: Context) : SQLiteOpenHelper(context, "meal_tracker_db", null, 2) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            """CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT,
                contrasena TEXT
            )"""
        )

        db?.execSQL(
            """CREATE TABLE IF NOT EXISTS objetivos_nutricionales (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                calorias INTEGER,
                proteinas INTEGER,
                carbohidratos INTEGER,
                grasas INTEGER,
                vitamina_a INTEGER,
                vitamina_c INTEGER
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS usuarios")
        db?.execSQL("DROP TABLE IF EXISTS objetivos_nutricionales")
        onCreate(db)
    }

    fun guardarObjetivosNutricionales(
        calorias: Int,
        proteinas: Int,
        carbohidratos: Int,
        grasas: Int,
        vitaminaA: Int,
        vitaminaC: Int
    ) {
        val db = writableDatabase
        db.execSQL("DELETE FROM objetivos_nutricionales") // Aseguramos una sola fila
        val values = ContentValues().apply {
            put("calorias", calorias)
            put("proteinas", proteinas)
            put("carbohidratos", carbohidratos)
            put("grasas", grasas)
            put("vitamina_a", vitaminaA)
            put("vitamina_c", vitaminaC)
        }
        db.insert("objetivos_nutricionales", null, values)
        db.close()
    }

    fun cargarObjetivosNutricionales(): NutritionGoals? {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM objetivos_nutricionales LIMIT 1", null)
        val goals = if (cursor.moveToFirst()) {
            NutritionGoals(
                calories = cursor.getInt(cursor.getColumnIndexOrThrow("calorias")),
                protein = cursor.getInt(cursor.getColumnIndexOrThrow("proteinas")),
                carbs = cursor.getInt(cursor.getColumnIndexOrThrow("carbohidratos")),
                fats = cursor.getInt(cursor.getColumnIndexOrThrow("grasas")),
                vitaminA = cursor.getInt(cursor.getColumnIndexOrThrow("vitamina_a")),
                vitaminC = cursor.getInt(cursor.getColumnIndexOrThrow("vitamina_c"))
            )
        } else null
        cursor.close()
        db.close()
        return goals
    }

    // 🔧 Añade esto para evitar el error
    fun getWritableDb(): SQLiteDatabase {
        return writableDatabase
    }

    fun getReadableDb(): SQLiteDatabase {
        return readableDatabase
    }
}
