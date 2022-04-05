package com.example.recipeapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.recipeapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.bottom_nav
import kotlinx.android.synthetic.main.activity_register.etEmail
import kotlinx.android.synthetic.main.activity_register.etPassword

@SuppressLint("CheckResult")
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
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
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                }
                R.id.addNewRecipe -> {
                    if (auth.currentUser != null) {
                        startActivity(Intent(this@RegisterActivity, AddRecipeActivity::class.java))
                    } else
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                }
                R.id.home -> {
                    startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                }
            }
            false
        }

        //Fullname validation
        val nameStream = RxTextView.textChanges(binding.etFullname)
            .skipInitialValue()
            .map { name ->
                name.isEmpty()
            }
        nameStream.subscribe {
            showNameExistAlert(it)
        }

        //Email validation
        val emailStream = RxTextView.textChanges(binding.etEmail)
            .skipInitialValue()
            .map { email ->
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
        emailStream.subscribe {
            showEmailValidAlert(it)
        }

        //Password validation
        val passwordStream = RxTextView.textChanges(binding.etPassword)
            .skipInitialValue()
            .map { password ->
                password.length < 6
            }
        passwordStream.subscribe {
            showTextMinimalAlert(it)
        }

        //Confirm password validation
        val passwordConfirmStream = Observable.merge(
            RxTextView.textChanges(binding.etPassword)
            .skipInitialValue()
            .map { password ->
                password.toString() != binding.etConfirmPassword.text.toString()
            },
            RxTextView.textChanges(binding.etConfirmPassword)
                .skipInitialValue()
                .map { confirmPassword ->
                    confirmPassword.toString() != binding.etPassword.text.toString()
                })

        passwordConfirmStream.subscribe {
            showPasswordConfirmAlert(it)
        }

        //Button enable true of false
        val invalidFieldsStream = Observable.combineLatest(
            nameStream,
            emailStream,
            passwordStream,
            passwordConfirmStream,
            { nameInvalid: Boolean, emailInvalid: Boolean, passwordInvalid: Boolean, passwordConfirmInvalid: Boolean ->
                !nameInvalid && !emailInvalid && !passwordInvalid && !passwordConfirmInvalid
            })
        invalidFieldsStream.subscribe { isValid ->
            if (isValid) {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.backgroundTintList = ContextCompat.getColorStateList(this, R.color.mint)
            } else {
                binding.btnRegister.isEnabled = false
                binding.btnRegister.backgroundTintList = ContextCompat.getColorStateList(this, R.color.darkGray)
            }
        }

        btnLogRegister.setOnClickListener {
            onBackPressed()
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            registerUser(email, password)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    private fun showNameExistAlert(isNotValid: Boolean) {
        binding.etFullname.error = if (isNotValid) "You need to enter name" else null
    }

    private fun showTextMinimalAlert(isNotValid: Boolean) {
        binding.etPassword.error = if (isNotValid) "Password must have at least 8 digits" else null
    }

    private fun showEmailValidAlert(isNotValid: Boolean) {
        binding.etEmail.error = if (isNotValid) "Email is not valid" else null
    }

    private fun showPasswordConfirmAlert(isNotValid: Boolean) {
        binding.etConfirmPassword.error = if (isNotValid) "Passwords must be same" else null
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    startActivity(Intent(this, LoginActivity::class.java))
                    Toast.makeText(this, "Register successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

}

