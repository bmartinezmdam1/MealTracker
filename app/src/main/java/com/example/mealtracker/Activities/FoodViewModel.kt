package com.example.mealtracker.Activities

import FoodItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FoodViewModel : ViewModel() {

    private val _foodList = MutableLiveData<MutableList<FoodItem>>(mutableListOf())
    val foodList: LiveData<MutableList<FoodItem>> get() = _foodList

    private val _totalCalories = MutableLiveData(0)
    val totalCalories: LiveData<Int> get() = _totalCalories

    private val _goals = MutableLiveData(NutritionGoals())
    val goals: LiveData<NutritionGoals> get() = _goals

    fun setFoodList(newList: List<FoodItem>) {
        _foodList.value = newList.toMutableList()
        recalculateCalories()
    }

    fun addFood(food: FoodItem) {
        val list = _foodList.value ?: mutableListOf()
        list.add(food)
        _foodList.value = list
        recalculateCalories()
    }
    fun getCaloriesSummary(): String {
        val consumed = _totalCalories.value ?: 0
        val goal = _goals.value?.calories ?: 2000
        val remaining = goal - consumed
        return "Calorías: $consumed / $goal (Restantes: $remaining)"
    }
    fun removeFood(food: FoodItem) {
        val list = _foodList.value ?: return
        list.removeAll { it.id == food.id }
        _foodList.value = list
        recalculateCalories()
    }

    private fun recalculateCalories() {
        _totalCalories.value = _foodList.value?.sumOf { it.calories } ?: 0
    }
    fun updateMacros(
        protein: Int, carbs: Int, fats: Int,
        vitaminA: Int, vitaminC: Int
    ) {
        val calories = protein*4 + carbs*4 + fats*9
        _goals.value = NutritionGoals(calories, protein, carbs, fats, vitaminA, vitaminC)
    }
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
