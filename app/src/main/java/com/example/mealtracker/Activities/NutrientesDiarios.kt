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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart

class NutrientesDiarios : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nutrientes_diarios)

        val foodList = intent.getParcelableArrayListExtra<FoodItem>("food_list") ?: arrayListOf()

        // Sumar los nutrientes consumidos
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

        setupHorizontalBarChart(findViewById(R.id.barChartProtein), totalProtein.toFloat(), 100f, "Proteínas", Color.BLUE)
        setupHorizontalBarChart(findViewById(R.id.barChartCarbs), totalCarbs.toFloat(), 100f, "Carbohidratos", Color.GREEN)
        setupHorizontalBarChart(findViewById(R.id.barChartFats), totalFats.toFloat(), 100f, "Grasas", Color.RED)
        setupPieChart(findViewById(R.id.pieChartMacros), totalProtein.toFloat(), totalCarbs.toFloat(), totalFats.toFloat())
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



    private fun setupPieChart(pieChart: PieChart, protein: Float, carbs: Float, fats: Float) {
        val total = protein + carbs + fats
        if (total == 0f) return

        val entries = listOf(
            PieEntry((protein / total) * 100, "Proteínas"),
            PieEntry((carbs / total) * 100, "Carbohidratos"),
            PieEntry((fats / total) * 100, "Grasas")
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
