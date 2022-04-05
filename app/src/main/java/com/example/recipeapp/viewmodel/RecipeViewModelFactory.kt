package com.example.recipeapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.recipeapp.repository.RecipeRepository

class RecipeViewModelFactory(
    private  val repository: RecipeRepository
    ): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RecipeViewModel(application = Application(), repository) as T
    }

}