package com.example.recipeapp.model

import android.os.Parcelable
import androidx.room.TypeConverters
import com.example.recipeapp.converter.IngredientConverter
import com.example.recipeapp.converter.NutrientConverter
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Nutrition(
    @TypeConverters(NutrientConverter::class)
    val nutrients: List<Nutrient?>?,
    @TypeConverters(IngredientConverter::class)
    val ingredients: List<Ingredient?>?
) : Parcelable