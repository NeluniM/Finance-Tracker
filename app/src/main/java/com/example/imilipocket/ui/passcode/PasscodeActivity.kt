package com.example.imilipocket.ui.passcode

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.imilipocket.databinding.ActivityPasscodeBinding
import com.example.imilipocket.ui.dashboard.DashboardActivity
import com.example.imilipocket.data.PreferenceManager

class PasscodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasscodeBinding
    private lateinit var preferenceManager: PreferenceManager
    private var attemptCount = 0
    private val maxAttempts = 3
    private val correctUsername = "Neluni"
    private val correctPasscode = "1234"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasscodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)

        setupInputValidation()
        setupClickListeners()
    }

    private fun setupInputValidation() {
        // Username validation
        binding.editUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.usernameLayout.error = when {
                    s.isNullOrEmpty() -> "Username is required"
                    s.length < 3 -> "Username must be at least 3 characters"
                    else -> null
                }
            }
        })

        // Password validation
        binding.editPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.passwordLayout.error = when {
                    s.isNullOrEmpty() -> "Password is required"
                    s.length < 4 -> "Password must be at least 4 characters"
                    else -> null
                }
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnSubmit.setOnClickListener {
            if (validateInputs()) {
                attemptLogin()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val username = binding.editUsername.text.toString()
        val password = binding.editPassword.text.toString()
        var isValid = true

        // Username validation
        if (username.isEmpty()) {
            binding.usernameLayout.error = "Username is required"
            isValid = false
        } else if (username.length < 3) {
            binding.usernameLayout.error = "Username must be at least 3 characters"
            isValid = false
        } else {
            binding.usernameLayout.error = null
        }

        // Password validation
        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 4) {
            binding.passwordLayout.error = "Password must be at least 4 characters"
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        return isValid
    }

    private fun attemptLogin() {
        val username = binding.editUsername.text.toString()
        val password = binding.editPassword.text.toString()

        // Show loading state
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = false

        // Simulate network delay
        binding.root.postDelayed({
            if (username == correctUsername && password == correctPasscode) {
                loginSuccess()
            } else {
                loginFailed()
            }
            // Hide loading state
            binding.progressBar.visibility = View.GONE
            binding.btnSubmit.isEnabled = true
        }, 1000)
    }

    private fun loginSuccess() {
        // Reset attempt count
        attemptCount = 0
        
        // Save login state
        preferenceManager.setLoggedIn(true)
        
        // Navigate to dashboard
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun loginFailed() {
        attemptCount++
        
        if (attemptCount >= maxAttempts) {
            // Lock out user
            binding.btnSubmit.isEnabled = false
            Toast.makeText(
                this,
                "Too many failed attempts. Please try again later.",
                Toast.LENGTH_LONG
            ).show()
            
            // Reset after 1 minute
            binding.root.postDelayed({
                attemptCount = 0
                binding.btnSubmit.isEnabled = true
                Toast.makeText(
                    this,
                    "You can try again now.",
                    Toast.LENGTH_SHORT
                ).show()
            }, 60000) // 1 minute
        } else {
            val remainingAttempts = maxAttempts - attemptCount
            Toast.makeText(
                this,
                "Invalid username or password. $remainingAttempts attempts remaining.",
                Toast.LENGTH_SHORT
            ).show()
            binding.editPassword.text?.clear()
        }
    }
} 