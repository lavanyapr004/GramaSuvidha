package com.example.grama_suvidha

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // 1. Safe State Setup
            LocaleHelper.updateConfiguration(this, LocaleHelper.getLanguage(this))
            
            setContentView(R.layout.activity_login)

            // Back arrow — icon is set in XML, just handle the click
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.loginToolbar)
                .setNavigationOnClickListener { finish() }

            val etEmail: TextInputEditText = findViewById(R.id.etEmail)
            val etPassword: TextInputEditText = findViewById(R.id.etPassword)
            val btnSubmitLogin: Button = findViewById(R.id.btnSubmitLogin)
            val tvGoToRegister: TextView = findViewById(R.id.tvGoToRegister)
            val btnToggleLanguage: Button = findViewById(R.id.btnToggleLanguage)

            btnToggleLanguage.setOnClickListener {
                val currentLang = LocaleHelper.getLanguage(this)
                val newLang = if (currentLang == "kn") "en" else "kn"
                LocaleHelper.setLocale(this, newLang)
                recreate()
            }

            btnSubmitLogin.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    // Validate credentials against Room database
                    val userDao = AppDatabase.getDatabase(this).userDao()
                    val passwordHash = password.hashCode().toString()

                    lifecycleScope.launch {
                        try {
                            android.util.Log.d("LoginActivity", "Attempting login with identifier: '$email'")
                            android.util.Log.d("LoginActivity", "Password hash: '$passwordHash'")

                            val user = withContext(Dispatchers.IO) {
                                userDao.login(email, passwordHash)
                            }

                            if (user != null) {
                                android.util.Log.d("LoginActivity", "Login SUCCESS for user: ${user.name} (id=${user.id})")
                                // Save session
                                val prefs = getSharedPreferences("GramaSuvidhaPrefs", Context.MODE_PRIVATE)
                                with(prefs.edit()) {
                                    putInt("loggedInUserId", user.id)
                                    putString("name", user.name)
                                    putString("email", user.email)
                                    putString("phone", user.phone)
                                    putString("joinedDate", user.joinedDate)
                                    apply()
                                }

                                Toast.makeText(this@LoginActivity, R.string.login_success, Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                // Debug: check if user exists at all
                                val byEmail = withContext(Dispatchers.IO) { userDao.getUserByEmail(email) }
                                val byPhone = withContext(Dispatchers.IO) { userDao.getUserByPhone(email) }
                                if (byEmail != null) {
                                    android.util.Log.e("LoginActivity", "User found by email but password mismatch. Stored hash: '${byEmail.passwordHash}', Provided hash: '$passwordHash'")
                                } else if (byPhone != null) {
                                    android.util.Log.e("LoginActivity", "User found by phone but password mismatch. Stored hash: '${byPhone.passwordHash}', Provided hash: '$passwordHash'")
                                } else {
                                    android.util.Log.e("LoginActivity", "No user found with identifier: '$email'. Please register first.")
                                }
                                Toast.makeText(this@LoginActivity, R.string.login_failed, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("LoginActivity", "Login DB Error", e)
                            Toast.makeText(this@LoginActivity, "DB Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, R.string.error_fill_all, Toast.LENGTH_SHORT).show()
                }
            }

            tvGoToRegister.setOnClickListener {
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }
        } catch (t: Throwable) {
            android.util.Log.e("LoginActivity", "Login Init Error", t)
            android.widget.Toast.makeText(this, "Login Error: ${t.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
            setContentView(android.widget.FrameLayout(this))
        }
    }
}
