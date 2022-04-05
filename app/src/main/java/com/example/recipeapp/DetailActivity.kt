package com.example.recipeapp

import android.content.ContentValues
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.recipeapp.model.AnalyzedInstruction
import com.example.recipeapp.model.Nutrition
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.model.SavedRecipe
import com.example.recipeapp.repository.RecipeRepository
import com.example.recipeapp.viewmodel.RecipeViewModel
import com.example.recipeapp.viewmodel.RecipeViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.android.synthetic.main.activity_add_recipe.*
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.rv_sub_category.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext

class DetailActivity : BaseActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var auth: FirebaseAuth
    var sourceLink = ""
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var fromActivity: String
    private lateinit var recipe: Recipe
    private lateinit var savedRecipe: SavedRecipe
    private val STORAGE_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        var id = intent.getIntExtra("id", 0)
        fromActivity = intent.getStringExtra("from")!! //da znam odakle je pozvana ova aktivnost

        //Auth
        auth = FirebaseAuth.getInstance()

        val recipeDao = com.example.recipeapp.data.RecipeDatabase.getDatabase(application).recipeDao()
        val repository = RecipeRepository(recipeDao)
        val viewModelFactory = RecipeViewModelFactory(repository)
        recipeViewModel = ViewModelProvider(this, viewModelFactory).get(RecipeViewModel::class.java)

        //spinner
        var spinner = spServings
        val adapter = ArrayAdapter.createFromResource(this, R.array.servings_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        spinner.setAdapter(adapter)
        spinner.onItemSelectedListener = this

        imgToolbarBtnBack.setOnClickListener {
            finish()
        }

        btnSourceLink.setOnClickListener {
            val uri: Uri = Uri.parse(sourceLink)

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        btnPdf.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                    val permission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permission, STORAGE_CODE)
                } else
                    savePdf()
            } else
                savePdf()
        }

        getSpecificItem(id)

        if (fromActivity == "saved")
            imgToolbarBtnFav.visibility = View.GONE
        else if (fromActivity == "home")
            imgToolbarBtnMenu.visibility = View.GONE

        //dugme za cuvanje recepta
        imgToolbarBtnFav.setOnClickListener {
            if (fromActivity == "home")
                saveRecipe(id)
        }

        imgToolbarBtnMenu.setOnClickListener {
            performOptionsMenuClick(imgToolbarBtnMenu, id)
        }

        imgBtnSocialMedia.setOnClickListener {
            shareRecipe()
        }
    }

    private fun shareRecipe() {
        var intent = Intent()
        intent.setAction(Intent.ACTION_SEND)

        Log.d("linkk", sourceLink)
        if (sourceLink != "") {
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_TEXT, sourceLink)
            intent.putExtra(Intent.EXTRA_TITLE, "Share your recipe")
            startActivity(Intent.createChooser(intent, "Share recipe"))
        } else {
            Toast.makeText(this, "There is no source link for this recipe", Toast.LENGTH_SHORT).show()
        }
    }

    fun savePdf() {
        val mDoc = Document()

        try {
            PdfWriter.getInstance(mDoc, FileOutputStream(getFilePath()))
            mDoc.open()

            val data = "Recipe"
            mDoc.addAuthor("Milica")
            mDoc.add(Paragraph(data))
            mDoc.add(Paragraph(data))
            mDoc.close()
            Toast.makeText(this, "File is saved to \n${getFilePath()}", Toast.LENGTH_SHORT).show()
        } catch(e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }

    }

    fun getFilePath(): String {
        var contextWrapper = ContextWrapper(this)
        var directory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        var file = File(directory, "Recipe" + ".pdf")

        return file.path
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            STORAGE_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    savePdf()
                } else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveRecipe(id: Int) {
        if (auth.currentUser != null) {
            val builder = AlertDialog.Builder(this)
            builder.setPositiveButton("Yes") {_, _ ->
                recipeViewModel.readAllRecipes.observe(this, Observer { recipes ->
                    var recipe = recipes.find { it.id == id }
                    if (recipe != null) {
                        var savedRecipe = SavedRecipe(0, recipe.title, recipe.type, recipe.image, recipe.totalTime,
                            recipe.servings, recipe.analyzedInstructions, recipe.nutrition, auth.currentUser?.email!!, recipe.sourceUrl)
                        recipeViewModel.addSavedRecipe(savedRecipe)
                    }
                })
                Toast.makeText(this, "Recipe successfully saved!", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("No") {_, _ -> }
            builder.setTitle("Save recipe?")
            builder.setMessage("Do you want do add this recipe to saved recipes?")
            builder.create().show()
        } else
            startActivity(Intent(this@DetailActivity, LoginActivity::class.java))
    }

    fun getSpecificItem(id: Int) {
        if (fromActivity == "home") {
            //Toast.makeText(this, recipe.toString(), Toast.LENGTH_SHORT).show()
            recipeViewModel.readAllRecipes.observe(this, Observer { recipes ->
                recipe = recipes.find { it.id == id }!!
                if (recipe != null) {
                    Glide.with(this).load(recipe.image).centerCrop().into(imgItem)
                    tvTitle.text = recipe.title
                    tvTime.text = recipe.totalTime.toString().plus("'")
                    tvServings.text = recipe.servings.toString()
                    //Toast.makeText(this, recipe.toString(), Toast.LENGTH_SHORT).show()
                    tvCalories.text = recipe.nutrition!!.nutrients!!.get(0)!!.amount.toString().plus(" kcal") //kolicina kalorija
                    if (recipe.sourceUrl!! == "")
                        btnSourceLink.visibility = View.GONE
                    else
                        sourceLink = recipe.sourceUrl!!
                    for (ingr in recipe.nutrition!!.ingredients!!) {
                        if (ingr != null) {
                            tvIngredients.append(ingr.name.plus(": ").plus(ingr.amount).plus(" ").plus(ingr.unit).plus("\n"))
                        }
                    }

                    for (step in recipe.analyzedInstructions?.get(0)?.steps!!) {
                        if (step != null) {
                            tvInstructions.append(step.number.toString().plus(": ").plus(step.step).plus("\n"))
                        }
                    }

                    recipeViewModel.readAllSavedRecipes.observe(this, Observer { recipes ->
                        //ako je recept vec sacuvan za nalog ulogovanog korisnika
                        var recipe1 = recipes.find { it.title == recipe.title
                                && it.image == recipe.image
                                && it.userEmail == auth.currentUser!!.email }
                        //Log.d("recc", recipe.toString())
                        if (recipe1 != null) {
                            imgToolbarBtnFav.visibility = View.INVISIBLE
                        }
                    })

                }
            })
        } else if (fromActivity == "saved") {
            recipeViewModel.readAllSavedRecipes.observe(this, Observer { recipes ->
                savedRecipe = recipes.find { it.id == id }!!
                if (savedRecipe != null) {
                    Glide.with(this).load(savedRecipe.image).centerCrop().into(imgItem)
                    tvTitle.text = savedRecipe.title
                    tvTime.text = savedRecipe.totalTime.toString().plus("'")
                    tvServings.text = savedRecipe.servings.toString()
                    //Toast.makeText(this, recipe.sourceUrl.toString(), Toast.LENGTH_SHORT).show()
                    tvCalories.text = savedRecipe.nutrition!!.nutrients!!.get(0)!!.amount.toString().plus(" kcal") //kolicina kalorija
                    if (savedRecipe.sourceUrl!! == "")
                        btnSourceLink.visibility = View.GONE
                    else
                        sourceLink = savedRecipe.sourceUrl!!
                    for (ingr in savedRecipe.nutrition!!.ingredients!!) {
                        if (ingr != null) {
                            tvIngredients.append(ingr.name.plus(": ").plus(ingr.amount).plus(" ").plus(ingr.unit).plus("\n"))
                        }
                    }

                    for (step in savedRecipe.analyzedInstructions?.get(0)?.steps!!) {
                        if (step != null) {
                            tvInstructions.append(step.number.toString().plus(": ").plus(step.step).plus("\n"))
                        }
                    }
                }
            })
        }
    }

    //this method will handle the onclick options click
    private fun performOptionsMenuClick(view: View, id: Int) {
        //create object of PopupMenu and pass context and view where we want
        //to show the popup menu
        val popupMenu = PopupMenu(this, view)
        //add the menu
        popupMenu.inflate(R.menu.popup_menu)
        //implement on menu item click Listener
        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when(item?.itemId){
                    R.id.menuDelete -> {
                        //here are the logic to delete an item from the list
                        val builder = AlertDialog.Builder(this@DetailActivity)
                        builder.setPositiveButton("Yes") {_, _ ->
                            recipeViewModel.readAllSavedRecipes.observe(this@DetailActivity, Observer { recipes ->
                                val recipe = recipes.find { it.id == id }
                                if (recipe != null) {
                                    /*val savedRecipe = SavedRecipe(0, recipe.title, recipe.image, recipe.type, recipe.totalTime,
                                        recipe.servings, recipe.analyzedInstructions, recipe.nutrition, auth.currentUser?.email!!, recipe.sourceUrl)*/
                                    recipeViewModel.deleteSavedRecipe(recipe)
                                    startActivity(Intent(this@DetailActivity, SavedActivity::class.java))
                                }
                            })
                        }
                        builder.setNegativeButton("No") {_, _ -> }
                        builder.setTitle("Delete saved recipe?")
                        builder.setMessage("Are you sure you want to delete your saved recipe?")
                        builder.create().show()
                        return true
                    }
                    //in the same way you can implement others
                    R.id.menuUpdate -> {
                        recipeViewModel.readAllSavedRecipes.observe(this@DetailActivity, Observer { recipes ->
                            val recipe = recipes.find { it.id == id }
                            if (recipe != null) {
                                val intent = Intent(this@DetailActivity, UpdateRecipeActivity::class.java)
                                intent.putExtra("savedRecipe", recipe)
                                startActivity(intent)
                            }
                        })
                        return true
                    }
                }
                return false
            }
        })
        popupMenu.show()
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        changeAmount(Integer.parseInt(p0?.selectedItem.toString()))
    }

    private fun changeAmount(selectedNumber: Int?) {
        if (this::recipe.isInitialized) {
            var servings = 0
            when (selectedNumber) {
                1 -> servings = 1
                2 -> servings = 2
                3 -> servings = 3
                4 -> servings = 4
                5 -> servings = 5
                6 -> servings = 6
                7 -> servings = 7
                8 -> servings = 8
            }
            tvIngredients.text = ""
            tvCalories.text = ""
            tvServings.text = ""
            if (fromActivity == "home") {
                for (ingr in recipe.nutrition!!.ingredients!!) {
                    if (ingr != null) {
                        tvIngredients.append(ingr.name.plus(": ").plus(ingr.amount?.times(servings)).plus(" ").plus(ingr.unit).plus("\n"))
                    }
                }
                tvCalories.text = recipe.nutrition!!.nutrients!!.get(0)!!.amount?.times(servings).toString().plus(" kcal")
                tvServings.text = recipe.servings.times(servings).toString()
            } else {
                for (ingr in savedRecipe.nutrition!!.ingredients!!) {
                    if (ingr != null) {
                        tvIngredients.append(ingr.name.plus(": ").plus(ingr.amount?.times(servings)).plus(" ").plus(ingr.unit).plus("\n"))
                    }
                }
                tvCalories.text = savedRecipe.nutrition!!.nutrients!!.get(0)!!.amount?.times(servings).toString().plus(" kcal")
                tvServings.text = savedRecipe.servings.times(servings).toString()
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        //TODO("Not yet implemented")
    }

}