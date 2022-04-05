package com.example.recipeapp.converter

import androidx.room.TypeConverter
import com.example.recipeapp.model.AnalyzedInstruction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AnalyzedInstructionConverter {

    @TypeConverter
    fun fromCategoryList(category: List<AnalyzedInstruction?>?): String? {
        if (category == null)
            return null
        else {
            val gson = Gson()
            val type = object: TypeToken<List<AnalyzedInstruction?>?>() {

            }.type
            return gson.toJson(category, type)
            //serijalizacija - iz objekta u json
        }
    }

    @TypeConverter
    fun toCategoryList(categoryString: String): List<AnalyzedInstruction?>? {
        if (categoryString == null)
            return null
        else {
            val gson = Gson()
            val type = object: TypeToken<List<AnalyzedInstruction?>?>() {

            }.type
            return gson.fromJson(categoryString, type)
            //deserijalizacija - iz json-a u objekat
        }
    }

}