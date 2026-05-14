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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // 1. Safe State Setup
            LocaleHelper.updateConfiguration(this, LocaleHelper.getLanguage(this))
            
            setContentView(R.layout.activity_register)

            // Back arrow — icon is set in XML, just handle the click
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.registerToolbar)
                .setNavigationOnClickListener { finish() }

            val etName: TextInputEditText = findViewById(R.id.etName)
            val etEmail: TextInputEditText = findViewById(R.id.etEmail)
            val etPhone: TextInputEditText = findViewById(R.id.etPhone)
            val etPassword: TextInputEditText = findViewById(R.id.etPassword)
            val btnSubmitRegister: Button = findViewById(R.id.btnSubmitRegister)
            val tvGoToLogin: TextView = findViewById(R.id.tvGoToLogin)
            val btnToggleLanguage: Button = findViewById(R.id.btnToggleLanguage)

            btnToggleLanguage.setOnClickListener {
                val currentLang = LocaleHelper.getLanguage(this)
                val newLang = if (currentLang == "kn") "en" else "kn"
                LocaleHelper.setLocale(this, newLang)
                recreate()
            }

            btnSubmitRegister.setOnClickListener {
                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (name.isNotEmpty() && (email.isNotEmpty() || phone.isNotEmpty()) && password.isNotEmpty()) {
                    val userDao = AppDatabase.getDatabase(this).userDao()
                    val passwordHash = password.hashCode().toString()
                    val joinedDate = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())

                    lifecycleScope.launch {
                        try {
                            // Check for duplicate email/phone
                            val existingByEmail = if (email.isNotEmpty()) {
                                withContext(Dispatchers.IO) { userDao.getUserByEmail(email) }
                            } else null

                            val existingByPhone = if (phone.isNotEmpty()) {
                                withContext(Dispatchers.IO) { userDao.getUserByPhone(phone) }
                            } else null

                            if (existingByEmail != null || existingByPhone != null) {
                                Toast.makeText(this@RegisterActivity, R.string.user_already_exists, Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            val newUser = User(
                                name = name,
                                email = email,
                                phone = phone,
                                passwordHash = passwordHash,
                                joinedDate = joinedDate,
                                avatarResId = R.drawable.ic_profile
                            )

                            val userId = withContext(Dispatchers.IO) {
                                userDao.insertUser(newUser)
                            }

                            // Save user session to SharedPreferences
                            val sharedPrefs = getSharedPreferences("GramaSuvidhaPrefs", Context.MODE_PRIVATE)
                            with(sharedPrefs.edit()) {
                                putInt("loggedInUserId", userId.toInt())
                                putString("name", name)
                                putString("email", email)
                                putString("phone", phone)
                                putString("joinedDate", joinedDate)
                                apply()
                            }

                            Toast.makeText(this@RegisterActivity, R.string.register_success, Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } catch (e: Exception) {
                            Toast.makeText(this@RegisterActivity, "DB Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, R.string.error_fill_all, Toast.LENGTH_SHORT).show()
                }
            }

            tvGoToLogin.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        } catch (t: Throwable) {
            android.util.Log.e("RegisterActivity", "Register Init Error", t)
            android.widget.Toast.makeText(this, "Register Error: ${t.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
            setContentView(android.widget.FrameLayout(this))
        }
    }
}
