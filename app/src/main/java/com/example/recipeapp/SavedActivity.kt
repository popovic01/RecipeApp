package com.example.recipeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeapp.adapter.SavedRecipesAdapter
import com.example.recipeapp.model.SavedRecipe
import com.example.recipeapp.repository.RecipeRepository
import com.example.recipeapp.viewmodel.RecipeViewModel
import com.example.recipeapp.viewmodel.RecipeViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_add_recipe.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.bottom_nav
import kotlinx.android.synthetic.main.activity_saved.*
import kotlinx.android.synthetic.main.activity_saved.btnDelete
import kotlinx.android.synthetic.main.activity_update_recipe.*

class SavedActivity : BaseActivity(), androidx.appcompat.widget.SearchView.OnQueryTextListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var adapter: SavedRecipesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved)

        //recycler view
        val rvRecipe: RecyclerView = findViewById(R.id.rvRecipe)
        adapter = SavedRecipesAdapter()
        rvRecipe.adapter = adapter
        rvRecipe.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val recipeDao = com.example.recipeapp.data.RecipeDatabase.getDatabase(application).recipeDao()
        val repository = RecipeRepository(recipeDao)
        val viewModelFactory = RecipeViewModelFactory(repository)
        //recipeViewModel
        recipeViewModel = ViewModelProvider(this, viewModelFactory).get(RecipeViewModel::class.java)

        recipeViewModel.readAllSavedRecipes.observe(this, Observer { recipes ->
            //sacuvani recepti za ulogovanog korisnika
            var recipesForUser: List<SavedRecipe> = recipes.filter { it.userEmail == auth.currentUser!!.email }
            adapter.setData(recipesForUser)
        })

        //Auth
        auth = FirebaseAuth.getInstance()

        //bottom navigation
        bottom_nav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.profile -> {
                    if (auth.currentUser != null) {
                        Intent(this, LogoutActivity::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                        }
                    } else
                        startActivity(Intent(this@SavedActivity, LoginActivity::class.java))
                }
                R.id.addNewRecipe -> {
                    if (auth.currentUser != null) {
                        startActivity(Intent(this@SavedActivity, AddRecipeActivity::class.java))
                    } else
                        startActivity(Intent(this@SavedActivity, LoginActivity::class.java))
                }
                R.id.home -> {
                    startActivity(Intent(this@SavedActivity, HomeActivity::class.java))
                }
            }
            false
        }

        btnDelete.setOnClickListener {
            deleteAllRecipes()
        }

        val svRecipe = findViewById<androidx.appcompat.widget.SearchView>(R.id.svRecipe)
        svRecipe.isSubmitButtonEnabled = true
        svRecipe.setOnQueryTextListener(this)

    }

    private fun deleteAllRecipes() {
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton("Yes") {_, _ ->
            recipeViewModel.deleteAllSavedRecipes(auth.currentUser?.email!!)
            Toast.makeText(this, "Successfully deleted all saved recipes!", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No") {_, _ -> }
        builder.setTitle("Delete all saved recipes?")
        builder.setMessage("Are you sure you want to delete all your saved recipes?")
        builder.create().show()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchDatabase(query)
        }
        return true
    }

    private fun searchDatabase(query: String) {
        val searchQuery = "%$query%"
        recipeViewModel.searchSavedRecipes(searchQuery, auth.currentUser?.email!!).observe(this, { list ->
            list.let {
                adapter.setData(it)
            }
        })
    }

}