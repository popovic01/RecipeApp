package com.example.recipeapp.converter

import androidx.room.TypeConverter
import com.example.recipeapp.model.Nutrition
import com.google.gson.Gson

class NutritionConverter {

    @TypeConverter
    fun nutritionToString(nutrition: Nutrition): String = Gson().toJson(nutrition)

    @TypeConverter
    fun stringToNutrition(string: String): Nutrition = Gson().fromJson(string, Nutrition::class.java)
}