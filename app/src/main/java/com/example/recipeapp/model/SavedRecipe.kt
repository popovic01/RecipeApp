package com.example.recipeapp.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.recipeapp.converter.AnalyzedInstructionConverter
import com.example.recipeapp.converter.IngredientConverter
import com.example.recipeapp.converter.NutrientConverter
import com.example.recipeapp.converter.NutritionConverter
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "saved_recipe_table")
data class SavedRecipe(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val type: String,
    val image: String,
    @SerializedName("readyInMinutes")
    val totalTime: Int,
    val servings: Int,
    @TypeConverters(AnalyzedInstructionConverter::class)
    val analyzedInstructions: List<AnalyzedInstruction?>?,
    @TypeConverters(NutritionConverter::class)
    val nutrition: Nutrition?,
    val userEmail: String,
    val sourceUrl: String?
): Parcelable