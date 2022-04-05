package com.example.recipeapp

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeapp.adapter.ListAdapter
import com.example.recipeapp.adapter.TypeAdapter
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.model.Type
import com.example.recipeapp.repository.RecipeRepository
import com.example.recipeapp.viewmodel.RecipeViewModel
import com.example.recipeapp.viewmodel.RecipeViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.launch

class HomeActivity : BaseActivity(), androidx.appcompat.widget.SearchView.OnQueryTextListener {

    var typeAdapter = TypeAdapter()
    private lateinit var auth: FirebaseAuth
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var adapter: ListAdapter

    //bottom navigation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val rvRecipe: RecyclerView = findViewById(R.id.rvSubCategory)
        adapter = ListAdapter()
        rvRecipe.adapter = adapter
        rvRecipe.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val recipeDao = com.example.recipeapp.data.RecipeDatabase.getDatabase(application).recipeDao()
        val repository = RecipeRepository(recipeDao)
        val viewModelFactory = RecipeViewModelFactory(repository)
        recipeViewModel = ViewModelProvider(this, viewModelFactory).get(RecipeViewModel::class.java)

        getTypes()
        typeAdapter.setClickListener(onClickedType)

        //Auth
        auth = FirebaseAuth.getInstance()

        //bottom navigation
        bottom_nav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.saved -> {
                    if (auth.currentUser != null) {
                        Intent(this, SavedActivity::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                        }
                    } else
                        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                }
                R.id.addNewRecipe -> {
                    if (auth.currentUser != null) {
                        startActivity(Intent(this@HomeActivity, AddRecipeActivity::class.java))
                    } else
                        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                }
                R.id.profile -> {
                    if (auth.currentUser != null) {
                        Intent(this, LogoutActivity::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                        }
                    } else
                        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                }
            }
            false
        }

        svRecipeHome.isSubmitButtonEnabled = true
        svRecipeHome.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchRecipes(query)
        }
        return true
    }

    private fun searchRecipes(query: String) {
        val searchQuery = "%$query%"

        recipeViewModel.searchDatabase(searchQuery).observe(this, Observer { recipes ->
            recipes.let {
                adapter.setData(it)
            }
        })
    }

    private val onClickedType = object: TypeAdapter.OnItemClickListener {
        override fun onClicked(categoryName: String) {
            getRecipesByType(categoryName)
        }
    }

    //setovanje naziva i slika za kategorije
    private fun getTypes() {
        recipeViewModel.readAllTypes.observe(this, Observer { types ->
            typeAdapter.setData(types)
            rvMainCategory.layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            rvMainCategory.adapter = typeAdapter
        })
        getRecipesByType("dessert")
    }

    private fun getRecipesByType(categoryName: String) {
        tvCategory.text = "Category: $categoryName"
        recipeViewModel.readAllRecipes.observe(this, Observer { recipes ->
            if (categoryName == "all") {
                adapter.setData(recipes)
            } else {
                var recipesByType: List<Recipe> = recipes.filter { it.type == categoryName }
                adapter.setData(recipesByType)
            }
            rvMainCategory.layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            rvMainCategory.adapter = typeAdapter
        })

    }
}