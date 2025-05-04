package com.example.mealtracker.Activities

import FoodItem
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(context: Context) : SQLiteOpenHelper(context, "meal_tracker_db", null, 4) {

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

        db?.execSQL(
            """CREATE TABLE IF NOT EXISTS alimentos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT UNIQUE,
                proteinas INTEGER,
                carbohidratos INTEGER,
                grasas INTEGER,
                vitamina_a INTEGER,
                vitamina_c INTEGER
            )"""
        )

        db?.execSQL(
            """CREATE TABLE IF NOT EXISTS alimentos_consumidos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT,
                calorias INTEGER,
                proteinas INTEGER,
                carbohidratos INTEGER,
                grasas INTEGER,
                vitamina_a INTEGER,
                vitamina_c INTEGER,
                gramos INTEGER,
                fecha TEXT
            )"""
        )

        Log.d("DBHelper", "onCreate ejecutado: tablas creadas")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS usuarios")
        db?.execSQL("DROP TABLE IF EXISTS objetivos_nutricionales")
        db?.execSQL("DROP TABLE IF EXISTS alimentos")
        db?.execSQL("DROP TABLE IF EXISTS alimentos_consumidos")
        onCreate(db)
        Log.d("DBHelper", "onUpgrade ejecutado: versión $oldVersion → $newVersion")
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
        db.execSQL("DELETE FROM objetivos_nutricionales")
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

    fun guardarAlimentoSiNoExiste(
        nombre: String,
        proteinas: Int,
        carbohidratos: Int,
        grasas: Int,
        vitaminaA: Int,
        vitaminaC: Int
    ) {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT * FROM alimentos WHERE nombre = ?", arrayOf(nombre))
        val existe = cursor.moveToFirst()
        cursor.close()

        if (!existe) {
            val values = ContentValues().apply {
                put("nombre", nombre)
                put("proteinas", proteinas)
                put("carbohidratos", carbohidratos)
                put("grasas", grasas)
                put("vitamina_a", vitaminaA)
                put("vitamina_c", vitaminaC)
            }
            db.insert("alimentos", null, values)
            Log.d("DBHelper", "Alimento guardado: $nombre")
        } else {
            Log.d("DBHelper", "Alimento ya existía: $nombre")
        }
        db.close()
    }

    fun obtenerAlimentoPorNombre(nombre: String): AlimentoPor100g? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM alimentos WHERE nombre = ?", arrayOf(nombre))
        val alimento = if (cursor.moveToFirst()) {
            AlimentoPor100g(
                proteinas = cursor.getInt(cursor.getColumnIndexOrThrow("proteinas")),
                carbohidratos = cursor.getInt(cursor.getColumnIndexOrThrow("carbohidratos")),
                grasas = cursor.getInt(cursor.getColumnIndexOrThrow("grasas")),
                vitaminaA = cursor.getInt(cursor.getColumnIndexOrThrow("vitamina_a")),
                vitaminaC = cursor.getInt(cursor.getColumnIndexOrThrow("vitamina_c"))
            )
        } else null
        cursor.close()
        db.close()
        return alimento
    }

    fun insertarAlimentoConsumido(
        nombre: String,
        calorias: Int,
        proteinas: Int,
        carbohidratos: Int,
        grasas: Int,
        vitaminaA: Int,
        vitaminaC: Int,
        gramos: Int,
        fecha: String
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("calorias", calorias)
            put("proteinas", proteinas)
            put("carbohidratos", carbohidratos)
            put("grasas", grasas)
            put("vitamina_a", vitaminaA)
            put("vitamina_c", vitaminaC)
            put("gramos", gramos)
            put("fecha", fecha)
        }
        db.insert("alimentos_consumidos", null, values)
        db.close()
    }

    fun obtenerAlimentosConsumidosPorFecha(fecha: String): List<FoodItem> {
        val alimentos = mutableListOf<FoodItem>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM alimentos_consumidos WHERE fecha = ?",
            arrayOf(fecha)
        )
        while (cursor.moveToNext()) {
            val alimento = FoodItem(
                name = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                calories = cursor.getInt(cursor.getColumnIndexOrThrow("calorias")),
                protein = cursor.getInt(cursor.getColumnIndexOrThrow("proteinas")),
                carbs = cursor.getInt(cursor.getColumnIndexOrThrow("carbohidratos")),
                fats = cursor.getInt(cursor.getColumnIndexOrThrow("grasas")),
                vitaminA = cursor.getInt(cursor.getColumnIndexOrThrow("vitamina_a")),
                vitaminC = cursor.getInt(cursor.getColumnIndexOrThrow("vitamina_c")),
                totalGrams = cursor.getInt(cursor.getColumnIndexOrThrow("gramos"))
            )
            alimentos.add(alimento)
        }
        cursor.close()
        db.close()
        return alimentos
    }

    fun eliminarAlimentosConsumidosPorFecha(fecha: String) {
        val db = writableDatabase
        db.delete("alimentos_consumidos", "fecha = ?", arrayOf(fecha))
        db.close()
    }
    fun getWritableDb(): SQLiteDatabase {
        return writableDatabase
    }

    fun obtenerTotalesDiariosAnteriores(fechaActual: String): List<DailyTotal> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
        SELECT fecha, 
        SUM(calorias) as total_calorias, 
        SUM(proteinas) as total_proteinas, 
        SUM(carbohidratos) as total_carbohidratos, 
        SUM(grasas) as total_grasas 
        FROM alimentos_consumidos 
        WHERE fecha < ? 
        GROUP BY fecha
        """.trimIndent(),
            arrayOf(fechaActual)
        )

        val dailyTotals = mutableListOf<DailyTotal>()
        while (cursor.moveToNext()) {
            val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
            val totalCalories = cursor.getInt(cursor.getColumnIndexOrThrow("total_calorias"))
            val totalProtein = cursor.getInt(cursor.getColumnIndexOrThrow("total_proteinas"))
            val totalCarbs = cursor.getInt(cursor.getColumnIndexOrThrow("total_carbohidratos"))
            val totalFats = cursor.getInt(cursor.getColumnIndexOrThrow("total_grasas"))
            dailyTotals.add(DailyTotal(fecha, totalCalories, totalProtein, totalCarbs, totalFats))
        }
        cursor.close()
        db.close()
        return dailyTotals
    }
}
