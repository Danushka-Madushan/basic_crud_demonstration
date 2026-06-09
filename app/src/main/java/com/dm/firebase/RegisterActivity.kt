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
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        // Initialize Firebase Auth
        auth = Firebase.auth
        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("users")

        val nameLayout = findViewById<TextInputLayout>(R.id.input_layout_name)
        val emailLayout = findViewById<TextInputLayout>(R.id.input_layout_email)
        val passwordLayout = findViewById<TextInputLayout>(R.id.input_layout_password)
        val confirmPasswordLayout =
            findViewById<TextInputLayout>(R.id.input_layout_confirm_password)

        val nameText = findViewById<EditText>(R.id.edit_text_name)
        val emailText = findViewById<EditText>(R.id.edit_text_email)
        val passwordText = findViewById<EditText>(R.id.edit_text_password)
        val passwordConfirmText = findViewById<EditText>(R.id.edit_text_confirm_password)
        val loginLink = findViewById<TextView>(R.id.text_login_link)
        val registerButton = findViewById<Button>(R.id.btn_register)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        registerButton.setOnClickListener {
            val name = nameText.text.toString().trim()
            val email = emailText.text.toString().trim()
            val password = passwordText.text.toString().trim()
            val confirmPassword = passwordConfirmText.text.toString().trim()

            var isValid = true

            if (name.isEmpty()) {
                nameLayout.error = "Name is required"
                isValid = false
            } else {
                nameLayout.error = null
            }

            if (email.isEmpty()) {
                emailLayout.error = "Email is required"
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.error = "Enter a valid email address"
                isValid = false
            } else {
                emailLayout.error = null
            }

            if (password.isEmpty()) {
                passwordLayout.error = "Password is required"
                isValid = false
            } else if (password.length < 6) {
                passwordLayout.error = "Password must be at least 6 characters"
                isValid = false
            } else {
                passwordLayout.error = null
            }

            if (confirmPassword.isEmpty()) {
                confirmPasswordLayout.error = "Confirm your password"
                isValid = false
            } else if (confirmPassword != password) {
                confirmPasswordLayout.error = "Passwords do not match"
                isValid = false
            } else {
                confirmPasswordLayout.error = null
            }

            if (isValid) {
                progressBar.visibility = View.VISIBLE
                registerUser(name, email, password, progressBar)
            }
        }

        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(
        name: String,
        email: String,
        password: String,
        progressBar: ProgressBar
    ) {
        // Launch a coroutine on the lifecycle scope
        lifecycleScope.launch {
            try {
                // 1. Wait for Auth to finish using .await()
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid

                if (userId != null) {
                    // 2. Map data
                    val userProfile = User(name, email)

                    // 3. Wait for Database write to finish using .await()
                    database.child(userId).setValue(userProfile).await()

                    // 4. If we get here, both succeeded!
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@RegisterActivity, "Registration Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@RegisterActivity, "Failed to get User ID", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                // Because we used .await(), this catch block WILL successfully
                // catch both Authentication errors and Database errors!
                progressBar.visibility = View.GONE
                Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}