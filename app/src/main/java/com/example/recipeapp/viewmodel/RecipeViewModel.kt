package com.example.recipeapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.recipeapp.data.RecipeDatabase
import com.example.recipeapp.repository.RecipeRepository
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.model.SavedRecipe
import com.example.recipeapp.model.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class RecipeViewModel(application: Application, private var repository: RecipeRepository): AndroidViewModel(application) {

    val readAllRecipes: LiveData<List<Recipe>>
    val readAllSavedRecipes: LiveData<List<SavedRecipe>>
    val readAllTypes: LiveData<List<Type>>

    init {
        val recipeDao = RecipeDatabase.getDatabase(application).recipeDao()
        repository = RecipeRepository(recipeDao)
        readAllRecipes = repository.readAllRecipes
        readAllSavedRecipes = repository.readAllSavedRecipes
        readAllTypes = repository.readAllTypes
    }

    fun addRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addRecipe(recipe)
        }
    }

    fun updateRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRecipe(recipe)
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecipe(recipe)
        }
    }

    fun deleteAllRecipes() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllRecipes()
        }
    }

    //for saved recipes
    fun addSavedRecipe(savedRecipe: SavedRecipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSavedRecipe(savedRecipe)
        }
    }

    fun updateSavedRecipe(savedRecipe: SavedRecipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSavedRecipe(savedRecipe)
        }
    }

    fun deleteSavedRecipe(savedRecipe: SavedRecipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSavedRecipe(savedRecipe)
        }
    }

    fun deleteAllSavedRecipes(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllSavedRecipes(email)
        }
    }

    fun searchDatabase(searchQuery: String): LiveData<List<Recipe>> {
        return repository.searchDatabase(searchQuery).asLiveData()
    }

    fun searchSavedRecipes(searchQuery: String, email: String): LiveData<List<SavedRecipe>> {
        return repository.searchSavedRecipes(searchQuery, email).asLiveData()
    }

}