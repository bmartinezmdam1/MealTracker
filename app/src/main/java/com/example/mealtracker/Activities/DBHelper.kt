import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.mealtracker.Activities.AlimentoPor100g
import com.example.mealtracker.Activities.DailyTotal
import com.example.mealtracker.Activities.NutritionGoals
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DBHelper(context: Context) : SQLiteOpenHelper(context, "meal_tracker_db", null, 4) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT,
                contrasena TEXT
            )
            """
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS objetivos_nutricionales (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                calorias INTEGER,
                proteinas INTEGER,
                carbohidratos INTEGER,
                grasas INTEGER,
                vitamina_a INTEGER,
                vitamina_c INTEGER
            )
            """
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS alimentos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT UNIQUE,
                proteinas INTEGER,
                carbohidratos INTEGER,
                grasas INTEGER,
                vitamina_a INTEGER,
                vitamina_c INTEGER
            )
            """
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS alimentos_consumidos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                food_id INTEGER,
                nombre TEXT,
                calorias INTEGER,
                proteinas INTEGER,
                carbohidratos INTEGER,
                grasas INTEGER,
                vitamina_a INTEGER,
                vitamina_c INTEGER,
                gramos INTEGER,
                fecha TEXT,
                FOREIGN KEY(food_id) REFERENCES alimentos(id)
            )
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        db.execSQL("DROP TABLE IF EXISTS objetivos_nutricionales")
        db.execSQL("DROP TABLE IF EXISTS alimentos")
        db.execSQL("DROP TABLE IF EXISTS alimentos_consumidos")
        onCreate(db)
        Log.d("DBHelper", "onUpgrade: $oldVersion -> $newVersion")
    }

    /**
     * Inserta un alimento base si no existe.
     */
    fun guardarAlimentoSiNoExiste(
        nombre: String,
        proteinas: Int,
        carbohidratos: Int,
        grasas: Int,
        vitaminaA: Int,
        vitaminaC: Int
    ) {
        writableDatabase.use { db ->
            db.rawQuery("SELECT id FROM alimentos WHERE nombre = ?", arrayOf(nombre)).use { cursor ->
                if (!cursor.moveToFirst()) {
                    ContentValues().apply {
                        put("nombre", nombre)
                        put("proteinas", proteinas)
                        put("carbohidratos", carbohidratos)
                        put("grasas", grasas)
                        put("vitamina_a", vitaminaA)
                        put("vitamina_c", vitaminaC)
                    }.let { values ->
                        db.insert("alimentos", null, values)
                        Log.d("DBHelper", "Alimento guardado: $nombre")
                    }
                }
            }
        }
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
    /**
     * Obtiene datos de un alimento base.
     */
    fun obtenerAlimentoPorNombre(nombre: String): AlimentoPor100g? {
        readableDatabase.use { db ->
            db.rawQuery(
                "SELECT proteinas, carbohidratos, grasas, vitamina_a, vitamina_c FROM alimentos WHERE nombre = ?",
                arrayOf(nombre)
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    return AlimentoPor100g(
                        proteinas     = cursor.getInt(cursor.getColumnIndexOrThrow("proteinas")),
                        carbohidratos = cursor.getInt(cursor.getColumnIndexOrThrow("carbohidratos")),
                        grasas        = cursor.getInt(cursor.getColumnIndexOrThrow("grasas")),
                        vitaminaA     = cursor.getInt(cursor.getColumnIndexOrThrow("vitamina_a")),
                        vitaminaC     = cursor.getInt(cursor.getColumnIndexOrThrow("vitamina_c"))
                    )
                }
            }
        }
        return null
    }
    fun getWritableDb(): SQLiteDatabase {
        return writableDatabase
    }
    /**
     * Obtiene el ID de un alimento base.
     */
    fun obtenerIdAlimentoPorNombre(nombre: String): Long {
        readableDatabase.use { db ->
            db.rawQuery("SELECT id FROM alimentos WHERE nombre = ?", arrayOf(nombre)).use { cursor ->
                if (cursor.moveToFirst()) {
                    return cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                }
            }
        }
        return -1L
    }

    /**
     * Inserta un registro de consumo.
     */
    fun insertarAlimentoConsumido(
        foodId: Long,
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
        writableDatabase.use { db ->
            ContentValues().apply {
                put("food_id", foodId)
                put("nombre", nombre)
                put("calorias", calorias)
                put("proteinas", proteinas)
                put("carbohidratos", carbohidratos)
                put("grasas", grasas)
                put("vitamina_a", vitaminaA)
                put("vitamina_c", vitaminaC)
                put("gramos", gramos)
                put("fecha", fecha)
            }.let { values ->
                db.insert("alimentos_consumidos", null, values)
            }
        }
    }
    /**
     * Elimina un consumo por ID y fecha (solo hoy).
     */

    fun eliminarAlimentoConsumidoPorId(id: Long) {
        writableDatabase.use { db ->
            db.delete(
                "alimentos_consumidos",
                "id = ?",
                arrayOf(id.toString())
            )
        }
    }

    /**
     * Elimina todos los consumos de una fecha dada.
     */
    fun eliminarAlimentosConsumidosPorFecha(fecha: String) {
        writableDatabase.use { db ->
            db.delete(
                "alimentos_consumidos",
                "fecha = ?",
                arrayOf(fecha)
            )
        }
    }

    /**
     * Obtiene los consumos de una fecha.
     */
    fun obtenerAlimentosConsumidosPorFecha(fecha: String): List<FoodItem> {
        val list = mutableListOf<FoodItem>()
        readableDatabase.use { db ->
            db.rawQuery(
                "SELECT id, nombre, calorias, proteinas, carbohidratos, grasas, vitamina_a, vitamina_c, gramos FROM alimentos_consumidos WHERE fecha = ?",
                arrayOf(fecha)
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    list += FoodItem(
                        id         = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        name       = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                        calories   = cursor.getInt(cursor.getColumnIndexOrThrow("calorias")),
                        protein    = cursor.getInt(cursor.getColumnIndexOrThrow("proteinas")),
                        carbs      = cursor.getInt(cursor.getColumnIndexOrThrow("carbohidratos")),
                        fats       = cursor.getInt(cursor.getColumnIndexOrThrow("grasas")),
                        vitaminA   = cursor.getInt(cursor.getColumnIndexOrThrow("vitamina_a")),
                        vitaminC   = cursor.getInt(cursor.getColumnIndexOrThrow("vitamina_c")),
                        totalGrams = cursor.getInt(cursor.getColumnIndexOrThrow("gramos"))
                    )
                }
            }
        }
        return list
    }

    /**
     * Obtiene totales diarios anteriores a una fecha.
     */
    fun obtenerTotalesDiariosAnteriores(fechaActual: String): List<DailyTotal> {
        val dailyTotals = mutableListOf<DailyTotal>()
        readableDatabase.use { db ->
            db.rawQuery(
                """
                SELECT fecha,
                       SUM(calorias)    AS total_calorias,
                       SUM(proteinas)   AS total_proteinas,
                       SUM(carbohidratos) AS total_carbohidratos,
                       SUM(grasas)      AS total_grasas
                FROM alimentos_consumidos
                WHERE fecha < ?
                GROUP BY fecha
                """.trimIndent(),
                arrayOf(fechaActual)
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    dailyTotals += DailyTotal(
                        fecha         = cursor.getString(cursor.getColumnIndexOrThrow("fecha")),
                        totalCalories = cursor.getInt(cursor.getColumnIndexOrThrow("total_calorias")),
                        totalProtein  = cursor.getInt(cursor.getColumnIndexOrThrow("total_proteinas")),
                        totalCarbs    = cursor.getInt(cursor.getColumnIndexOrThrow("total_carbohidratos")),
                        totalFats     = cursor.getInt(cursor.getColumnIndexOrThrow("total_grasas"))
                    )
                }
            }
        }
        return dailyTotals
    }
}