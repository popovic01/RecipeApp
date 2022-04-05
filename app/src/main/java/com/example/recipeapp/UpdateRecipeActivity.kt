package com.example.recipeapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.recipeapp.viewmodel.RecipeViewModel
import kotlinx.android.synthetic.main.activity_update_recipe.*
import kotlinx.android.synthetic.main.activity_update_recipe.btnImage
import kotlinx.android.synthetic.main.activity_update_recipe.etCalories
import kotlinx.android.synthetic.main.activity_update_recipe.etServings
import kotlinx.android.synthetic.main.activity_update_recipe.etTitle
import kotlinx.android.synthetic.main.activity_update_recipe.etTotalTime
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.recipeapp.model.*
import com.example.recipeapp.repository.RecipeRepository
import com.example.recipeapp.viewmodel.RecipeViewModelFactory
import kotlinx.android.synthetic.main.activity_add_recipe.*
import kotlinx.android.synthetic.main.activity_detail.*

class UpdateRecipeActivity : AppCompatActivity() {

    private val pickImage = 100 //constant to compare the activity result code
    private var imageUri: Uri? = null //uri of the selected image
    private lateinit var recipeViewModel: RecipeViewModel
    var recipe: SavedRecipe? = null
    lateinit var sourceUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_recipe)

        val recipeDao = com.example.recipeapp.data.RecipeDatabase.getDatabase(application).recipeDao()
        val repository = RecipeRepository(recipeDao)
        val viewModelFactory = RecipeViewModelFactory(repository)
        recipeViewModel = ViewModelProvider(this, viewModelFactory).get(RecipeViewModel::class.java)

        recipe = intent.getParcelableExtra("savedRecipe")

        //spinner
        var spinner = spType
        val adapter = ArrayAdapter.createFromResource(this, R.array.types_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        spinner.setAdapter(adapter)
        setType(recipe?.type)

        etTitle.setText(recipe?.title)
        //ivRecipe.setImageURI(Uri.parse(recipe?.image))
        Glide.with(this).load(recipe?.image).centerCrop().into(ivRecipe)
        etTotalTime.setText(recipe?.totalTime.toString())
        etServings.setText(recipe?.servings.toString())
        etCalories.setText(recipe?.nutrition?.nutrients?.get(0)?.amount.toString())
        val etInstructions: EditText = findViewById(R.id.etInstructions)
        val etIngredients: EditText = findViewById(R.id.etIngredients)

        if (recipe?.sourceUrl == "")
            sourceUrl = ""
        else
            sourceUrl = recipe?.sourceUrl!!

        Log.d("imag", recipe?.toString()!!)
        Log.d("sizee", recipe?.analyzedInstructions?.get(0)?.steps?.size.toString())

        for (step in recipe?.analyzedInstructions?.get(0)?.steps!!) {
            if (step != null) {
                etInstructions.append((step.step).plus(". "))
            }
        }

        for (ingr in recipe?.nutrition?.ingredients!!) {
            if (ingr != null) {
                etIngredients.append((ingr.name).plus(": ").plus(ingr.amount.toString()).plus(" ").plus(ingr.unit).plus("\n"))
            }
        }

        btnImage.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        btnUpdate.setOnClickListener {
            updateItem()
        }

    }

    private fun setType(type: String?) {
            when (type) {
                "dessert" -> spType.setSelection(0)
                "main course" -> spType.setSelection(1)
                "side dish" -> spType.setSelection(2)
                "breakfast" -> spType.setSelection(3)
                "appetizer" -> spType.setSelection(4)
                "salad" -> spType.setSelection(5)
                "bread" -> spType.setSelection(6)
                "soup" -> spType.setSelection(7)
                "sauce" -> spType.setSelection(8)
                "drink" -> spType.setSelection(9)
                else -> spType.setSelection(10)
            }
    }

    //this function is triggered when user selects the image from the imageChooser
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            Glide.with(this).load(imageUri).centerCrop().into(ivRecipe)
            //ivRecipe.setImageURI(imageUri)
        }
    }

    private fun updateItem() {
        val title = etTitle.text.toString()
        val type = spType.selectedItem.toString()
        val image: String?
        val totalTime = etTotalTime.text.toString()
        val servings = etServings.text.toString()
        val calories = etCalories.text.toString()
        val etInstructions = findViewById<EditText>(R.id.etInstructions)
        val instructions = etInstructions.text.toString()
        val etIngredients = findViewById<EditText>(R.id.etIngredients)
        val ingredients = etIngredients.text.toString()

        if (imageUri == null)
            image = recipe!!.image
        else
            image = imageUri!!.toString()

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

        //Toast.makeText(this, recipe!!.id.toString(), Toast.LENGTH_SHORT).show()
        //Toast.makeText(this, recipe!!.sourceUrl.toString(), Toast.LENGTH_SHORT).show()

        if (inputCheck(title, type, image, totalTime, servings, calories, instructions)) {
            //create recipe object
            val updatedRecipe = SavedRecipe(recipe!!.id, title, type, image, totalTime.toInt(), servings.toInt(),
                listOf(AnalyzedInstruction("", steps)), Nutrition(
                listOf(Nutrient(calories.toDouble(), "Calories", "kcal")),
                ingr), recipe!!.userEmail, sourceUrl)
            //update current recipe
            recipeViewModel.updateSavedRecipe(updatedRecipe)
            Toast.makeText(this, "Successfully updated!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SavedActivity::class.java))
        } else {
            Toast.makeText(this, "Please fill out all fields!", Toast.LENGTH_SHORT).show()
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