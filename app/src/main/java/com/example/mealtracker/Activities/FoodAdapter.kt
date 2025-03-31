package com.example.mealtracker.Activities

import FoodItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mealtracker.R

class FoodAdapter(private var foodList: List<FoodItem>) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {
    class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFoodName: TextView = view.findViewById(R.id.tv_food_name)
        val tvCalories: TextView = view.findViewById(R.id.tv_calories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.tvFoodName.text = food.name
        holder.tvCalories.text = "${food.calories} kcal"
    }

    override fun getItemCount(): Int = foodList.size

    fun updateData(newFoodList: List<FoodItem>) {
        foodList = newFoodList
        notifyDataSetChanged()
    }
}
