package com.example.recipeapp.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.model.SavedRecipe
import com.example.recipeapp.model.Type
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addRecipe(recipe: Recipe)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("DELETE FROM recipe_table")
    suspend fun deleteAllRecipes()

    @Query("SELECT * FROM recipe_table")
    fun readAllRecipes(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipe_table WHERE id LIKE :id")
    suspend fun getRecipeById(id: Int): Recipe

    @Query("SELECT * FROM recipe_table WHERE title LIKE :searchQuery")
    fun searchDatabase(searchQuery: String): Flow<List<Recipe>>

    @Query("SELECT * FROM saved_recipe_table WHERE title LIKE :searchQuery AND userEmail like :email")
    fun searchSavedRecipes(searchQuery: String, email: String): Flow<List<SavedRecipe>>

    //insert saved recipe for one user
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedRecipe(savedRecipe: SavedRecipe)

    @Update
    suspend fun updateSavedRecipe(savedRecipe: SavedRecipe)

    //read saved recipes of one user
    @Query("SELECT * FROM saved_recipe_table")
    fun readSavedRecipes(): LiveData<List<SavedRecipe>>

    //delete the recipe of one user
    @Delete
    suspend fun deleteSavedRecipe(savedRecipe: SavedRecipe)

    //delete all recipes of one user
    @Query("DELETE FROM saved_recipe_table WHERE userEmail LIKE :email")
    suspend fun deleteAllSavedRecipes(email: String)

    //dodavanje kategorije
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addType(type: Type)

    @Query("SELECT * FROM type_table")
    fun readAllTypes(): LiveData<List<Type>>

    //delete query to clear types on app startup
    @Query("DELETE FROM type_table")
    suspend fun clearDb()

    //delete query to clear recipes on app startup
    @Query("DELETE FROM recipe_table")
    suspend fun clearMeal()
}