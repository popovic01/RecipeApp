package com.example.recipeapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.recipeapp.model.*
import com.example.recipeapp.repository.RecipeRepository
import com.example.recipeapp.viewmodel.RecipeViewModel
import com.example.recipeapp.viewmodel.RecipeViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_add_recipe.*
import kotlinx.android.synthetic.main.activity_add_recipe.btnImage
import kotlinx.android.synthetic.main.activity_add_recipe.etCalories
import kotlinx.android.synthetic.main.activity_add_recipe.etIngredients
import kotlinx.android.synthetic.main.activity_add_recipe.etInstructions
import kotlinx.android.synthetic.main.activity_add_recipe.etServings
import kotlinx.android.synthetic.main.activity_add_recipe.etTitle
import kotlinx.android.synthetic.main.activity_add_recipe.etTotalTime
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_update_recipe.*

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var auth: FirebaseAuth
    private val pickImage = 100 //constant to compare the activity result code
    private var imageUri: Uri? = null //uri of the selected image

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        val recipeDao = com.example.recipeapp.data.RecipeDatabase.getDatabase(application).recipeDao()
        val repository = RecipeRepository(recipeDao)
        val viewModelFactory = RecipeViewModelFactory(repository)
        recipeViewModel = ViewModelProvider(this, viewModelFactory).get(RecipeViewModel::class.java)

        //spinner
        var spinner = spTypeAdd
        val adapter = ArrayAdapter.createFromResource(this, R.array.types_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        spinner.setAdapter(adapter)

        btnAdd.setOnClickListener {
            insertDataToDatabase()
        }

        btnImage.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        //Auth
        auth = FirebaseAuth.getInstance()

        val bottom_nav: BottomNavigationView = findViewById(R.id.bottom_nav)

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
                        startActivity(Intent(this@AddRecipeActivity, LoginActivity::class.java))
                }
                R.id.saved -> {
                    if (auth.currentUser != null) {
                        Intent(this, SavedActivity::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                        }
                    } else
                        startActivity(Intent(this@AddRecipeActivity, LoginActivity::class.java))
                }
                R.id.home -> {
                    startActivity(Intent(this@AddRecipeActivity, HomeActivity::class.java))
                }
            }
            false
        }
    }

    //this function is triggered when user selects the image from the imageChooser
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            ivRecipeAdd.setImageURI(imageUri)
        }
    }

    private fun insertDataToDatabase() {
        val title = etTitle.text.toString()
        val type = spTypeAdd.selectedItem.toString()
        val image = imageUri!!
        val totalTime = etTotalTime.text.toString()
        val servings = etServings.text.toString()
        val calories = etCalories.text.toString()
        val instructions = etInstructions.text.toString()
        val ingredients = etIngredients.text.toString()

        if (inputCheck(title, type, image.toString(), totalTime, servings, calories, instructions)) {
            val listOfInstructions = instructions.split(". ")
            var steps: MutableList<Step> = ArrayList()
            var i = 1
            for (instruction in listOfInstructions) {
                steps.add(Step(i, instruction))
                i++
            }

            val listOfIngredients = ingredients.split("\n")
            var listOfNames: MutableList<String> = ArrayList()
            var listOfAmounts: MutableList<Double> = ArrayList()
            var listOfUnits: MutableList<String> = ArrayList()
            var ingr: MutableList<Ingredient> = ArrayList()
            for (i in listOfIngredients) {
                listOfNames.add(i.split(": ")[0])
                listOfAmounts.add(i.split(": ")[1].split(" ")[0].toDouble())
                listOfUnits.add(i.split(": ")[1].split(" ")[1])
            }

            var ind = 0
            for (i in listOfIngredients) {
                ingr.add(Ingredient(listOfAmounts[ind], listOfNames[ind], listOfUnits[ind]))
                ind++
            }

            //create recipe object
            val recipe = Recipe(0, title, type, image.toString(), totalTime.toInt(), servings.toInt(),
                listOf(AnalyzedInstruction("", steps)), Nutrition(
                listOf(Nutrient(calories.toDouble(), "Calories","kcal")),
                ingr), "")
            Log.d("reecc", recipe.toString())
            Log.d("reecc1", recipe.sourceUrl.toString())
            //add data to database
            recipeViewModel.addRecipe(recipe)
            Toast.makeText(this, "Successfully added!", Toast.LENGTH_LONG).show()
            startActivity(Intent(this@AddRecipeActivity, HomeActivity::class.java))
        } else {
            Toast.makeText(this, "Please fill out all fields!", Toast.LENGTH_LONG).show()
        }
    }

    //checks if fields are empty
    private fun inputCheck(title: String, type: String, image: String, totalTime: String,
                           servings: String, calories: String, instructions: String): Boolean {
        return !(TextUtils.isEmpty(title) ||
                TextUtils.isEmpty(type) ||
                TextUtils.isEmpty(image) ||
                TextUtils.isEmpty(totalTime) ||
                TextUtils.isEmpty(servings) ||
                TextUtils.isEmpty(calories)) ||
                TextUtils.isEmpty(instructions)
    }

}