package com.example.mealtracker.Activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// Modelo de datos para una comida
class FoodViewModel : ViewModel() {
    private val _foodList = MutableLiveData<List<FoodItem>>(emptyList())
    val foodList: LiveData<List<FoodItem>> get() = _foodList

    private val _totalCalories = MutableLiveData(0)
    val totalCalories: LiveData<Int> get() = _totalCalories

    private val calorieGoal = 2000 // Meta de calorías

    fun addFood(food: FoodItem) {
        _foodList.value = _foodList.value!! + food
        _totalCalories.value = _foodList.value!!.sumOf { it.calories }
    }

    fun getCaloriesSummary(): String {
        val consumed = _totalCalories.value ?: 0
        val remaining = calorieGoal - consumed
        return "Calorías: $consumed / $calorieGoal (Restantes: $remaining)"
    }
}
