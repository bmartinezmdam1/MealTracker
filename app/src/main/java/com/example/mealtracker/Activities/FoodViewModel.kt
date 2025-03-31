package com.example.mealtracker.Activities

import FoodItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FoodViewModel : ViewModel() {

    // Lista de comidas
    private val _foodList = MutableLiveData<List<FoodItem>>(emptyList())
    val foodList: LiveData<List<FoodItem>> get() = _foodList

    // Calorías totales
    private val _totalCalories = MutableLiveData(0)
    val totalCalories: LiveData<Int> get() = _totalCalories

    // Objetivos nutricionales
    private val _goals = MutableLiveData(
        NutritionGoals(
            calories = 2000,
            protein = 100,
            carbs = 250,
            fats = 55,
            vitaminA = 900,
            vitaminC = 60
        )
    )
    val goals: LiveData<NutritionGoals> get() = _goals

    // Agregar comida y actualizar calorías
    fun addFood(food: FoodItem) {
        val updatedList = _foodList.value?.toMutableList() ?: mutableListOf()
        updatedList.add(food)
        _foodList.value = updatedList
        _totalCalories.value = updatedList.sumOf { it.calories }
    }

    // Resumen de calorías
    fun getCaloriesSummary(): String {
        val consumed = _totalCalories.value ?: 0
        val goal = _goals.value?.calories ?: 2000
        val remaining = goal - consumed
        return "Calorías: $consumed / $goal (Restantes: $remaining)"
    }

    // Actualizar macros → recalcular calorías
    fun updateMacros(protein: Int, carbs: Int, fats: Int, vitaminA: Int, vitaminC: Int) {
        val calories = protein * 4 + carbs * 4 + fats * 9
        _goals.value = NutritionGoals(
            calories = calories,
            protein = protein,
            carbs = carbs,
            fats = fats,
            vitaminA = vitaminA,
            vitaminC = vitaminC
        )
    }

    // Actualizar calorías → recalcular macros en proporción
    fun updateCalories(newCalories: Int) {
        val current = _goals.value ?: return

        val currentCal = current.protein * 4 + current.carbs * 4 + current.fats * 9
        if (currentCal == 0) return

        val proteinRatio = (current.protein * 4f) / currentCal
        val carbsRatio = (current.carbs * 4f) / currentCal
        val fatsRatio = (current.fats * 9f) / currentCal

        val newProtein = ((newCalories * proteinRatio) / 4).toInt()
        val newCarbs = ((newCalories * carbsRatio) / 4).toInt()
        val newFats = ((newCalories * fatsRatio) / 9).toInt()

        _goals.value = NutritionGoals(
            calories = newCalories,
            protein = newProtein,
            carbs = newCarbs,
            fats = newFats,
            vitaminA = current.vitaminA,
            vitaminC = current.vitaminC
        )
    }
}
