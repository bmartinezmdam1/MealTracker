package com.example.mealtracker.Activities

import DBHelper
import FoodItem
import android.os.Bundle
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import com.example.mealtracker.R
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.*

class NutrientesDiarios : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nutrientes_diarios)

        dbHelper = DBHelper(this)
        val foodList = intent.getParcelableArrayListExtra<FoodItem>("food_list") ?: arrayListOf()

        // 1. Calculate current intake totals
        val (calories, protein, carbs, fats) = calculateTotals(foodList)

        // 2. Load nutrition goals
        val goals = loadNutritionGoals()

        // 3. Setup current intake charts
        setupCurrentCharts(protein, carbs, fats, calories, goals)

        // 4. Setup historical estimates
        setupHistoricalCharts(goals)
    }

    private fun calculateTotals(foodList: ArrayList<FoodItem>): NutrientTotals {
        var calories = 0
        var protein = 0
        var carbs = 0
        var fats = 0

        foodList.forEach {
            calories += it.calories
            protein += it.protein
            carbs += it.carbs
            fats += it.fats
        }
        return NutrientTotals(calories, protein, carbs, fats)
    }

    private fun loadNutritionGoals(): NutritionGoals {
        val goals = dbHelper.cargarObjetivosNutricionales()
        return NutritionGoals(
            protein = goals?.protein ?: 100,
            carbs = goals?.carbs ?: 100,
            fats = goals?.fats ?: 100,
            calories = goals?.calories ?: 2000
        )
    }

    private fun setupCurrentCharts(
        protein: Int, carbs: Int, fats: Int, calories: Int,
        goals: NutritionGoals
    ) {
        // Protein chart
        setupHorizontalBarChart(
            findViewById(R.id.barChartProtein),
            protein.toFloat(),
            goals.protein.toFloat(),
            "Proteínas",
            Color.BLUE
        )

        // Carbs chart
        setupHorizontalBarChart(
            findViewById(R.id.barChartCarbs),
            carbs.toFloat(),
            goals.carbs.toFloat(),
            "Carbohidratos",
            Color.GREEN
        )

        // Fats chart
        setupHorizontalBarChart(
            findViewById(R.id.barChartFats),
            fats.toFloat(),
            goals.fats.toFloat(),
            "Grasas",
            Color.RED
        )

        // Calories chart
        setupHorizontalBarChart(
            findViewById(R.id.barChartCalories),
            calories.toFloat(),
            goals.calories.toFloat(),
            "Calorías",
            Color.MAGENTA
        )

        // Macronutrient pie chart (FIXED)
        setupMacroPieChart(
            findViewById(R.id.pieChartMacros),
            protein.toFloat(),
            carbs.toFloat(),
            fats.toFloat()
        )
    }

    private fun setupHistoricalCharts(goals: NutritionGoals) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = sdf.format(Date())
        val historicalData = dbHelper.obtenerTotalesDiariosAnteriores(currentDate)

        if (historicalData.isNotEmpty()) {
            val averages = calculateAverages(historicalData)

            // Protein estimate
            setupHorizontalBarChart(
                findViewById(R.id.barChartProteinEstimate),
                averages.protein,
                goals.protein.toFloat(),
                "Proteínas Estimadas",
                Color.BLUE
            )

            // Carbs estimate
            setupHorizontalBarChart(
                findViewById(R.id.barChartCarbsEstimate),
                averages.carbs,
                goals.carbs.toFloat(),
                "Carbohidratos Estimados",
                Color.GREEN
            )

            // Fats estimate
            setupHorizontalBarChart(
                findViewById(R.id.barChartFatsEstimate),
                averages.fats,
                goals.fats.toFloat(),
                "Grasas Estimadas",
                Color.RED
            )

            // Calories estimate
            setupHorizontalBarChart(
                findViewById(R.id.barChartCaloriesEstimate),
                averages.calories,
                goals.calories.toFloat(),
                "Calorías Estimadas",
                Color.MAGENTA
            )

            // Estimate pie chart
            setupMacroPieChart(
                findViewById(R.id.pieChartMacrosEstimate),
                averages.protein,
                averages.carbs,
                averages.fats
            )
        }
    }

    private fun calculateAverages(data: List<DailyTotal>): NutrientAverages {
        var totalCalories = 0
        var totalProtein = 0
        var totalCarbs = 0
        var totalFats = 0

        data.forEach {
            totalCalories += it.totalCalories
            totalProtein += it.totalProtein
            totalCarbs += it.totalCarbs
            totalFats += it.totalFats
        }

        return NutrientAverages(
            calories = totalCalories.toFloat() / data.size,
            protein = totalProtein.toFloat() / data.size,
            carbs = totalCarbs.toFloat() / data.size,
            fats = totalFats.toFloat() / data.size
        )
    }

    // Fixed pie chart implementation
    private fun setupMacroPieChart(pieChart: PieChart, protein: Float, carbs: Float, fats: Float) {
        val total = protein + carbs + fats
        if (total == 0f) return

        val entries = listOf(
            PieEntry(protein, "Proteínas"),
            PieEntry(carbs, "Carbohidratos"),
            PieEntry(fats, "Grasas")
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(Color.BLUE, Color.GREEN, Color.RED)
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            isRotationEnabled = false
            setUsePercentValues(false) // Show actual values
            setEntryLabelColor(Color.BLACK)
            animateY(1000)
            invalidate()
        }
    }

    private fun setupHorizontalBarChart(
        chart: HorizontalBarChart,
        value: Float,
        max: Float,
        label: String,
        color: Int
    ) {
        val entry = BarEntry(0f, value)
        val dataSet = BarDataSet(listOf(entry), label).apply {
            this.color = color
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        chart.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            setFitBars(true)
            axisLeft.apply {
                axisMaximum = max
                axisMinimum = 0f
                setDrawGridLines(false)
            }
            xAxis.isEnabled = false
            axisRight.isEnabled = false
            legend.isEnabled = false
            animateY(1000)
            invalidate()
        }
    }

    // Data classes
    private data class NutrientTotals(
        val calories: Int,
        val protein: Int,
        val carbs: Int,
        val fats: Int
    )

    private data class NutritionGoals(
        val protein: Int,
        val carbs: Int,
        val fats: Int,
        val calories: Int
    )

    private data class NutrientAverages(
        val calories: Float,
        val protein: Float,
        val carbs: Float,
        val fats: Float
    )
}