package com.example.mealtracker.Activities

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "meal_tracker_db", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        // Crear la tabla cuando la base de datos se crea por primera vez
        db?.execSQL(
            """CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT,
                contrasena TEXT
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Implementar si necesitas actualizar la base de datos en el futuro
        db?.execSQL("DROP TABLE IF EXISTS usuarios")
        onCreate(db)
    }

    // Método para obtener una referencia de escritura a la base de datos
    fun getWritableDb(): SQLiteDatabase {
        return writableDatabase
    }

    // Método para obtener una referencia de solo lectura a la base de datos
    fun getReadableDb(): SQLiteDatabase {
        return readableDatabase
    }
}
