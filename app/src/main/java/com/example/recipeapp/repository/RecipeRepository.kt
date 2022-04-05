package com.example.recipeapp.repository

import androidx.lifecycle.LiveData
import com.example.recipeapp.api.RetrofitInstance
import com.example.recipeapp.data.RecipeDao
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.model.SavedRecipe
import com.example.recipeapp.model.Type
import kotlinx.coroutines.flow.Flow
import retrofit2.Call
import retrofit2.Response

class RecipeRepository(private val recipeDao: RecipeDao) {

    val readAllRecipes: LiveData<List<Recipe>> = recipeDao.readAllRecipes()
    val readAllSavedRecipes: LiveData<List<SavedRecipe>> = recipeDao.readSavedRecipes()
    val readAllTypes: LiveData<List<Type>> = recipeDao.readAllTypes()
    //var id: Int = 0
    //val recipeById: Recipe = recipeDao.getRecipeById(id)

    /*suspend fun recipeById(): Recipe {
        return recipeDao.getRecipeById(id)
    }*/

    suspend fun addRecipe(recipe: Recipe) {
        recipeDao.addRecipe(recipe)
    }

    suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.updateRecipe(recipe)
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.deleteRecipe(recipe)
    }

    suspend fun deleteAllRecipes() {
        recipeDao.deleteAllRecipes()
    }

    suspend fun addSavedRecipe(savedRecipe: SavedRecipe) {
        recipeDao.insertSavedRecipe(savedRecipe)
    }

    suspend fun updateSavedRecipe(savedRecipe: SavedRecipe) {
        recipeDao.updateSavedRecipe(savedRecipe)
    }

    suspend fun deleteSavedRecipe(savedRecipe: SavedRecipe) {
        recipeDao.deleteSavedRecipe(savedRecipe)
    }

    suspend fun deleteAllSavedRecipes(email: String) {
        recipeDao.deleteAllSavedRecipes(email)
    }

    fun searchDatabase(searchQuery: String): Flow<List<Recipe>> {
        return recipeDao.searchDatabase(searchQuery)
    }

    fun searchSavedRecipes(searchQuery: String, email: String): Flow<List<SavedRecipe>> {
        return recipeDao.searchSavedRecipes(searchQuery, email)
    }

    /*suspend fun getRecipeById(number: Int): Response<Recipe> {
        return RetrofitInstance.api.getRecipeById(number)
    }*/

    /*suspend fun getRecipes(title: String): Response<List<Recipe>> {
        return RetrofitInstance.api.getRecipes(title)
    }*/

    /*suspend fun getRecipes(type: String): Response<List<Recipe>> {
        return RetrofitInstance.api.getRecipes(type, "228aac27c0b5442195bf278ae4b6f749")
    }*/

    //searching recipes by name from api
    /*suspend fun getRecipesByName(text: String): Call<List<Recipe>> {
        return RetrofitInstance.api.getRecipesByName(text, true, "228aac27c0b5442195bf278ae4b6f749")
    }*/

   /* suspend fun getRandomRecipes(): Response<List<Recipe>> {
        return RetrofitInstance.api.getRandomRecipes()
    }*/
}