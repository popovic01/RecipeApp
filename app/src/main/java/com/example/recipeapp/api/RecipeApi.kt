package com.example.recipeapp.api

import com.example.recipeapp.model.FoodRecipe
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.utils.Constants.Companion.API_KEY
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipeApi {

    @GET("/recipes/{id}/information?includeNutrition=true&apiKey=$API_KEY")
    fun getRecipeById(
        @Path("id") id: Int
    ): Call<Recipe>

    @GET("/recipes/complexSearch")
    fun getRecipesByType(
        @Query("addRecipeInformation") addInformation: Boolean,
        @Query("addRecipeNutrition") addNutrition: Boolean,
        @Query("type") typeName: String,
        @Query("apiKey") apiKey: String,
        @Query("instructionsRequired") instructionsRequired: Boolean
    ): Call<FoodRecipe>

    //searching recipes by name from api
    @GET("/recipes/complexSearch")
    fun getRecipesByName(
        @Query("query") text: String,
        @Query("addRecipeInformation") information: Boolean,
        @Query("apiKey") apiKey: String
    ): Call<FoodRecipe>
}