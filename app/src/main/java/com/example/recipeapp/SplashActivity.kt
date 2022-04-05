package com.example.recipeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.recipeapp.api.RetrofitInstance
import com.example.recipeapp.model.FoodRecipe
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.model.Type
import com.example.recipeapp.utils.Constants.Companion.API_KEY
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : BaseActivity(), EasyPermissions.RationaleCallbacks, EasyPermissions.PermissionCallbacks {

    private var READ_STORAGE_PERM = 123
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //Auth
        auth = FirebaseAuth.getInstance()

        readStorageTask()
        btnGetStarted.setOnClickListener {
            var intent = Intent(this@SplashActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /* override fun onStart() {
        super.onStart()
        /*if (auth.currentUser != null) {
            Intent(this, LogoutActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
            }
        }*/
    }*/

    fun getTypes() {
        var types = ArrayList<Type>() //niz kategorija
        types.addAll(listOf(
            Type(0,"dessert", "https://www.themealdb.com//images//category//dessert.png"),
            Type(0,"main course", "https://www.themealdb.com//images//category//vegetarian.png"),
            Type(0,"side dish", "https://www.themealdb.com//images//category//side.png"),
            Type(0,"breakfast", "https://www.themealdb.com//images//category//breakfast.png"),
            Type(0,"appetizer", "https://www.yummyhealthyeasy.com/wp-content/uploads/2018/06/Low-Carb-Avocado-Shrimp-Cucumber-Appetizer-Square.jpg"),
            Type(0,"salad", "https://www.themealdb.com//images//category//vegan.png"),
            Type(0,"bread", "https://www.seriouseats.com/thmb/KgEDqab_YPR1uKunnno7RfN8Ktc=/880x0/filters:no_upscale():max_bytes(150000):strip_icc():format(webp)/__opt__aboutcom__coeus__resources__content_migration__serious_eats__seriouseats.com__2011__06__20200419-no-knead-bread-vicky-wasik2-a20f97803cb349e38c2c3fad18f767b5.jpg"),
            Type(0,"soup", "https://hips.hearstapps.com/hmg-prod.s3.amazonaws.com/images/homemade-pumpkin-soup-royalty-free-image-1571855802.jpg?crop=0.667xw:1.00xh;0.333xw,0&resize=480:*"),
            Type(0,"sauce", "https://www.cookingclassy.com/wp-content/uploads/2020/05/pizza-sauce-17.jpg"),
            Type(0,"drink", "https://static.toiimg.com/photo/msid-69409726/69409726.jpg?1547795.png")
        ))

        for (type in types) {
            getRecipesByType(type.name) //prosledjivanje svake kategorije
        }
        //dodavanje svih kategorija u bazu
        insertTypesIntoDb(types)

    }

    //getovanje svih jela po kategorijama
    fun getRecipesByType(typeName: String) {
        val call = RetrofitInstance.api.getRecipesByType(true, true, typeName, API_KEY, true)
        call.enqueue(object: Callback<FoodRecipe> {

            override fun onFailure(call: Call<FoodRecipe>, t: Throwable) {
                Toast.makeText(this@SplashActivity, t.message, Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(
                call: Call<FoodRecipe>,
                response: Response<FoodRecipe>
            ) {
                Log.d("reec", response.body().toString())
                if (response.body() != null)
                    insertRecipesIntoDb(typeName, response.body()!!)
            }

        })
    }

    //dodavanje kategorija u bazu
    fun insertTypesIntoDb(types: List<Type>) {
        //add types into room database using coroutines
        launch {
            this.let {
                com.example.recipeapp.data.RecipeDatabase.getDatabase(this@SplashActivity)
                    .recipeDao().addType(Type(0, "all", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d3/Supreme_pizza.jpg/1200px-Supreme_pizza.jpg"))
                for(type in types) {
                    com.example.recipeapp.data.RecipeDatabase.getDatabase(this@SplashActivity)
                        .recipeDao().addType(type)
                }
            }
        }
    }

    //we need category name to get meal list according to category name
    fun insertRecipesIntoDb(typeName: String, foodRecipe: FoodRecipe) {
        //add meal data into room database using coroutines
        launch {
            this.let {
                for (arr in foodRecipe.results) {
                    var recipe = Recipe(
                        arr.id,
                        arr.title,
                        typeName,
                        arr.image,
                        arr.totalTime,
                        arr.servings,
                        arr.analyzedInstructions,
                        arr.nutrition,
                        arr.sourceUrl
                    )
                    com.example.recipeapp.data.RecipeDatabase.getDatabase(this@SplashActivity)
                        .recipeDao().addRecipe(recipe) //dodavanje u bazu
                }
                btnGetStarted.visibility = View.VISIBLE
            }
        }
    }

    fun clearDb() {
        launch {
            this.let {
                //com.example.recipeapp.data.RecipeDatabase.getDatabase(this@SplashActivity).recipeDao().clearDb()
                com.example.recipeapp.data.RecipeDatabase.getDatabase(this@SplashActivity).recipeDao().clearMeal()
            }
        }
    }

    private fun hasReadStoragePermission(): Boolean {
        return EasyPermissions.hasPermissions(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun readStorageTask() {
        if (hasReadStoragePermission()) {
            clearDb()
            getTypes()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "This app need access to your storage",
                READ_STORAGE_PERM,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onRationaleAccepted(requestCode: Int) {
    }

    override fun onRationaleDenied(requestCode: Int) {
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    }
}