package com.example.mealtracker.Activities

import FoodItem
import android.os.Bundle
import com.example.mealtracker.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import android.graphics.Color
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.HorizontalBarChart

class NutrientesDiarios : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nutrientes_diarios)

        dbHelper = DBHelper(this)

        val foodList = intent.getParcelableArrayListExtra<FoodItem>("food_list") ?: arrayListOf()

        var totalCalories = 0
        var totalProtein = 0
        var totalCarbs = 0
        var totalFats = 0

        foodList.forEach {
            totalCalories += it.calories
            totalProtein += it.protein
            totalCarbs += it.carbs
            totalFats += it.fats
        }

        val goal = dbHelper.cargarObjetivosNutricionales()
        val proteinGoal = goal?.protein ?: 100
        val carbsGoal = goal?.carbs ?: 100
        val fatsGoal = goal?.fats ?: 100
        val calorieGoal = goal?.calories ?: 2000

        setupHorizontalBarChart(findViewById(R.id.barChartProtein), totalProtein.toFloat(), proteinGoal.toFloat(), "Proteínas", Color.BLUE)
        setupHorizontalBarChart(findViewById(R.id.barChartCarbs), totalCarbs.toFloat(), carbsGoal.toFloat(), "Carbohidratos", Color.GREEN)
        setupHorizontalBarChart(findViewById(R.id.barChartFats), totalFats.toFloat(), fatsGoal.toFloat(), "Grasas", Color.RED)
        setupHorizontalBarChart(findViewById(R.id.barChartCalories), totalCalories.toFloat(), calorieGoal.toFloat(), "Calorías", Color.MAGENTA)
        setupPieChart(findViewById(R.id.pieChartMacros), totalProtein.toFloat(), totalCarbs.toFloat(), totalFats.toFloat(), proteinGoal.toFloat(), carbsGoal.toFloat(), fatsGoal.toFloat())
    }

    private fun setupHorizontalBarChart(barChart: HorizontalBarChart, value: Float, max: Float, label: String, color: Int) {
        val entries = listOf(BarEntry(0f, value.coerceAtMost(max)))
        val dataSet = BarDataSet(entries, label).apply {
            this.color = color
            valueTextSize = 12f
        }

        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.setDrawValueAboveBar(true)
        barChart.axisLeft.axisMaximum = max
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.isEnabled = false
        barChart.xAxis.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.invalidate()
    }

    private fun setupPieChart(pieChart: PieChart, protein: Float, carbs: Float, fats: Float, proteinGoal: Float, carbsGoal: Float, fatsGoal: Float) {
        val totalGoal = proteinGoal + carbsGoal + fatsGoal
        if (totalGoal == 0f) return

        val entries = listOf(
            PieEntry((protein / totalGoal) * 100, "Proteínas"),
            PieEntry((carbs / totalGoal) * 100, "Carbohidratos"),
            PieEntry((fats / totalGoal) * 100, "Grasas")
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(Color.BLUE, Color.GREEN, Color.RED)
            valueTextSize = 12f
        }

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.isRotationEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.invalidate()
    }

}
