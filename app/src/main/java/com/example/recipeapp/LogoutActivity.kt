package com.example.recipeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.recipeapp.databinding.ActivityLogoutBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*

class LogoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogoutBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Auth
        auth = FirebaseAuth.getInstance()

        //bottom navigation
        bottom_nav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.saved -> {
                    startActivity(Intent(this@LogoutActivity, SavedActivity::class.java))
                }
                R.id.home -> {
                    startActivity(Intent(this@LogoutActivity, HomeActivity::class.java))
                }
                R.id.addNewRecipe -> {
                    if (auth.currentUser != null) {
                        startActivity(Intent(this@LogoutActivity, AddRecipeActivity::class.java))
                    } else
                        startActivity(Intent(this@LogoutActivity, LoginActivity::class.java))
                }
            }
            false
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Intent(this, LoginActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
                Toast.makeText(this, "Logout successful", Toast.LENGTH_SHORT).show()
            }
        }
    }
}