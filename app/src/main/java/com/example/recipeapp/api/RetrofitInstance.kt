package com.example.recipeapp.api

import com.example.recipeapp.utils.Constants.Companion.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//we want this instance to be singleton
object RetrofitInstance {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
             //we need to add GsonConverterFactory to convert JSON object to Java object
    }

    val api: RecipeApi by lazy {
        retrofit.create(RecipeApi::class.java)
    }

}