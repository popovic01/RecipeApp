package com.example.recipeapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.recipeapp.R.anim
import com.example.recipeapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.bottom_nav
import kotlinx.android.synthetic.main.activity_login.etEmail
import kotlinx.android.synthetic.main.activity_login.etPassword

@SuppressLint("CheckResult")
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                        startActivity(Intent(this@LoginActivity, LoginActivity::class.java))
                }
                R.id.addNewRecipe -> {
                    if (auth.currentUser != null) {
                        startActivity(Intent(this@LoginActivity, AddRecipeActivity::class.java))
                    } else
                        startActivity(Intent(this@LoginActivity, LoginActivity::class.java))
                }
                R.id.home -> {
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                }
            }
            false
        }

        //Email validation
        val emailStream = RxTextView.textChanges(binding.etEmail)
            .skipInitialValue()
            .map { email ->
                email.isEmpty()
            }
        emailStream.subscribe {
            showTextMinimalAlert(it, "Email")
        }

        //Password validation
        val passwordStream = RxTextView.textChanges(binding.etPassword)
            .skipInitialValue()
            .map { password ->
                password.isEmpty()
            }
        passwordStream.subscribe {
            showTextMinimalAlert(it, "Password")
        }

        //Button enable true of false
        val invalidFieldsStream = Observable.combineLatest(
            emailStream,
            passwordStream,
            { emailInvalid: Boolean, passwordInvalid: Boolean ->
                !emailInvalid && !passwordInvalid
            })
        invalidFieldsStream.subscribe { isValid ->
            if (isValid) {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.backgroundTintList = ContextCompat.getColorStateList(this, R.color.mint)
            } else {
                binding.btnLogin.isEnabled = false
                binding.btnLogin.backgroundTintList = ContextCompat.getColorStateList(this, R.color.darkGray)
            }
        }

        btnRegLogin.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(anim.slide_from_right, anim.slide_to_left)
        }

        btnLogin.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()
                loginUser(email, password)
        }
    }

    private fun showTextMinimalAlert(isNotValid: Boolean, text: String) {
        if (text == "Email")
            binding.etEmail.error = if (isNotValid) "You need to enter email" else null
        else if (text == "Password")
            binding.etPassword.error = if (isNotValid) "You need to enter password" else null
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { login ->
                if (login.isSuccessful) {
                    Intent(this, LogoutActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, login.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }
}