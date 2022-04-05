package com.example.recipeapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.recipeapp.converter.AnalyzedInstructionConverter
import com.example.recipeapp.converter.IngredientConverter
import com.example.recipeapp.converter.NutrientConverter
import com.example.recipeapp.converter.NutritionConverter
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.model.SavedRecipe
import com.example.recipeapp.model.Type

@Database(entities = [Recipe::class, SavedRecipe::class, Type::class], version = 1, exportSchema = false)
@TypeConverters(NutrientConverter::class, IngredientConverter::class, NutritionConverter::class, AnalyzedInstructionConverter::class)
abstract class RecipeDatabase: RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null

        fun getDatabase(context: Context): RecipeDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null)
                return tempInstance
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "recipe_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }

}