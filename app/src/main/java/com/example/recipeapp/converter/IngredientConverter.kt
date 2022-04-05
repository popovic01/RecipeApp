package com.example.recipeapp.converter

import androidx.room.TypeConverter
import com.example.recipeapp.model.Ingredient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class IngredientConverter {

    /*@TypeConverter
    fun fromStringToList(value: String?): List<Ingredient?>? {
        val listType = object : TypeToken<List<Ingredient?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromListToString(list: List<Ingredient?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }*/

    @TypeConverter
    fun fromCategoryList(category: List<Ingredient?>?): String? {
        if (category == null)
            return null
        else {
            val gson = Gson()
            val type = object: TypeToken<Ingredient>() {

            }.type
            return gson.toJson(category, type)
            //serijalizacija - iz objekta u json
        }
    }

    @TypeConverter
    fun toCategoryList(categoryString: String): List<Ingredient?>? {
        if (categoryString == null)
            return null
        else {
            val gson = Gson()
            val type = object: TypeToken<Ingredient>() {

            }.type
            return gson.fromJson(categoryString, type)
            //deserijalizacija - iz json-a u objekat
        }
    }

}