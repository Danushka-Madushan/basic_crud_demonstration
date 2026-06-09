package com.dm.firebase

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        auth = Firebase.auth

        val registerLink = findViewById<TextView>(R.id.text_register_link)
        val emailLayout = findViewById<TextInputLayout>(R.id.input_layout_email)
        val passwordLayout = findViewById<TextInputLayout>(R.id.input_layout_password)

        val emailText = findViewById<EditText>(R.id.edit_text_email)
        val passwordText = findViewById<EditText>(R.id.edit_text_password)
        val loginButton = findViewById<Button>(R.id.btn_login)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        loginButton.setOnClickListener {
            val email = emailText.text.toString().trim()
            val password = passwordText.text.toString().trim()

            var isValid = true

            // Email Validation
            if (email.isEmpty()) {
                emailLayout.error = "Email is required"
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.error = "Enter a valid email address"
                isValid = false
            } else {
                emailLayout.error = null
            }

            // Password Validation
            if (password.isEmpty()) {
                passwordLayout.error = "Password is required"
                isValid = false
            } else {
                passwordLayout.error = null
            }

            // Trigger Authentication if form is valid
            if (isValid) {
                progressBar.visibility = View.VISIBLE
                loginUser(email, password, progressBar)
            }
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun loginUser(email: String, password:  String, progressBar: ProgressBar) {
        lifecycleScope.launch {
            try {
                // Execute sign in asynchronously using Coroutines
                val authResult = auth.signInWithEmailAndPassword(email, password).await()

                if (authResult.user != null) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()

                    // Navigate to MainActivity on success
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish() // Close LoginActivity so pressing back won't bring them here again
                }
            } catch (e: Exception) {
                // Handles bad credentials, missing networks, or disabled accounts
                progressBar.visibility = View.GONE
                Toast.makeText(this@LoginActivity, "Login Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // If user is already authenticated, skip login!
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}